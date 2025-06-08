package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    /**
     *
     * @param ordersSubmitDTO
     * @return
     */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);
    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     *
     * @param
     * @return
     */
    PageResult history(int page,int pageSize,Integer status);

    /**
     *
     * @param id
     * @return
     */
    OrderVO getOrderById(Long id);

    /**
     *
     * @param id
     */
    void cancelOrder(Long id);

    /**
     *
     * @param id
     */
    void anotherOne(Long id);

    /**
     *
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult orderSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     *
     * @return
     */
    OrderStatisticsVO statistics();

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void reject(OrdersRejectionDTO ordersRejectionDTO);

    void cancel(OrdersCancelDTO ordersCancelDTO);

    void delivery(Long id);

    void complete(Long id);
}
