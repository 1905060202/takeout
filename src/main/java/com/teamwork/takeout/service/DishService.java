package com.teamwork.takeout.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.teamwork.takeout.entity.Dish;
import com.teamwork.takeout.entity.DishDto;

public interface DishService extends IService<Dish> {
    //新增菜品。同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息
    public void updateWithFlavor(DishDto dishDto);

}