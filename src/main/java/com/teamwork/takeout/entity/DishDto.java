package com.teamwork.takeout.entity;

import com.teamwork.takeout.entity.Dish;
import com.teamwork.takeout.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}