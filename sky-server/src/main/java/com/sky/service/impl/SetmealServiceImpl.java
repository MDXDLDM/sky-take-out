package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;
    /**
     * 增加套餐
     * @param setmealDTO
     * @return
     */
    @Transactional
    public void addSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);
        Long id=setmeal.getId();
        //套餐菜品关系表也要插入
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(id));
        setmealDishMapper.addAll(setmealDishes);
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page=setmealMapper.pageQuery(setmealPageQueryDTO);
        PageResult pageResult=new PageResult(page.getTotal(),page.getResult());
        return pageResult;
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Transactional
    public void deleteByIds(Long[] ids) {
        //起售中的不能删除
        for (Long id : ids) {
            Setmeal setmeal=setmealMapper.getById(id);
            System.out.println(setmeal.getStatus());
            if (setmeal.getStatus().equals(StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //事务保证一致性
        setmealMapper.deleteAll(ids);
        setmealDishMapper.deleteAll(ids);
    }

    /**
     * "根据id查询套餐"
     * @param id
     * @return
     */
    public SetmealVO getById(String id) {
        Long Id = Long.parseLong(id);
        Setmeal setmeal=setmealMapper.getById(Id);
        SetmealVO setmealVO=new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        List<SetmealDish> setmealDishes=setmealDishMapper.getBySetmealIds(Id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 修改菜品
     * @param setmealDTO
     */
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();
        setmealMapper.update(setmeal);
        if (setmealDishes!=null&& !setmealDishes.isEmpty()){
            //此时需要修改套餐菜品关系
            Long id=setmealDTO.getId();
            setmealDishMapper.deleteAll(new Long[]{id});
            setmealDishMapper.addAll(setmealDishes);
        }
    }

    /**
     * 起售禁售套餐
     * @param status
     * @param id
     */
    public void changeStatus(String status, String id) {
        Integer statusId = Integer.parseInt(status);
        Long setmealId = Long.parseLong(id);
        if (statusId.equals(StatusConstant.ENABLE)) {
            //如果当前套餐中有正在未起售的菜品，则不允许起售
            Long counts=dishMapper.getDishCountsBySetmealId(setmealId);
            if (counts!=0L){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }
        Setmeal setmeal=Setmeal.builder().status(statusId).id(setmealId).build();
        setmealMapper.update(setmeal);
    }

    /**
     *
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> setmeals=setmealMapper.list(setmeal);
        return setmeals;
    }


    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
