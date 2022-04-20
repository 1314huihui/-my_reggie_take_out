package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @Description: TODO 员工登录controller层
 * @Author: lyh
 * @Date: 2022/4/16 20:41
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * @Description: TODO 编写登录方法
     * @Author: lyh
     * @Date: 2022/4/16 21:43
     */

    @PostMapping("/login")
    public R<Employee> logn(HttpServletRequest request, @RequestBody Employee employee) {//把穿过来的数据封装成employee对象，需要跟我们的实体类里面的名字对应
        /** 员工登录
         * request作用   登录成功==》员工对象
         * employee   员工对象的id存到session中一份==》如果想获取当前用户随时都能获取
         */
        //  ①. 将页面提交的密码password进行md5加密处理, 得到加密后的字符串
        String password = employee.getPassword();
                //调用md5加密的工具类
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        // ②. 根据页面提交的用户名username查询数据库中员工数据信息
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
                 //条件封装
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        //  ③. 如果没有查询到, 则返回登录失败结果
        if (emp==null) {
            return R.error("登录失败");
        }

        // ④. 密码比对，如果不一致, 则返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("登录失败");
        }

        // ⑤. 查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return  R.error("账号已经禁用");
        }

        // ⑥. 登录成功，将员工id存入Session, 并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());

        return R.success(emp);
    }

    /**
     * @Description: TODO 员工退出方法
     * @Author: lyh
     * @Date: 2022/4/18 9:53
     */
    @PostMapping("/logout")
    public  R<String> logout(HttpServletRequest request) {
        //清理Sesson中保存的当前登录员工的id
         request.getSession().removeAttribute("employee");
        return  R.success("退出成功");
    }

}
