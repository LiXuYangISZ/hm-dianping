package com.hmdp.config;

import com.hmdp.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author lxy
 * @version 1.0
 * @Description MVC配置类
 * @date 2022/11/24 1:22
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 增加登录拦截器，并对不必要的请求路径排除拦截
        registry.addInterceptor(new LoginInterceptor()).excludePathPatterns(
                "/user/login",
                "/user/code",
                "blog/hot",
                "shop/**",
                "shop-type/**",
                "upload/**",
                "voucher/**"
        );
    }
}
