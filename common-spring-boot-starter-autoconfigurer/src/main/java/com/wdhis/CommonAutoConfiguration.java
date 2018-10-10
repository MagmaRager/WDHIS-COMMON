package com.wdhis;

import com.wdhis.common.config.datasource.DataSourceNames;
import com.wdhis.common.config.datasource.dynamic.DynamicDataSource;
import com.wdhis.common.config.datasource.dynamic.DynamicDataSourceAspect;
import com.wdhis.common.handler.GlobalExceptionHandler;
import com.wdhis.common.interceptor.MonitorIntercepter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ALAN on 2018/10/9.
 */
@Configuration
@ComponentScan("com.wdhis.common")
public class CommonAutoConfiguration {

}
