package com.hmdp;

import com.hmdp.config.ResourceConfig;
import com.hmdp.service.IShopService;
import com.hmdp.utils.SendSmsUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private ResourceConfig config;

    @Resource
    IShopService shopService;

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

    /**
     * 预热
     */
    @Test
    public void testSave2Redis() throws InterruptedException {
        shopService.saveShop2Redis(1L,30L);
    }


}
