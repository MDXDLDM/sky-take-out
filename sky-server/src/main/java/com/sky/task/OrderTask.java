package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务
 */
@Component
@Slf4j
public class OrderTask {

    private static final String REASON="订单支付超时";
    private static final String key="SHOP_STATUS";
    private static final Integer ON=1;
    private static final Integer OFF=0;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 订单支付超时 每分钟判断
     */
    @Scheduled(cron = "0 * * * * ?")
    public void orderTimeOut(){
        log.info("判断支付超时订单:{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> ordersList=orderMapper.getTimeOut(Orders.PENDING_PAYMENT,time);
        if (ordersList!=null&&ordersList.size()>0){
            for (Orders orders:ordersList){
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason(REASON);
                orders.setCancelTime(LocalDateTime.now());
                //使用一条语句同时更新所有order语法较难实现，只能牺牲性能
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理忘记设置完成的订单 凌晨三点执行
     */
    @Scheduled(cron = "0 30 0 * * ?")
    public void orderDeliveryCheck(){
        log.info("处理未点击完成订单:{}", LocalDateTime.now());
        //处理上个工作日订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-30);
        List<Orders> ordersList=orderMapper.getTimeOut(Orders.DELIVERY_IN_PROGRESS,time);
        if (ordersList!=null&&ordersList.size()>0){
            for (Orders orders:ordersList){
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 开始营业
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void openShop(){
        log.info("开始营业:{}", LocalDateTime.now());
        redisTemplate.opsForValue().set(key, ON);
    }
    /**
     * 停止营业
     */
    @Scheduled(cron = "0 0 22 * * ?")
    public void closeShop(){
        log.info("停止营业:{}", LocalDateTime.now());
        redisTemplate.opsForValue().set(key, OFF);
    }
}

