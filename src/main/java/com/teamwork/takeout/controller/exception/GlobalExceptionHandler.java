package com.teamwork.takeout.controller.exception;

import com.teamwork.takeout.common.CustomException;
import com.teamwork.takeout.common.R;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


import java.util.Arrays;

/**
 * 全局异常处理
 * 说明：项目中无论哪个类 出了异常，都会被本类所捕获
 */

//思想：面向切面编程---对原有的代码进行增强 并且不会改变原有的代码
//第一个注解以及值：
//所有的调用一般都是从Controller开始的，因此异常最后会抛到Controller ，只需要接受Controller异常即可
@ControllerAdvice(annotations = {RestController.class, Controller.class})
//异常处理后，需要返回信息给前端，将信息返回到 响应体中
@ResponseBody
//打日志使用 如：log.info()
@Slf4j
public class GlobalExceptionHandler {

        /**
         * 定一个一个专门处理的异常
         * 用来提示用户名重复
         * 因为是Spring框架，因此会先抛DuplicateKeyException异常，然后在抛SQLIntegrityConstraintViolationException异常，
         * 所以需要方法中先捕获DuplicateKeyException异常

         */
        @ExceptionHandler(DuplicateKeyException.class)
        public R<String> doDuplicateKeyExceptionExceptionHandler(DuplicateKeyException ex){
            log.error("异常信息：{}",ex.getMessage());
//            : Duplicate entry 'guest' for key 'idx_username'
//        1.判断 错误消息 是否包含 Duplicate entry 字符串
            boolean duplicate_entry = ex.getMessage().contains("Duplicate entry");
//        如果包含，则进行切割字符串
            if (duplicate_entry){
                String[] s = ex.getMessage().split(" ");
//            Arrays.toString(数组)  能将数组 转化成字符串
                log.info("数组对象：{}",s);

                log.info("数组：{}", Arrays.toString(s));
                return R.error("用户名:"+s[9]+"重复");
            }

            return R.error("未知错误");
        }


        /**
         * 异常处理方法
         * @ExceptionHandler(Exception.class)
         * 代表 可以接受Exception以及其子类的所有异常
         * @return
         */
        @ExceptionHandler(Exception.class)
        public R<String> doOtherExceptionHandler(Exception ex){
            log.error(ex.getMessage());
            return R.error("服务器正忙，请稍后");
        }

    /**
     * 异常处理方法
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }
}
