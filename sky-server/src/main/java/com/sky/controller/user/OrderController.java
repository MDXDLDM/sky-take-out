package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     *
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("提交订单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("提交订单:{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO=orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }
    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     *
     * @param
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("用户查询历史订单")
    public Result<PageResult> history(int page, int pageSize, Integer status){
        log.info("历史订单查询:{},{},{}", page, pageSize, status);
        PageResult pageResult=orderService.history(page,pageSize,status);
        return Result.success(pageResult);
    }

    /**
     *
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查看历史订单详情")
    public Result<OrderVO> getOrderById(@PathVariable("id") Long id){
        log.info("查看历史订单详情:{}",id);
        OrderVO orderVO=orderService.getOrderById(id);
        return Result.success(orderVO);
    }

    /**
     *
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("用户取消订单")
    public Result cancelOrder(@PathVariable("id") Long id){
        log.info("用户取消订单:{}",id);
        orderService.cancelOrder(id);
        return Result.success();
    }

    /**
     *
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result anotherOne(@PathVariable("id") Long id){
        log.info("再来一单:{}",id);
        orderService.anotherOne(id);
        return Result.success();
    }

    /**
     *
     * @return
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("催单")
    public Result remind(@PathVariable("id") Long id){
        log.info("催单:{}",id);
        orderService.remind(id);
        return Result.success();
    }
}
