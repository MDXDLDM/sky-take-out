package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增菜品
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 批量删除菜品
     * @param ids
     */
    void delete(Long[] ids);

    /**
     * 修改菜品
     * @param dish
     */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据id查询菜品
     * @param dishId
     * @return
     */
    @Select("select * from dish where id=#{id}")
    Dish getById(Long dishId);

    /**
     * 起售禁售菜品
     * @param dishStatus
     * @param dishId
     */
//    @Update("update dish set status=#{dishStatus} where id=#{dishId}")
//    @AutoFill(OperationType.UPDATE)
//    void changeStatus(Integer dishStatus, Long dishId);

    /**
     * 根据分类id查询菜品
     * @param id
     * @return
     */
    List<Dish> getDishByCategoryId(Long id);

    /**
     * 根据套餐id查询起售总数
     * @param setmealId
     * @return
     */
    Long getDishCountsBySetmealId(Long setmealId);

    /**
     *
     * @param dish
     * @return
     */

    List<Dish> list(Dish dish);
}
