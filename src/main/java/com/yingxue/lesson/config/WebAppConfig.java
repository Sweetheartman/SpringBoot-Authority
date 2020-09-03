package com.yingxue.lesson.config;

import com.yingxue.lesson.interceptor.CaptchaInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @program: permission-actual-project
 * @description: MVC配置类(配置图片映射以及拦截器)
 * @author: lidekun
 * @create: 2020-08-30 17:54
 **/
@Configuration
public class WebAppConfig implements WebMvcConfigurer {
    /**
     * 虚拟地址
     */
    @Value("${file.static-path}")
    private String fileStaticPath;

    /**
     * 实际地址
     */
    @Value("${file.path}")
    private String filePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(fileStaticPath).addResourceLocations("file:"+filePath);
    }

    /**
     * 将自定义拦截器注入容器
     * @return CaptchaInterceptor
     */
    @Bean
    public CaptchaInterceptor captchaInterceptor(){
        return new CaptchaInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(captchaInterceptor())
                .addPathPatterns("/api/user/login");
    }
}
