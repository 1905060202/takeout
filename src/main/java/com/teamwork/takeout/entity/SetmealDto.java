package com.teamwork.takeout.entity;

import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {
    //注意，这里继承了Setmeal，也就是说，SetmealDto拥有Setmeal的所有属性

    private List<SetmealDish> setmealDishes;//套餐关联的菜品集合

    private String categoryName;//分类名称
}
