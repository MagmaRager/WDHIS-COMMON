package com.wdhis.common.interceptor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ALAN on 2018/10/8.
 */
@Aspect
@Component
//@ConditionalOnProperty(prefix = "wdhis.common.monitor", name = "on", havingValue = "true", matchIfMissing = true)
public class MonitorIntercepter {

    static final String pstr = "execution(* com.wdhis..*.*(..))";   //com.wdhis下所有方法

    ThreadLocal<Long> startTime = new ThreadLocal<>();// 开始时间

    @Autowired
    MeterRegistry meterRegistry;

//    @Value("${wdhis.common.monitor.intercept_url}")
//    private String urls;

//    private List<String> interceptUrlList;
//
//    public MonitorIntercepter() {
//        interceptUrlList = Arrays.asList(urls.split(","));
//    }


    @Pointcut(pstr)
    public void cal() {
    }

    //对Controller下面的方法执行前进行切入，初始化开始时间 
    @Before("cal()")
    public void beforMethod(JoinPoint jp) {
        startTime.set(System.currentTimeMillis());
    }

    //对Controller下面的方法执行后进行切入，统计方法执行的次数和耗时情况 
    //注意，这里的执行方法统计的数据不止包含Controller下面的方法，也包括环绕切入的所有方法的统计信息 
    @AfterReturning("cal()")
    public void afterMethod(JoinPoint jp) {
        long end = System.currentTimeMillis();
        long total = end - startTime.get();
        String methodName = jp.getSignature().getName();

        //

        Counter featureCounter = meterRegistry.counter(methodName);
        featureCounter.increment();
        Counter featureTicker = meterRegistry.counter(methodName + "_totalms");
        featureTicker.increment(total);
        System.out.println("COUNT: " + featureCounter.count() + " " + featureTicker.count() + "ms");

    }

    @AfterThrowing(value="cal()", throwing="ex")
    public void afterMethod(JoinPoint jp, Exception ex) {
        long end = System.currentTimeMillis();
        long total = end - startTime.get();
        String methodName = jp.getSignature().getName();

        //

        Counter featureCounter = meterRegistry.counter(methodName + "_ERROR");
        featureCounter.increment();
        Counter featureTicker = meterRegistry.counter(methodName + "_ERROR_totalms");
        featureTicker.increment(total);
        System.out.println("ERROR COUNT:" + featureCounter.count() + " " + featureTicker.count() + "ms " + ex.getMessage());

    }

    private boolean urlInList(String url, List<String> urls){
        for (String urlx: urls) {
            if(urlx.endsWith("/*")) {
                int length = urlx.length() - 2;
                if(url.substring(0, length).equals(urlx.substring(0, length))){
                    return true;
                }
            }
            else {
                if(url.equals(urlx)) {
                    return true;
                }
            }
        }
        return false;
    }
}
