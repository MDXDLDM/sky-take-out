package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;
    /**
     * 增加菜品
     * @param dishDTO
     */
    @Transactional
    public void add(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        List<DishFlavor> flavors= dishDTO.getFlavors();
        dishMapper.insert(dish);
        Long dishId = dish.getId();
        if (flavors!=null && !flavors.isEmpty()) {
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            dishFlavorMapper.insertAll(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);
        PageResult pageResult=new PageResult(page.getTotal(),page.getResult());
        return pageResult;
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Transactional
    public void deleteAll(Long[] ids) {
        //如果在售则不能删除
        for (Long id : ids) {
            Dish dish=dishMapper.getById(id);
            if (dish.getStatus()==1){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //如果有与套餐关联的则不能删除
        Long counts=setmealDishMapper.getByDishIds(ids);
        if (counts!=0L){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        dishMapper.delete(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Transactional
    public void update(DishDTO dishDTO) {
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        List<DishFlavor> flavors=dishDTO.getFlavors();
        if (flavors!=null && !flavors.isEmpty()) {
            Long id=dishDTO.getId();
            Long[] ids=new Long[]{id};
            dishFlavorMapper.deleteByDishIds(ids);
            flavors.forEach(dishflavor -> dishflavor.setDishId(id));
            dishFlavorMapper.insertAll(flavors);
        }
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    public DishVO getById(String id) {
        Long dishId=Long.parseLong(id);
        Dish dish=dishMapper.getById(dishId);
        DishVO dishVO=new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        List<DishFlavor> flavors=dishFlavorMapper.getById(dishId);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 起售禁售才菜品
     * @param status
     * @param id
     */
    public void changeStatus(String status, String id) {
        Integer dishStatus=Integer.parseInt(status);
        Long dishId=Long.parseLong(id);
        Dish dish=Dish.builder().status(dishStatus).id(dishId).build();
        dishMapper.update(dish);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     */
    public List<Dish> getDishByCategoryId(String categoryId) {
        Long Id=Long.parseLong(categoryId);
        List<Dish> dishes=dishMapper.getDishByCategoryId(Id);
        return dishes;
    }

}
