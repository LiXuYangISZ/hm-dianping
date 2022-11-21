package com.hmdp;

import com.hmdp.utils.SendSmsUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;

class HmDianPingApplicationTests {

    @Test
    public void testSendSms(){
        String[] phone = new String[1];
        String[] templateParam = new String[2];
        phone[0] = "18625983574";
        templateParam[0] = "123456";
        templateParam[1] = "5";
        SendSmsUtil.sendSms(phone,templateParam);
    }
}
