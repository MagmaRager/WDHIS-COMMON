package com.wdhis.common.interceptor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ALAN on 2018/10/8.
 */
@Aspect
@Component
@ConditionalOnProperty(prefix = "wdhis.monitor", name = "on", havingValue = "true", matchIfMissing = true)
public class MonitorIntercepter {

    static final String pstr = "execution(* com.wdhis..*.*(..))";   //com.wdhis下所有方法

    ThreadLocal<Long> startTime = new ThreadLocal<>();// 开始时间

    @Autowired
    MeterRegistry meterRegistry;

    @Value("${wdhis.monitor.intercept_method}")
    private String methods;

    private List<String> interceptMethodList;

    public MonitorIntercepter() {
    }

    @Pointcut(pstr)
    public void cal() {
    }

    //对Controller下面的方法执行前进行切入，初始化开始时间
    //获取需要监视的方法名列表
    @Before("cal()")
    public void beforMethod(JoinPoint jp) {
        if(interceptMethodList == null) {
            interceptMethodList = Arrays.asList(methods.split(","));
        }
        startTime.set(System.currentTimeMillis());
    }

    //对Controller下面的方法执行后进行切入，统计方法执行的次数和耗时情况 
    //注意，这里的执行方法统计的数据不止包含Controller下面的方法，也包括环绕切入的所有方法的统计信息 
    @AfterReturning("cal()")
    public void afterMethod(JoinPoint jp) {
        long end = System.currentTimeMillis();
        long total = end - startTime.get();
        String methodName = jp.getSignature().getName();
        String methodLongName = jp.getSignature().toLongString();

        if(methodInList(methodName, interceptMethodList)) {
            Counter featureCounter = meterRegistry.counter(methodLongName + "_count");
            featureCounter.increment();
            Counter featureTicker = meterRegistry.counter(methodLongName + "_totalms");
            featureTicker.increment(total);
            System.out.println("COUNT: " + featureCounter.count() + " " + featureTicker.count() + "ms");
        }
    }

    @AfterThrowing(value="cal()", throwing="ex")
    public void afterMethod(JoinPoint jp, Exception ex) {
        long end = System.currentTimeMillis();
        long total = end - startTime.get();
        String methodName = jp.getSignature().getName();
        String methodLongName = jp.getSignature().toLongString();

        if(methodInList(methodName, interceptMethodList)) {
            Counter featureCounter = meterRegistry.counter(methodLongName + "_ERROR_count");
            featureCounter.increment();
            Counter featureTicker = meterRegistry.counter(methodLongName + "_ERROR_totalms");
            featureTicker.increment(total);
            System.out.println("ERROR COUNT:" + featureCounter.count() + " " + featureTicker.count() + "ms " + ex.getMessage());
        }
    }

    private boolean methodInList(String method, List<String> methods){
        for (String urlx: methods) {
            if (method.equals(urlx)) {
                return true;
            }
        }
        return false;
    }
}
