package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 查询当前dish是否关联套餐
     * @param dishIds
     * @return
     */
    Long getByDishIds(Long[] dishIds);

    /**
     * 增加所有套餐菜品关系
     * @param setmealDishes
     */
    //@AutoFill(OperationType.INSERT)
    void addAll(List<SetmealDish> setmealDishes);

    /**
     * 删除所有套餐对应的关系
     * @param ids
     */
    void deleteAll(Long[] ids);

    /**
     * 根据套餐id查询关系
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getBySetmealIds(Long id);
}
