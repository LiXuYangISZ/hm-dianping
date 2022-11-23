package com.hmdp.interceptor;

import com.hmdp.entity.User;
import com.hmdp.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author lxy
 * @version 1.0
 * @Description 登录拦截器
 * @date 2022/11/24 1:12
 */
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 在进入Controller之前会被执行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取session
        HttpSession session = request.getSession();
        // 2.获取session中的用户
        Object user = session.getAttribute("user");
        // 3.如果用户不存在，则拦截,返回401状态码
        if(user == null){
            response.setStatus(401);
            return false;
        }

        // 4.如果存在，则保存到ThreadLocal
        UserHolder.saveUser((User) user);
        // 5.放行
        return false;
    }

    /**
     * 在执行完Controller里面的逻辑后执行下面代码
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 移除用户
        UserHolder.removeUser();
    }
}
