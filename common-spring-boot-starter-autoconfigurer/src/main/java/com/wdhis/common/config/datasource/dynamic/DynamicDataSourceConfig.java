package com.wdhis.common.config.datasource.dynamic;

import com.alibaba.druid.pool.DruidDataSource;
import com.wdhis.common.config.datasource.DataSourceNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ALAN on 2018/9/21.
 */
@Configuration
@PropertySource("classpath:ds.properties")
public class DynamicDataSourceConfig {

    @Autowired
    private Environment myEnv;

    @Bean
    @Primary
    public DynamicDataSource dataSource()
    {
        DruidDataSource productDataSource = new DruidDataSource();
        productDataSource.setDbType(myEnv.getProperty("custom.datasource.product.type"));
        productDataSource.setDriverClassName(myEnv.getProperty("custom.datasource.product.driverClassName"));
        productDataSource.setUrl(myEnv.getProperty("custom.datasource.product.url"));
        productDataSource.setUsername(myEnv.getProperty("custom.datasource.product.username"));
        productDataSource.setPassword(myEnv.getProperty("custom.datasource.product.password"));
        productDataSource.setValidationQuery(myEnv.getProperty("custom.datasource.validationQuery"));

        DruidDataSource centreDataSource = new DruidDataSource();
        centreDataSource.setDbType(myEnv.getProperty("custom.datasource.centre.type"));
        centreDataSource.setDriverClassName(myEnv.getProperty("custom.datasource.centre.driverClassName"));
        centreDataSource.setUrl(myEnv.getProperty("custom.datasource.centre.url"));
        centreDataSource.setUsername(myEnv.getProperty("custom.datasource.centre.username"));
        centreDataSource.setPassword(myEnv.getProperty("custom.datasource.centre.password"));
        centreDataSource.setValidationQuery(myEnv.getProperty("custom.datasource.validationQuery"));

        DruidDataSource historyDataSource = new DruidDataSource();
        historyDataSource.setDbType(myEnv.getProperty("custom.datasource.history.type"));
        historyDataSource.setDriverClassName(myEnv.getProperty("custom.datasource.history.driverClassName"));
        historyDataSource.setUrl(myEnv.getProperty("custom.datasource.history.url"));
        historyDataSource.setUsername(myEnv.getProperty("custom.datasource.history.username"));
        historyDataSource.setPassword(myEnv.getProperty("custom.datasource.history.password"));
        historyDataSource.setValidationQuery(myEnv.getProperty("custom.datasource.validationQuery"));

        Map<String, DataSource> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceNames.PRODUCT, productDataSource);
        targetDataSources.put(DataSourceNames.CENTRE, centreDataSource);
        targetDataSources.put(DataSourceNames.HISTORY, historyDataSource);
        return new DynamicDataSource(productDataSource, targetDataSources);
    }

}
