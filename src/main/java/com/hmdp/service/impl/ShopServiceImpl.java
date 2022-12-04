package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryById(Long id) {
        // 缓存穿透
        // Shop shop = queryWithPassThrough(id);

        // 互斥锁解决缓存击穿
        Shop shop = queryWithMutex(id);

        if (shop==null) {
            return Result.fail("店铺不存在！");
        }
        // 返回
        return Result.ok(shop);
    }

    /**
     * 互斥锁解决缓存击穿
     * @param id
     * @return
     */
    private Shop queryWithMutex(Long id) {
        // 1.从查询Redis中是否有数据
        String shopJson = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_KEY + id);
        // 2.判断是否存在
        if (StrUtil.isNotBlank(shopJson)) {
            // 存在则直接返回
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }

        // 3.判断命中的是否是空值
        if(shopJson != null){
            return null;
        }

        // 4.实现缓存重建
        String key = RedisConstants.LOCK_SHOP_KEY+id;
        Shop shop = null;
        try {
            // 4.1 获取互斥锁
            boolean isLock = tryLock(key);
            // 4.2判断是否获取成功
            if(!isLock){
                // 4.3失败，则休眠并重试
                Thread.sleep(50);
                // 注意：获取锁的同时应该再次检测redis缓存是否存在，做DoubleCheck,如果存在则无需重建缓存
                return queryWithMutex(id);
            }
            // 4.4成功，根据id查询数据库
            shop = getById(id);

            // 模拟重建时的延时
            Thread.sleep(200);

            // 5.不存在，返回错误
            if(shop==null){
                // 将空值写入到redis
                stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id,"",RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            // 6.存在就加入到Redis,并返回
            stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY+id, JSONUtil.toJsonStr(shop),RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            // 7.释放互斥锁
            unlock(key);
        }
        return shop;
    }

    private Shop queryWithPassThrough(Long id) {
        // 1.从查询Redis中是否有数据
        String shopJson = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_KEY + id);
        // 2.如果有则直接返回
        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }

        // 判断命中的是否是空值 (上面已经判断过不为空的情况了，下面只有 “” 和 null的两种情况，为null说明不存在，为“”说明空缓存)
        if(shopJson != null){
            return null;
        }

        // 3.如果没有，就去查数据库
        Shop shop = this.baseMapper.selectById(id);
        // 4.如果没找到则返回错误信息
        if(shop==null){
            stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY + id,"",RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }

        // 5.如果查到了就加入到Redis,并返回
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY+id, JSONUtil.toJsonStr(shop),RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return shop;
    }

    /**
     * 获取锁：使用setnx模拟互斥锁
     * 为了防止出现死锁，所以应该为其设置过期时间
     * @param key
     * @return
     */
    private boolean tryLock(String key){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", RedisConstants.LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁
     * @param key
     */
    private void unlock(String key){
        stringRedisTemplate.delete(key);
    }

    @Transactional
    @Override
    public Result update(Shop shop) {
        Long id = shop.getId();
        if(id == null){
            return Result.fail("id不能为null");
        }
        this.updateById(shop);
        // 删除缓存
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY+id);
        return Result.ok();
    }
}
