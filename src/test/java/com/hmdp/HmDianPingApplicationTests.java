package com.hmdp;

import cn.hutool.json.JSONUtil;
import com.hmdp.config.ResourceConfig;
import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.SendSmsUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private ResourceConfig config;

    @Resource
    IShopService shopService;

    @Resource
    CacheClient cacheClient;

    @Resource
    StringRedisTemplate stringRedisTemplate;

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

    @Test
    public void testCachePreHotWithMutex(){
        Shop shop = shopService.getById(1);
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY+1, JSONUtil.toJsonStr(shop),20,TimeUnit.SECONDS);
    }

    @Test
    public void testCachePreHotWithLogicalExpire(){
        cacheClient.setWithLogicalExpire(RedisConstants.CACHE_SHOP_KEY+1,shopService.getById(1),20L,TimeUnit.SECONDS);
    }


}
