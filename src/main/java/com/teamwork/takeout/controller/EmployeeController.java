package com.teamwork.takeout.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamwork.takeout.common.BaseContext;
import com.teamwork.takeout.common.R;
import com.teamwork.takeout.entity.Employee;
import com.teamwork.takeout.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

//Spring4之后新加入的注解，原来返回json需要@ResponseBody和@Controller配合。
//即@RestController是@ResponseBody和@Controller的组合注解。
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    /**
     * ①. 将页面提交的密码 password 进行 md5 加密处理 , 得到加密后的字符串
     *
     * ②. 根据页面提交的用户名 username 查询数据库中员工数据信息
     *
     * ③. 如果没有查询到, 则返回登录失败结果
     *
     * ④. 密码比对，如果不一致, 则返回登录失败结果
     *
     * ⑤. 查看员工状态，如果为已禁用状态，则返回员工已禁用结果
     *
     * ⑥. 登录成功，将员工 id 存入 Session：对应参数HttpServletRequest request, 并返回登录成功结果
     */

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @Autowired
    private EmployeeService employeeService;
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        // @RequestBody表示参数是通过请求体传过来的
        /**
         * @RequestBody主要用来接收前端传递给后端的json字符串中的数据的(请求体中的数据的)；
         * 而最常用的使用请求体传参的无疑是POST请求了，所以使用@RequestBody接收数据时，一般都用POST方式进行提交。
         * 在后端的同一个接收方法里，@RequestBody与@RequestParam()可以同时使用，
         * @RequestBody最多只能有一个，而@RequestParam()可以有多个。
         *
         *
         * **/
        //1、将页面提交的密码password进行md5加密处理
        //md5是不可逆的，密文无法被逆向解密
        //只有重置密码，没有找回密码，因为密码已经被加密了，不可逆，开发人员也不知道具体密码是啥
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();//实例化一个Wrapper
        queryWrapper.eq(Employee::getUsername,employee.getUsername());//获取用户名
        Employee emp = employeeService.getOne(queryWrapper);//根据用户名获取对象
        //getone()根据用户名获取1个员工对象
        //如果emp存在，说明数据可中有数据

        //3、如果没有查询到则返回登录失败结果
        if(emp == null){
            return R.error("登录失败");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus() == 0){
            return R.error("账号已禁用");
        }

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("logout ok");
    }

    //员工增加
    /**
     * 保存用户
     * @param request
     * @param employee
     * @return
     */
    /**
     * 保存用户
     * 逻辑修改：先查后增
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("用户的基本信息：{}",employee.toString());
//        0 先查后增
        String username = employee.getUsername();
        QueryWrapper<Employee> wrapper = new QueryWrapper<>();
        wrapper.eq(username!=null,"username",username);
        Employee one = employeeService.getOne(wrapper);
        if (one!=null){
//            如果one不等于null 说明已经存在，提示用户
            return   R.error("添加用户失败，用户名:"+username+ "已存在");
        }
//        1. 设置默认密码 123456   存密文 e10adc3949ba59abbe56e057f20f883e
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        2. 设置默认状态 为启动
        employee.setStatus(1);
//        3. 创建时间 和修改时间 一致
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
////        4. 创建者 和修改者 一致
////        创建者id 已经在登陆的时候，存到Session，取出即可使用
//        HttpSession session = request.getSession();
//        employee.setUpdateUser((Long) session.getAttribute("employee"));
//        employee.setCreateUser((Long) session.getAttribute("employee"));
//       5. 执行保存用户的操作
        boolean savestatus = employeeService.save(employee);

// 三元表达式：  condition?条件为true执行：条件为false的时候执行
        return savestatus?R.success("添加用户成功"):R.error("添加用户失败，请稍后再试");
    }

    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize , String name){
        //分页构造器，传入当前页码，每页多少数据
        Page<Employee> employeePage = new Page<>(page,pageSize);
        //构建条件查询器，模糊查询
        QueryWrapper<Employee> wrapper = new QueryWrapper<>();
        wrapper.like(name!=null,"username",name);

        //开始分页查询
        employeeService.page(employeePage,wrapper);
        return R.success(employeePage);
    }
    /**
     * 禁用启用员工
     * status 0 ---禁用
     * status 1 ---启用
     * 问题：前端获取Long类型id 会出现精度丢失  如1545285566273286145 -->1545285566273286000
     * 解决步鄹：
     * 1.  4.5.3笔记 引入JacksonObjectMapper--->config包
     * 2. 在WebMvcConfig内中，添加JacksonObjectMapper  需要重写的方法是extendMessageConverters
     *
     *
     * @param request
     * @param employee
     * @return
     */
    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());
//
//        Long empId = (Long)request.getSession().getAttribute("employee");
//
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");
    }

//典型的路径参数，通过前端抓包看出来的
    //@PathVariable 可以将 URL 中占位符参数绑定到控制器处理方法的入参中:URL 中的 {xxx} 占位符可以通过
//@PathVariable("xxx") 绑定到操作方法的入参中。
    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }
}
