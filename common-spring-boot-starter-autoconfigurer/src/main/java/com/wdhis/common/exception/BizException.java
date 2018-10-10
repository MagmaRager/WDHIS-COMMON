package com.wdhis.common.exception;

/**
 * Created by ALAN on 2018/9/13.
 *
 * 业务异常
 */
public class BizException extends RuntimeException {

    private String exCode;

    private String exInfo;

    private Exception innerException;

    public String getExCode() {
        return exCode;
    }

    public String getExInfo() {
        return exInfo;
    }

    public Exception getInnerException() {
        return innerException;
    }

    public String getErrorMessage() {
        return innerException.getMessage();
    }

    public String getErrorStackTrace() {
        StringBuilder sb = new StringBuilder();
        for(StackTraceElement elem : innerException.getStackTrace()) {
            sb.append(elem + "\n");
        }
        return sb.toString();
    }

    public BizException(String code, String info) {
        this(code, info, new Exception());
    }

    public BizException(String code, String info, Exception ex) {
        exCode = code;
        exInfo = info;
        innerException = ex;

    }
}