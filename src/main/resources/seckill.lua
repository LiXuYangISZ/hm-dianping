---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by 物竞天择适者生存.
--- DateTime: 2023/2/6 17:02
---
-- 1.参数列表
-- 1.1 优惠券Id
local voucherId = ARGV[1]
-- 1.2 用户id
local userId = ARGV[2]
-- 1.3 订单id
local orderId = ARGV[3]

-- 2.数据key
-- 2.1 库存key
local stockKey = 'seckill:stock:' .. voucherId
-- 2.2 订单key
local orderKey = 'seckill:order:' .. voucherId

-- 3.脚本业务
-- 3.1 判断库存是否充足 get stockKey
if (tonumber(redis.call('get', stockKey)) <= 0) then
    -- 3.1.2 库存不足,返回1
    return 1
end

-- 3.2 判断用户是否已经下过单
if (redis.call('sismember', orderKey, userId) == 1) then
    -- 3.2.2 存在，说明重复下单，返回2
    return 2
end

-- 3.3 扣库存 incrby stockKey -1
redis.call('incrby', stockKey, -1)

-- 3.4 下单(保存用户) sadd orderKey userId
redis.call('sadd', orderKey, userId)

-- 3.5 发送消息到队列中，XADD stream.orders * k1 v1 k2 v2 ...
-- 注意：在赋值时，名称为id封装时效果会更好哦，因为VoucherOrder中的id对应着orderId
redis.call('xadd','stream.orders','*','userId',userId,'voucherId',voucherId,'id',orderId)

return 0
