package org.example.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * RedisTemplate 常用操作封装。
 * <p>
 * 项目中验证码、缓存、列表、Hash、Set、ZSet 等操作都可以通过这个工具类调用。
 */
@Component
@SuppressWarnings({"unchecked", "rawtypes"})
public class RedisUtils {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取 key 的剩余过期时间，单位秒。
     */
    public <K> long getExpireTime(K key) {
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire == null ? -2 : expire;
    }

    /**
     * 给 key 设置过期时间，单位秒。
     */
    public <K> void setExpireTime(K key, long expireTime) {
        if (expireTime > 0) {
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
        }
    }

    /**
     * 移除 key 的过期时间，让它变成永久有效。
     */
    public <K> void removeExpireTime(K key) {
        redisTemplate.boundValueOps(key).persist();
    }

    /**
     * 根据 pattern 获取 key 集合，例如 user:*。
     */
    public <K> Set<K> keys(K key) {
        return redisTemplate.keys(key);
    }

    /**
     * 判断 key 是否存在。
     */
    public <K> boolean hasKey(K key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 批量删除 key。
     */
    public <K> void delete(Collection<K> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 设置分布式锁，立即返回结果。
     */
    public <K, V> Boolean setNx(K key, V value, long expire) {
        return redisTemplate.opsForValue().setIfAbsent(key, value, expire, TimeUnit.SECONDS);
    }

    /**
     * 设置分布式锁，在 timeout 毫秒内循环尝试获取锁。
     */
    public <K, V> Boolean setNx(K key, V value, long expire, long timeout) {
        long start = System.currentTimeMillis();
        for (; ; ) {
            if (Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, expire, TimeUnit.SECONDS))) {
                return true;
            }
            if (System.currentTimeMillis() - start > timeout) {
                return false;
            }
        }
    }

    /**
     * 释放分布式锁，只有 value 匹配时才删除，避免误删其他请求持有的锁。
     */
    public <K, V> boolean releaseNx(K key, V value) {
        Object currentValue = redisTemplate.opsForValue().get(key);
        if (currentValue != null && value.equals(currentValue)) {
            return Boolean.TRUE.equals(redisTemplate.opsForValue().getOperations().delete(key));
        }
        return false;
    }

    /**
     * 写入普通字符串或对象缓存。
     */
    public <K, V> void set(K key, V value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 写入普通缓存并设置过期时间，单位秒。
     */
    public <K, V> void set(K key, V value, long time) {
        if (time > 0) {
            redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    /**
     * 对数值缓存做递增。
     */
    public Long incrBy(String key, long number) {
        return redisTemplate.opsForValue().increment(key, number);
    }

    /**
     * 对数值缓存做递减。
     */
    public Long decrBy(String key, long number) {
        return redisTemplate.opsForValue().decrement(key, number);
    }

    /**
     * 根据 key 获取普通缓存。
     */
    public <K, V> V get(K key) {
        BoundValueOperations<K, V> boundValueOperations = redisTemplate.boundValueOps(key);
        return boundValueOperations.get();
    }

    /**
     * 从列表右侧插入一个元素。
     */
    public <K, V> void listRightPush(K key, V value) {
        ListOperations<K, V> listOperations = redisTemplate.opsForList();
        listOperations.rightPush(key, value);
    }

    /**
     * 从列表左侧插入一个元素。
     */
    public <K, V> void listLeftPush(K key, V value) {
        ListOperations<K, V> listOperations = redisTemplate.opsForList();
        listOperations.leftPush(key, value);
    }

    /**
     * 从列表右侧批量插入元素。
     */
    public <K, V> void listRightPushAll(K key, List<V> value) {
        redisTemplate.opsForList().rightPushAll(key, value);
    }

    /**
     * 从列表左侧批量插入元素。
     */
    public <K, V> void listLeftPushAll(K key, List<V> value) {
        redisTemplate.opsForList().leftPushAll(key, value);
    }

    /**
     * 根据下标读取列表元素，下标可以为负数。
     */
    public <K, V> V listGetWithIndex(K key, long index) {
        ListOperations<K, V> listOperations = redisTemplate.opsForList();
        return listOperations.index(key, index);
    }

    /**
     * 从列表左侧弹出一个元素。
     */
    public <K, V> V listLeftPop(K key) {
        ListOperations<K, V> listOperations = redisTemplate.opsForList();
        return listOperations.leftPop(key);
    }

    /**
     * 在指定时间内等待并从列表左侧弹出一个元素。
     */
    public <K, V> V listLeftPop(K key, long timeout, TimeUnit unit) {
        ListOperations<K, V> listOperations = redisTemplate.opsForList();
        return listOperations.leftPop(key, timeout, unit);
    }

    /**
     * 从列表右侧弹出一个元素。
     */
    public <K, V> V listRightPop(K key) {
        ListOperations<K, V> listOperations = redisTemplate.opsForList();
        return listOperations.rightPop(key);
    }

    /**
     * 在指定时间内等待并从列表右侧弹出一个元素。
     */
    public <K, V> V listRightPop(K key, long timeout, TimeUnit unit) {
        ListOperations<K, V> listOperations = redisTemplate.opsForList();
        return listOperations.rightPop(key, timeout, unit);
    }

    /**
     * 获取列表指定范围内的元素，end 为 -1 时表示直到末尾。
     */
    public <K, V> List<V> listRange(K key, long start, long end) {
        ListOperations<K, V> listOperations = redisTemplate.opsForList();
        return listOperations.range(key, start, end);
    }

    /**
     * 获取列表长度。
     */
    public <K> long listSize(K key) {
        Long size = redisTemplate.opsForList().size(key);
        return Objects.requireNonNullElse(size, 0L);
    }

    /**
     * 根据下标修改列表元素。
     */
    public <K, V> void listSet(K key, long index, V value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    /**
     * 从列表中移除指定数量的元素。
     */
    public <K, V> long listRemove(K key, long count, V value) {
        Long removed = redisTemplate.opsForList().remove(key, count, value);
        return removed == null ? 0 : removed;
    }

    /**
     * 根据 hash key 和字段读取值。
     */
    public <K, HK, HV> HV hashGet(K key, String item) {
        HashOperations<K, HK, HV> hashOperations = redisTemplate.opsForHash();
        return hashOperations.get(key, item);
    }

    /**
     * 读取 hash 下的所有键值。
     */
    public <K, HK, HV> Map<HK, HV> hashMGet(K key) {
        HashOperations<K, HK, HV> hashOperations = redisTemplate.opsForHash();
        return hashOperations.entries(key);
    }

    /**
     * 批量写入 hash。
     */
    public <K> void hashMSet(K key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /**
     * 批量写入 hash 并设置过期时间，单位秒。
     */
    public <K> void hashMSet(K key, Map<String, Object> map, long expireTime) {
        redisTemplate.opsForHash().putAll(key, map);
        if (expireTime > 0) {
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
        }
    }

    /**
     * 向 hash 写入单个字段。
     */
    public <K, HK, HV> void hashPut(K key, HK hKey, HV value) {
        HashOperations<K, HK, HV> hashOperations = redisTemplate.opsForHash();
        hashOperations.put(key, hKey, value);
    }

    /**
     * 向 hash 写入单个字段并设置过期时间，单位秒。
     */
    public <K, HK, HV> void hashPut(K key, HK hKey, HV value, long expireTime) {
        redisTemplate.opsForHash().put(key, hKey, value);
        if (expireTime > 0) {
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
        }
    }

    /**
     * 判断 hash 中是否存在指定字段。
     */
    public <K, HK, HV> boolean hashHasKey(K key, HK hKey) {
        HashOperations<K, HK, HV> hashOperations = redisTemplate.opsForHash();
        return hashOperations.hasKey(key, hKey);
    }

    /**
     * 读取 hash 中所有 value。
     */
    public <K, HK, HV> List<HV> hashValues(K key) {
        HashOperations<K, HK, HV> hashOperations = redisTemplate.opsForHash();
        return hashOperations.values(key);
    }

    /**
     * 读取 hash 中所有字段名。
     */
    public <K, HK, HV> Set<HK> hashHKeys(K key) {
        HashOperations<K, HK, HV> hashOperations = redisTemplate.opsForHash();
        return hashOperations.keys(key);
    }

    /**
     * 删除 hash 中的字段并返回删除数量。
     */
    public <K, HK, HV> Long hashDelete(K key, Object... hashKeys) {
        HashOperations<K, HK, HV> hashOperations = redisTemplate.opsForHash();
        return hashOperations.delete(key, hashKeys);
    }

    /**
     * 向 set 添加元素。
     */
    public <K, V> void setAdd(K key, V... values) {
        redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 向 set 添加元素并设置过期时间，单位秒。
     */
    public <K, V> void setAdd(K key, long expireTime, V... values) {
        redisTemplate.opsForSet().add(key, values);
        if (expireTime > 0) {
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
        }
    }

    /**
     * 获取 set 元素数量。
     */
    public <K> long setSize(K key) {
        Long size = redisTemplate.opsForSet().size(key);
        return size == null ? 0 : size;
    }

    /**
     * 获取 set 中的所有元素。
     */
    public <K, V> Set<V> setValues(K key) {
        SetOperations<K, V> setOperations = redisTemplate.opsForSet();
        return setOperations.members(key);
    }

    /**
     * 判断 set 中是否存在指定元素。
     */
    public <K, V> boolean setHasKey(K key, V value) {
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
    }

    /**
     * 从 set 中删除元素并返回删除数量。
     */
    public <K, V> Long setDelete(K key, Object... value) {
        SetOperations<K, V> setOperations = redisTemplate.opsForSet();
        return setOperations.remove(key, value);
    }

    /**
     * 向 zset 添加元素和分数。
     */
    public <K, V> void zSetAdd(K key, V value, long score) {
        ZSetOperations<K, V> zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.add(key, value, score);
    }

    /**
     * 按排名范围获取 zset 元素。
     */
    public <K, V> Set<V> zSetValuesRange(K key, long score1, long score2) {
        ZSetOperations<K, V> zSetOperations = redisTemplate.opsForZSet();
        return zSetOperations.range(key, score1, score2);
    }

    /**
     * 根据 value 删除 zset 元素并返回删除数量。
     */
    public <K, V> Long zSetDeleteByValue(K key, Object... value) {
        ZSetOperations<K, V> zSetOperations = redisTemplate.opsForZSet();
        return zSetOperations.remove(key, value);
    }

    /**
     * 根据排名范围删除 zset 元素并返回删除数量。
     */
    public <K, V> Long zSetDeleteRange(K key, long size1, long size2) {
        ZSetOperations<K, V> zSetOperations = redisTemplate.opsForZSet();
        return zSetOperations.removeRange(key, size1, size2);
    }

    /**
     * 根据分数范围删除 zset 元素并返回删除数量。
     */
    public <K, V> Long zSetDeleteByScore(K key, long score1, long score2) {
        ZSetOperations<K, V> zSetOperations = redisTemplate.opsForZSet();
        return zSetOperations.removeRangeByScore(key, score1, score2);
    }
}
