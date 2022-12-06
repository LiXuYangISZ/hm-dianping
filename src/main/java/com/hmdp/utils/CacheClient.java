package com.hmdp.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.entity.Shop;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author lxy
 * @version 1.0
 * @Description Redis操作缓存的工具类
 * @date 2022/12/6 0:33
 */
@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 将任意Java对象序列化为json并存储在string类型的key中，并且可以设置TTL过期时间
     * @param key
     * @param value
     * @param time
     * @param unit
     */
    public void set(String key, Object value, Long time, TimeUnit unit){
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value),time,unit);
    }

    /**
     * 逻辑过期解决缓存击穿
     * @param key
     * @param value
     * @param time
     * @param unit
     */
    public void setWithLogicalExpire(String key,Object value,Long time,TimeUnit unit){
        // 设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        // 写入Redis
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(redisData));
    }

    /**
     * 根据指定的key查询缓存,并利用缓存空值来解决缓存穿透问题
     * @param keyPrefix key前缀
     * @param id
     * @param type
     * @param dbFallback 降级的函数
     * @param time       时间
     * @param unit       单位
     * @param <R>
     * @param <ID>
     * @return
     */
    private <R,ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type,
                                          Function <ID,R> dbFallback,Long time,TimeUnit unit) {
        String key = keyPrefix + id;
        // 1.从Redis中查询R数据
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2.判断是否存在
        if (StrUtil.isNotBlank(json)) {
            // 3.存在,则直接返回
            return JSONUtil.toBean(json, type);
        }

        // 4.判断命中的是否是空值 (上面已经判断过不为空的情况了，下面只有 “” 和 null的两种情况，为null说明不存在，为“”说明空缓存)
        if (json != null) {
            return null;
        }

        // 3.如果没有，就去查数据库
        R r = dbFallback.apply(id);
        // 4.如果没找到则返回错误信息
        if (r == null) {
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }

        // 5.如果查到了就加入到Redis,并返回
        this.set(key,r,time,unit);
        return r;
    }

    /**
     * 获取锁：使用setnx模拟互斥锁
     * 为了防止出现死锁，所以应该为其设置过期时间
     *
     * @param key
     * @return
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", RedisConstants.LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁
     *
     * @param key
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
