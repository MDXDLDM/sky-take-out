package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    /**
     *
     * @param start
     * @param end
     * @return
     */
    public TurnoverReportVO turnoverReport(LocalDate start, LocalDate end) {
        //一个dateList
        //一个turnoverList
        List<LocalDate> dateList=new LinkedList<>();
        List<Double> turnoverList=new LinkedList<>();
        while(true){
            dateList.add(start);
            LocalDateTime timeStart = LocalDateTime.of(start, LocalTime.MIN);
            LocalDateTime timeEnd = LocalDateTime.of(start, LocalTime.MAX);
            Double sum=orderMapper.getByDateAndStatus(timeStart,timeEnd,Orders.COMPLETED);
            if(sum==null){
                sum=0.0;
            }
            turnoverList.add(sum);
            if (start.isEqual(end)){
                break;
            }
            start = start.plusDays(1);
        }
        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
        return turnoverReportVO;
    }
}
