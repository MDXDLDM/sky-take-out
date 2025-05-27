package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    /**
     *
     * @param flavors
     */
    void insertAll(List<DishFlavor> flavors);

    /**
     * 根据dishid删除
     * @param ids
     */
    void deleteByDishIds(Long[] ids);

    /**
     *
     * @param id
     * @return
     */
    @Select("select * from dish_flavor where dish_id=#{id}")
    List<DishFlavor> getById(Long id);
}
