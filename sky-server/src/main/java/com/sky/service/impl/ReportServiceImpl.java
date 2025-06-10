package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    /**
     *
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO turnoverReport(LocalDate begin, LocalDate end) {
        //一个dateList
        //一个turnoverList
        List<LocalDate> dateList=new LinkedList<>();
        List<Double> turnoverList=new LinkedList<>();
        while(true){
            dateList.add(begin);
            LocalDateTime timeStart = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime timeEnd = LocalDateTime.of(begin, LocalTime.MAX);
            Double sum=orderMapper.getByDateAndStatus(timeStart,timeEnd,Orders.COMPLETED);
            if(sum==null){
                sum=0.0;
            }
            turnoverList.add(sum);
            if (begin.isEqual(end)){
                break;
            }
            begin = begin.plusDays(1);
        }
        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
        return turnoverReportVO;
    }

    @Override
    public UserReportVO userReport(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new LinkedList<>();
        List<Integer> newUserList=new LinkedList<>();
        List<Integer> allUserList=new LinkedList<>();
        while(true){
            dateList.add(begin);
            LocalDateTime timeStart = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime timeEnd = LocalDateTime.of(begin, LocalTime.MAX);
            Integer newCount=userMapper.getByDate(timeStart,timeEnd);
            Integer allCount=userMapper.getAllUserCounts(timeEnd);
            if (newCount==null){
                newCount=0;
            }
            if (allCount==null){
                allCount=0;
            }
            newUserList.add(newCount);
            allUserList.add(allCount);
            if (begin.isEqual(end)){
                break;
            }
            begin = begin.plusDays(1);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(allUserList,","))
                .build();
    }

    /**
     *
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO orderReport(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new LinkedList<>();
        List<Integer> totalOrderList=new LinkedList<>();
        List<Integer> validOrderList=new LinkedList<>();
        Integer totalCount=0;
        Integer validCount=0;
        while(true){
            dateList.add(begin);
            LocalDateTime timeStart = LocalDateTime.of(begin, LocalTime.MIN);
            LocalDateTime timeEnd = LocalDateTime.of(begin, LocalTime.MAX);
            Integer todayTotalCount=orderMapper.getCountByDate(timeStart,timeEnd,null);
            Integer todayValidCount=orderMapper.getCountByDate(timeStart,timeEnd,Orders.COMPLETED);
            if (todayTotalCount==null){
                todayTotalCount=0;
            }
            if (todayValidCount==null){
                todayValidCount=0;
            }
            totalOrderList.add(todayTotalCount);
            validOrderList.add(todayValidCount);
            totalCount+=todayTotalCount;
            validCount+=todayValidCount;
            if (begin.isEqual(end)){
                break;
            }
            begin = begin.plusDays(1);
        }
        return OrderReportVO.builder()
                .totalOrderCount(totalCount)
                .validOrderCount(validCount)
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(totalOrderList,","))
                .validOrderCountList(StringUtils.join(validOrderList,","))
                .orderCompletionRate((double)validCount/totalCount)
                .build();
    }

    /**
     *
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO salesReport(LocalDate begin, LocalDate end) {
        //找到符合条件的订单
        LocalDateTime timeStart = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime timeEnd = LocalDateTime.of(end, LocalTime.MAX);
//        List<Orders> orders=orderMapper.getByDates(timeStart,timeEnd);
//        Map<String,Integer> map=new TreeMap<>();
//        //找每一个order的orderDetail
//        for (Orders order:orders){
//            List<OrderDetail> details=orderDetailMapper.getByOrderId(order.getId());
//            for(OrderDetail orderDetail:details){
//                String name=orderDetail.getName();
//                Integer number = orderDetail.getNumber();
//                if (!map.containsKey(name)){
//                    map.put(name,0);
//                }
//                map.put(name,map.get(name)+number);
//            }
//        }
//        //构造返回值
//        int count=0;int size=map.size();
//        while(count<10&&count<size){
//
//        }
        List<GoodsSalesDTO> goodsSalesDTOS=orderMapper.getTop10(timeStart,timeEnd,Orders.COMPLETED);
        List<String> nameList = goodsSalesDTOS.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = goodsSalesDTOS.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        //
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }
}
