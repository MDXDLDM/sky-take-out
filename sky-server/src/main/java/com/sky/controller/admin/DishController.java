package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    private static final String PATTERN="dish_*";
    /**
     * 增加员工
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("增加菜品")
    public Result add(@RequestBody DishDTO dishDTO){
        log.info("增加菜品:{}",dishDTO);
        dishService.add(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询:{}",dishPageQueryDTO);
        PageResult pageResult=dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result deleteAll(@RequestParam Long[] ids){
        log.info("批量删除菜品:{}",ids);
        cleanCache(PATTERN);
        dishService.deleteAll(ids);
        return Result.success();
    }

    /**
     * 修改菜品
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品:{}",dishDTO);
        cleanCache(PATTERN);
        dishService.update(dishDTO);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable String id){
        log.info("根据id查询菜品:{}",id);
        DishVO dishVO=dishService.getById(id);
        return Result.success(dishVO);
    }

    /**
     * 起售禁售菜品
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("起售禁售菜品")
    public Result chageStatus(@PathVariable String status,String id){
        log.info("起售禁售菜品:{},{}",id,status);
        cleanCache(PATTERN);
        dishService.changeStatus(status,id);
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> getDishByCategoryId(String categoryId){
        log.info("根据分类id查询菜品:{}",categoryId);
        List<Dish> dishes=dishService.getDishByCategoryId(categoryId);
        return Result.success(dishes);
    }

    /**
     * 清理redis缓存
     * @param pattern
     */
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
