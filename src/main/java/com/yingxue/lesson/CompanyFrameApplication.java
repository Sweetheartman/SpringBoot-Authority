package com.yingxue.lesson;

import cn.shuibo.annotation.EnableSecurity;
import com.yingxue.lesson.filter.CrossOriginFilter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * @author 84382
 */
@SpringBootApplication
@MapperScan("com.yingxue.lesson.mapper")
@EnableSecurity
public class CompanyFrameApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompanyFrameApplication.class, args);
    }

    @Bean
    public CrossOriginFilter crossOriginfilter(){
        return new CrossOriginFilter();
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setName("crossOriginfilter");
        filterRegistrationBean.setFilter(crossOriginfilter());
        filterRegistrationBean.addUrlPatterns("/api/*");
        return filterRegistrationBean;
    }
}
