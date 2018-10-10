package com.wdhis.common.handler;

import com.wdhis.common.exception.BizException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ALAN on 2018/9/13.
 *
 * 统一错误处理Controller类，用于controller抛出异常后的处理
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    //业务报错
    @ExceptionHandler(value = BizException.class)
    @ResponseBody
    public Map<String, String> bizErrorHandler(HttpServletResponse resp, BizException e) throws Exception {
        //String errorInfo = Base64Util.encodeData(e.getMessage());

//        String errorInfo = URLEncoder.encode(e.getMessage(), "UTF-8");
//        resp.setHeader("BizErrorMsg", errorInfo);

        //req.setAttribute("javax.servlet.error.status_code", 520);
        resp.setStatus(520);

        Map<String, String> map = new HashMap<>();

        map.put("code", e.getExCode());
        map.put("info", e.getExInfo());
        map.put("message", e.getErrorMessage());
        map.put("stacktrace", e.getErrorStackTrace());

        return map;
    }

}
