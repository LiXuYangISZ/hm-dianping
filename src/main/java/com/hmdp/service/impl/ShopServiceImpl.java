package com.hmdp.service.impl;

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

import javax.annotation.Resource;

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
        // 1.从查询Redis中是否有数据
        String shopJson = stringRedisTemplate.opsForValue().get(RedisConstants.CACHE_SHOP_KEY + id);
        // 2.如果有则直接返回
        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }

        // 3.如果没有，就去查数据库
        Shop shop = this.baseMapper.selectById(id);
        // 4.如果没找到则返回错误信息
        if(shop==null){
            return Result.fail("店铺不存在~");
        }

        // 5.如果查到了就加入到Redis,并返回
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY+id, JSONUtil.toJsonStr(shop));
        return Result.ok(shop);
    }
}
