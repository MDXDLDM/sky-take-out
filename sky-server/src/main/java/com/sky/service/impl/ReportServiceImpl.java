package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private WorkspaceService workspaceService;
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

    /**
     * 导出运营数据报表
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        //1. 查询数据库，获取营业数据---查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //2. 通过POI将数据写入到Excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
