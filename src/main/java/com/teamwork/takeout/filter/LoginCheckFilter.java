package com.teamwork.takeout.filter;

import com.alibaba.fastjson.JSON;
import com.teamwork.takeout.common.BaseContext;
import com.teamwork.takeout.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 检查用户是否已经完成登录
 * filterName：过滤器在对象容器中的名字
 * urlPatterns：匹配路径
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;  //获取session中值
        HttpServletResponse response = (HttpServletResponse) servletResponse; //重定向

        //1、获取本次请求的URI
        String requestURI = request.getRequestURI();

//        {}占位符
        log.info("接受到的请求路径:{}", requestURI);

        //定义不需要处理的请求路径--白名单
        String[] urls = new String[]{
                "/employee/login",  // 登陆页 直接放行
                "/employee/logout",//注销页 直接放行
                "/backend/**", //所有的pc端的 前端资源 放行
                "/front/**", //所有的移动端 前端资源 放行
                "/common/**",
                "/user/login",
                "/user/sendMsg"
        };


        //2、判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        //3、如果不需要处理，则直接放行
        if (check) {
            //         filterChain.doFilter 放行
            filterChain.doFilter(servletRequest, servletResponse);
//            直接返回该方法，下面的代码不要执行了
            return;
        }


        //4、判断登录状态，如果已登录，则直接放行
        HttpSession session = request.getSession();
        Object employee = session.getAttribute("employee");

//        如果用户id不等于空，说明已经登陆过，直接放行

        //4-2、判断登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));

            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

        if (employee != null) {

            log.info("用户已经登录，直接放行");

            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(servletRequest, servletResponse);
            //            直接返回该方法，下面的代码不要执行了
            return;
        }


        log.info("用户未登录");
        //5、如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据

//        JSON.toJSONString 将对象 转成 JSON字符串 给前端使用  返回的数据一定是NOTLOGIN  且code 为0
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

        return;

    }


    /**
     * 路径匹配，检查本次请求是否需要放行
     *
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}