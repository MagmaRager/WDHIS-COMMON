package com.wdhis.common.controller;


import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ALAN on 2018/10/12.
 */
@Api(description = "公共数据")
@Controller
public class CommonController {

    @Autowired
    MeterRegistry meterRegistry;

    @ApiOperation(value = "监控信息", httpMethod = "GET", produces = "application/json")
    @ApiResponse(code = 200, message = "success", response = String.class, responseContainer = "List")
    @RequestMapping("/monitor")
    @ResponseBody
    public List<String> monitor() {
        List<Meter> list = meterRegistry.getMeters();
        List<String> infoList = new ArrayList<>();
        for (Meter m : list) {
            String hd = m.getId().getName();
            System.out.println(hd);
            if(hd.endsWith("_count")) {
                String hd1 = hd.substring(0, hd.length() - 6);
                if(hd1.endsWith("_ERROR")) {
                    String hd2 = hd.substring(0, hd1.length() - 6);
                    infoList.add(hd2 + " -ERROR : " + meterRegistry.counter(hd).count() + " times.");
                }
                else {
                    infoList.add(hd1 + " : " + meterRegistry.counter(hd).count() + " times.");
                }
            }
            else if(hd.endsWith("_totalms")) {
                String hd1 = hd.substring(0, hd.length() - 8);
                if(hd1.endsWith("_ERROR")) {
                    String hd2 = hd.substring(0, hd1.length() - 6);
                    infoList.add(hd2 + " -ERROR : " + meterRegistry.counter(hd).count() + "ms totally.");
                }
                else {
                    infoList.add(hd1 + " : " + meterRegistry.counter(hd).count() + "ms totally.");
                }
            }
            else if(hd.endsWith("_slowReqCount")) {
                String hd1 = hd.substring(1, hd.length() - 12);
                infoList.add(hd + " : " + meterRegistry.counter(hd).count() + "times as slow request.");
            }

        }
        return infoList;
    }

//    @ApiOperation(value = "监控信息i", httpMethod = "GET", produces = "application/json")
//    @ApiResponse(code = 200, message = "success", response = Meter.class, responseContainer = "List")
//    @RequestMapping("/monitori")
//    @ResponseBody
//    public List<Meter> monitori() {
//        List<Meter> list = meterRegistry.getMeters();
//        return list;
//    }
}
