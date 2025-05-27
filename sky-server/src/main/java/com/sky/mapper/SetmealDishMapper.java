package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealDishMapper {
    /**
     * 查询当前dish是否关联套餐
     * @param dishIds
     * @return
     */
    Long getByDishIds(Long[] dishIds);
}
