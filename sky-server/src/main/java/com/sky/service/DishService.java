package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 增加菜品
     * @param dishDTO
     */
    void add(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteAll(Long[] ids);

    /**
     * 修改菜品
     * @param dishDTO
     */
    void update(DishDTO dishDTO);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    DishVO getById(String id);

    /**
     * 起售禁售才菜品
     * @param status
     * @param id
     */
    void changeStatus(String status, String id);

    /**
     * 根据分类id查询菜品
     * @param categoryId
     */
    List<Dish> getDishByCategoryId(String categoryId);
}
