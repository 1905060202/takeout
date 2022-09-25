package com.teamwork.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamwork.takeout.common.R;
import com.teamwork.takeout.entity.Category;
import com.teamwork.takeout.entity.Dish;
import com.teamwork.takeout.entity.DishDto;
import com.teamwork.takeout.service.CategoryService;
import com.teamwork.takeout.service.DishFlavorService;
import com.teamwork.takeout.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    //新增菜品
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    //菜品信息分类查询,较难的一部分内容
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件过滤
        queryWrapper.like(name!=null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();//分类id

            Category category = categoryService.getById(categoryId);

            if(categoryId!=null){

                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    //根据id查询菜品信息和对应口味信息
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    //修改菜品信息
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品成功");
    }
    //菜品的启用与禁用
    /**
     * 依据分析 Network 中的请求,在 DishController 中定义 updateStatus 方法
     * 通过菜品 Id 获得所有的菜品（有可能是批量禁用和启用）
     * 通过 Stream 流的形式，设置状态值
     * 调用 updateBatchById 批量进行修改
     * 观察控制台 sql 语句的输出
     * 重启服务器，登陆，进行测试
     * **/

    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable("status") Integer status, @RequestParam("ids") List<Long> ids){
        log.info("status:{},停售起售的ids:{}",status,ids);
        //通过ids批量获取dish菜品
        List<Dish> dishes = dishService.listByIds(ids);
        //利用stream流，设置dish的status字段，传过来的为status值
        dishes = dishes.stream().map(s->{s.setStatus(status);return s;}).collect(Collectors.toList());
        //批量修改
        boolean b = dishService.updateBatchById(dishes);

        return b?R.success("操作成功"):R.error("操作失败");
    }

    //删除菜品 --真删除
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") List<Long> ids){
        log.info("删除的ids：{}",ids);

        boolean deleteStatus = dishService.removeByIds(ids);
        return deleteStatus?R.success("删除成功"):R.error("删除失败");
    }
}    