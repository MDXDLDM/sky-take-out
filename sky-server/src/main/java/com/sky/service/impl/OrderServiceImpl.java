package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;
    /**
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理业务异常:地址错误、购物车为空
        AddressBook byId = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (byId == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        ShoppingCart shoppingCarts=new ShoppingCart();
        shoppingCarts.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCarts);
        if (list==null){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //插入一条订单数据
        Orders orders=new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setNumber(String.valueOf(System.currentTimeMillis())+BaseContext.getCurrentId());
        orders.setPhone(byId.getPhone());
        orders.setConsignee(byId.getConsignee());
        orders.setUserId(BaseContext.getCurrentId());
        orders.setAddress(byId.getProvinceName()+byId.getCityName()+byId.getDistrictName()+byId.getDetail());

        orderMapper.insert(orders);
        //订单明细表中插入多条数据
        //每一项从购物车中获取
        List<OrderDetail> details=new LinkedList<>();
        for (ShoppingCart shoppingCart : list) {
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(shoppingCart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            details.add(orderDetail);
        }
        orderDetailMapper.insertAll(details);
        //清空购物车
        shoppingCartMapper.deleteAll(shoppingCarts);
        //
        OrderSubmitVO orderSubmitVO=OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "川小川外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        //为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改
        Integer OrderPaidStatus = Orders.PAID; //支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED;  //订单状态，待接单

        //发现没有将支付时间 check_out属性赋值，所以在这里更新
        LocalDateTime check_out_time = LocalDateTime.now();

        //获取订单号码
        String orderNumber = ordersPaymentDTO.getOrderNumber();

        log.info("调用updateStatus，用于替换微信支付更新数据库状态的问题");
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, orderNumber);
        Orders orders=orderMapper.getByNumber(orderNumber);
        //websocker推送消息
        Map map=new HashMap<>();
        map.put("type",1);
        map.put("orderId",orders.getId());
        map.put("content","订单号:"+orderNumber);
        //
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     *
     * @param
     * @return
     */
    public PageResult history(int page,int pageSize,Integer status) {
        PageHelper.startPage(page,pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);
        Page<Orders> pages=orderMapper.history(ordersPageQueryDTO);
        List<OrderVO> list=new LinkedList<>();
        for (Orders order : pages) {
            Long orderId = order.getId();
            List<OrderDetail> details=orderDetailMapper.getByOrderId(orderId);
            OrderVO orderVO=new OrderVO();
            BeanUtils.copyProperties(order,orderVO);
            orderVO.setOrderDetailList(details);
            list.add(orderVO);
        }
        PageResult pageResult=new PageResult(pages.getTotal(), list);
        return pageResult;
    }

    /**
     *
     * @param id
     * @return
     */
    public OrderVO getOrderById(Long id) {
        Orders order=orderMapper.getById(id);
        List<OrderDetail> details=orderDetailMapper.getByOrderId(id);
        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(order,orderVO);
        orderVO.setOrderDetailList(details);
        return orderVO;
    }

    /**
     *
     * @param id
     */
    public void cancelOrder(Long id) {
        Orders order =orderMapper.getById(id);
        order.setStatus(Orders.CANCELLED);
        if (order.getPayStatus()==Orders.PAID){
            order.setPayStatus(Orders.REFUND);
        }else{
            order.setPayStatus(Orders.UN_PAID);
        }
        orderMapper.update(order);
    }

    /**
     *
     * @param id
     */
    @Transactional
    public void anotherOne(Long id) {
        //清空原有购物车
        ShoppingCart shoppingCart=new ShoppingCart();
        shoppingCart.setId(BaseContext.getCurrentId());
        shoppingCartMapper.deleteAll(shoppingCart);
        //将历史订单的所有加入购物车
        List<OrderDetail> details=orderDetailMapper.getByOrderId(id);
        List<ShoppingCart> cartList=new LinkedList<>();
        for (OrderDetail orderDetail : details) {
            ShoppingCart shoppingCart1=new ShoppingCart();
            BeanUtils.copyProperties(orderDetail,shoppingCart1);
            cartList.add(shoppingCart1);
            shoppingCart1.setCreateTime(LocalDateTime.now());
            shoppingCart1.setUserId(BaseContext.getCurrentId());
        }
        shoppingCartMapper.insertAll(cartList);
    }

    //管理端接口

    /**
     *
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult orderSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.history(ordersPageQueryDTO);

        // 部分订单状态，需要额外返回订单菜品信息，将Orders转化为OrderVO
        List<OrderVO> orderVOList = getOrderVOList(page);

        return new PageResult(page.getTotal(), orderVOList);
    }


    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单菜品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderDishes = getOrderDishesStr(orders);

                // 将订单菜品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        // 查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        // 将该订单对应的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

    /**
     *
     * @return
     */
    public OrderStatisticsVO statistics() {
        Integer confirmed=orderMapper.getByStatus(Orders.CONFIRMED);
        Integer delivery=orderMapper.getByStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer toBeConfirmed=orderMapper.getByStatus(Orders.TO_BE_CONFIRMED);
        OrderStatisticsVO orderStatisticsVO=new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(delivery);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        return orderStatisticsVO;
    }

    /**
     *
     * @param ordersConfirmDTO
     */
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders=Orders.builder().id(ordersConfirmDTO.getId()).status(Orders.CONFIRMED).build();
        orderMapper.update(orders);
    }

    /**
     *
     * @param ordersRejectionDTO
     */
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        // 订单只有存在且状态为2（待接单）才可以拒单
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 拒单需要退款，根据订单id更新订单状态、拒单原因、取消时间
        Orders orders = new Orders();
        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == Orders.PAID) {
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }


    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == 1) {
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }
    /**
     * 派送订单
     *
     * @param id
     */
    public void delivery(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }
    /**
     * 完成订单
     *
     * @param id
     */
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     *
     * @param id
     */
    public void remind(Long id) {
        Orders order = orderMapper.getById(id);
        if(order==null){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Map map=new HashMap();
        map.put("type",2);
        map.put("orderId",id);
        map.put("content","订单号:"+order.getNumber());
        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }
}
