package com.dmswide.nowcoder.service.impl;

import com.dmswide.nowcoder.service.StatisticService;
import com.dmswide.nowcoder.util.RedisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class StatisticServiceImpl implements StatisticService {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

    //指定的ip存入UV
    @Override
    public void recordUV(String ip) {
        String redisKey = RedisKeyUtil.getUVKey(simpleDateFormat.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey,ip);
    }

    @Override
    public long calculateUV(Date start, Date end) {
        if(start == null || end == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        //[start,end]之内的全部key存到list中
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getUVKey(simpleDateFormat.format(calendar.getTime()));
            keyList.add(key);
            calendar.add(Calendar.DATE,1);
        }
        //生成合并数据的key来准备接收合并后的数据
        String redisKey = RedisKeyUtil.getUVKey(simpleDateFormat.format(start), simpleDateFormat.format(end));
        redisTemplate.opsForHyperLogLog().union(redisKey,keyList.toArray(new String[0]));

        return redisTemplate.opsForHyperLogLog().size(redisKey);
    }

    @Override
    public void recordDAU(Integer userId) {
        String redisKey = RedisKeyUtil.getDAUKey(simpleDateFormat.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey,userId,true);
    }

    @Override
    public Long calculateDAU(Date start, Date end) {
        if(start == null || end == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        //整理[start,end]范围内的日期的key
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while(!calendar.getTime().after(end)){
            String key = RedisKeyUtil.getDAUKey(simpleDateFormat.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE,1);
        }
        //所有的值进行or操作
         return redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDAUKey(simpleDateFormat.format(start), simpleDateFormat.format(end));
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,redisKey.getBytes(),keyList.toArray(new byte[0][0]));
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });
    }
}
