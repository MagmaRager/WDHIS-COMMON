package com.wdhis.common.config;

import com.wdhis.common.interceptor.CookieInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ALAN on 2018/8/29.
 */
@Configuration
@ConditionalOnWebApplication
public class WebMvcConfigurerAdapter implements WebMvcConfigurer {

    @Value("${wdhis.datasecurity.key}")
    private String keyCookie = "9588028820109132";  //默认DES密码

    @Value("${wdhis.datasecurity.url}")
    private String urls;

    @Value("${wdhis.datasecurity.url.exclude}")
    private String urlse ;

    @Value("${wdhis.datasecurity.login}")
    private String urllogin;

    @Value("${wdhis.datasecurity.validatetime}")
    private int validateTime;   //生效时间（秒）

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> ul = Arrays.asList(urls.split(","));
        List<String> ule = Arrays.asList(urlse.split(","));
        List<String> ull = Arrays.asList(urllogin.split(","));
        registry.addInterceptor(new CookieInterceptor(keyCookie, ul, ule, ull, validateTime));
    }

}
