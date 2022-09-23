package com.teamwork.takeout.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teamwork.takeout.entity.Dish;
import com.teamwork.takeout.mapper.DishMapper;
import com.teamwork.takeout.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper,Dish> implements DishService {
}