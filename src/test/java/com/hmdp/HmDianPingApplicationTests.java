package com.hmdp;

import com.hmdp.config.ResourceConfig;
import com.hmdp.utils.SendSmsUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private ResourceConfig config;

    @Test
    public void testSendSms(){
        String[] phone = new String[1];
        String[] templateParam = new String[2];
        phone[0] = "18625983574";
        templateParam[0] = "123456";
        templateParam[1] = "5";
        SendSmsUtil.sendSms(phone,templateParam,config);
    }

    @Test
    public void getResourceConfig(){
        System.out.println(config.getSmsSecretId());
        System.out.println(config.getSmsSecretKey());
        System.out.println(config.getSmsSdkAppId());
        System.out.println(config.getSmsSignName());
        System.out.println(config.getSmsTemplateId());
    }


}
