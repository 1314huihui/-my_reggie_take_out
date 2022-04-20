package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Description: TODO 过滤器解决未登录访问页面现象
 * @Author: lyh
 * @Date: 2022/4/18 21:23
 */

//过滤器名称为loginCheckFilter，需要拦截所有的请求
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LonginCheckFilter implements Filter {

    /**
     *
     * @param servletRequest
     * @param servletResponse
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     * A. 获取本次请求的URI
     * B. 判断本次请求, 是否需要登录, 才可以访问
     * C. 如果不需要，则直接放行
     * D. 判断登录状态，如果已登录，则直接放行
     * E. 如果未登录, 则返回未登录结果
     */
    //而现在有个问题，我们的这个路径"/backend/**"   和  /backend/index.html不一致，无法匹配这个
    //路径匹配器，专门用来路径比较的，支持通配符匹配.是spring提供的一个工具类
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response =(HttpServletResponse) servletResponse;
        //A. 获取本次请求的URI
        String requestURI = request.getRequestURI();  //backend/index.html
        //B. 判断本次请求, 是否需要登录, 才可以访问
                //只拦截数据，不拦截页面.静态页面全部放行

        String[] urls = new String[] {  //不需要放行的路径
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        };

        //B. 判断本次请求, 是否需要登录, 才可以访问
        boolean check = check(urls, requestURI);

        //C. 如果不需要，则直接放行    匹配到给定的几个路径，放行
        if (check) {
            filterChain.doFilter(request,response);
            return;
        }

        //D. 判断登录状态，如果已登录，则直接放行
                //登录成功会把登录者id存到session中，即session不为空状态
        if (request.getSession().getAttribute("employee")!=null) {
            filterChain.doFilter(request,response);
            return;
        }

        // E. 如果未登录, 则返回未登录结果。通过输出流的方式向我们客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));  //未登录把R对象转化为json对象，直接输出
        log.info("拦截到请求：{}",request.getRequestURI() );     //{} 占位符的使用
        return;
    }
    /**
     * @Description: TODO 路径匹配，检查本次请求是否需要放行
     * @Author: lyh
     * @Date: 2022/4/19 18:18
     */

    public boolean check(String[] urls,String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
             return true;
            }
        }
        return false;
    }
}
