package com.teamwork.takeout.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teamwork.takeout.common.R;
import com.teamwork.takeout.entity.Dish;
import com.teamwork.takeout.entity.DishDto;
import com.teamwork.takeout.entity.DishFlavor;
import com.teamwork.takeout.mapper.DishMapper;
import com.teamwork.takeout.service.DishFlavorService;
import com.teamwork.takeout.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper,Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    //新增菜品，同时保存对应的口味数据
    /**
     * 页面传递的菜品口味信息，仅仅包含 name 和 value 属性，缺少一个非常重要的属性 dishId，
     * 所以在保存完菜品的基本信息后，我们需要获取到菜品 ID，然后为菜品口味对象属性 dishId 赋值。
     *
     * 具体逻辑如下：
     *
     * ①. 保存菜品基本信息 ;
     *
     * ②. 获取保存的菜品 ID ;
     *
     * ③. 获取菜品口味列表，遍历列表，为菜品口味对象属性 dishId 赋值;
     *
     * ④. 批量保存菜品口味列表;
     * **/
    @Transactional
    //由于在 saveWithFlavor 方法中，进行了两次数据库的保存操作，操作了两张表，
    // 那么为了保证数据的一致性，我们需要在方法上加上注解 @Transactional 来控制事务。
    public void saveWithFlavor(DishDto dishDto){
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);
        //一次数据库保存操作
        Long dishId = dishDto.getId();
        //菜品口味
        // stream().map()可以让你转化一个对象成其他的对象
        //collect(Collectors.toList()),将数据收集进一个列表(Stream 转换为 List，允许重复值，有顺序)
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors =  flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
        //二次数据库保存操作
    }

    //根据id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id){
        //查询菜品基本信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        //复制属性
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto){
        //更新dish表基本信息
        this.updateById(dishDto);
        //清理当前菜品对应口味数据---dish flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据--dish flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }
}