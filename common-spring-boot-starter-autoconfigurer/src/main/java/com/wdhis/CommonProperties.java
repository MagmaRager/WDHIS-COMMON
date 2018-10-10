package com.wdhis;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by ALAN on 2018/10/9.
 */
@ConfigurationProperties(prefix = "wdhis.common")
public class CommonProperties {
    private String prefix;
    private String suffix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
