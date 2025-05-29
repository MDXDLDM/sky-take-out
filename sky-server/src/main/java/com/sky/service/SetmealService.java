package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;
import org.springframework.stereotype.Service;


public interface SetmealService {

    /**
     * 增加套餐
     * @param setmealDTO
     * @return
     */
    void addSetmeal(SetmealDTO setmealDTO);

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteByIds(Long[] ids);

    /**
     * "根据id查询套餐"
     * @param id
     * @return
     */
    SetmealVO getById(String id);

    /**
     * 修改菜品
     * @param setmealDTO
     */
    void update(SetmealDTO setmealDTO);

    /**
     * 起售禁售套餐
     * @param status
     * @param id
     */
    void changeStatus(String status, String id);
}
