package com.wdhis.common.config.datasource.dynamic;

import com.wdhis.common.config.datasource.DataSourceNames;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


/**
 * 切换数据源Advice
 */
@Aspect
@Order(-10)//保证该AOP在@Transactional之前执行
@Component
public class DynamicDataSourceAspect {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Pointcut("@annotation(com.wdhis.common.config.datasource.dynamic.TargetDataSource)")//注意：这里的xxxx代表的是上面public @interface DataSource这个注解DataSource的包名
    public void dataSourcePointCut() {

    }

    @Around("dataSourcePointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        TargetDataSource ds = method.getAnnotation(TargetDataSource.class);
        if (ds == null) {
            DynamicDataSource.setDataSource(DataSourceNames.PRODUCT);
            logger.debug("set datasource is " + DataSourceNames.PRODUCT);
        } else {
            DynamicDataSource.setDataSource(ds.value());
            logger.debug("set datasource is " + ds.value());
        }
        try {
            return point.proceed();
        } finally {
            DynamicDataSource.clearDataSource();
            logger.debug("clean datasource");
        }
    }


}