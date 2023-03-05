package com.seven.springcloud.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seven.springcloud.dao.UserAddressMapper;
import com.seven.springcloud.service.UserAddressService;
import com.seven.springcloud.entities.Address;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@Slf4j
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, Address> implements UserAddressService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserAddressMapper userAddressMapper;
    @Resource
    private RedissonClient redisson;

    //创建线程池
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Override
    @CachePut(value = {"addressCache"})
    public String searchAddress(String name) {

        String place = stringRedisTemplate.opsForValue().get(name);

        if (StringUtils.isEmpty(place)){
            log.info("查数据库");
            QueryWrapper<Address> wrapper = new QueryWrapper<>();
            wrapper.eq("village",name);
            Address address = userAddressMapper.selectOne(wrapper);
            place = address.getLongKey();

            stringRedisTemplate.opsForValue().set(name,place);
        }else {
            log.info("查缓存");
        }
        return place;
    }


    @Override
    //任务串行
    public Boolean searchAllUpdate() throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(()->{
            //任务1：查出所有地址
            log.info("开启任务1");
            List<Address> addressList = userAddressMapper.selectList(new QueryWrapper<>());
            Map<String, List<Address>> map = new HashMap<>();
            map.put("addressList",addressList);
            return map;
        },executor).thenApplyAsync(res->{
            int count = 0;
            log.info("开启任务2");
            List<Address> list = res.get("addressList");
            //任务2：修改地址详情空值，并计数
            for(Address address:list){
                if(address.getLongKey()==null){
                    address.setLongKey(address.getCounty()+"-"+address.getVillage());
                    userAddressMapper.updateById(address);
                    count++;
                }
            }
            return count;
        },executor);


        CompletableFuture<Integer> future02 = CompletableFuture.supplyAsync(()->{
            log.info("开启任务3");
            return userAddressMapper.selectList(new QueryWrapper<>()).size();
        },executor);

        //两任务组合，同时完成后进行
        CompletableFuture<Boolean> future = future01.thenCombineAsync(future02, Integer::equals,executor);
        //返回计数结果
        return future.get();
    }


    //使用spring cache整合redis进行缓存
    //sync，调用redisCacheManage中的加锁（本地锁）的get方法，去查缓存
    @Cacheable(value={"addressCache"},key="#root.methodName",sync = true)
    @Override
    public Map<String, List<Address>> searchAllNew() {
        Map<String,List<Address>> map = new HashMap<>();
        List<Address> addressList = userAddressMapper.selectList(new QueryWrapper<>());
        log.info("查数据库");
        map.put("address",addressList);
        return map;
    }

    public Map<String,Address> searchByDb(int id){

        Map<String,Address> map = new HashMap<>();
        RLock lock = redisson.getFairLock("myLock");       //获取锁
        try {
            lock.lock();    //上锁
            synchronized (this){
                if(StringUtils.isEmpty(stringRedisTemplate.opsForValue().get("addressList"))){
                    log.info("查数据库");
                    Address address = userAddressMapper.selectById(id);
                    map.put("placeList",address);
                }else {
                    log.info("缓存击中！");
                    return JSON.parseObject(stringRedisTemplate.opsForValue().get("addressList"),new TypeReference<Map<String,List<Address>>>(){});
                }
            }
        }catch (Exception e){
            log.warn("系统错误，稍后重试");
        }
        finally {
            lock.unlock();    //删除锁
        }

        return map;
    }

    //布隆过滤器添加数据
    @Override
    public int bloomAdd(Address address) {
        //拼接全称
        address.setLongKey(address.getCounty()+"-"+address.getVillage());
        //数据库中插入数据
        int result = userAddressMapper.insert(address);
        //插入成功，存入布隆过滤器中
        if(result > 0){
            //获取布隆过滤器
            RBloomFilter<Object> bloomFilter = redisson.getBloomFilter("idList");
            //初始化布隆过滤器(数据量，误差率)
            bloomFilter.tryInit(1000000L,0.02);
            //往过滤器中加入数据
            bloomFilter.add(address.getId());
        }
        return result;
    }

    @Override
    public int bloomAdd() {
        List<Address> list = userAddressMapper.selectList(new QueryWrapper<Address>());
        for(Address a:list){
            //获取布隆过滤器
            RBloomFilter<Object> bloomFilter = redisson.getBloomFilter("idList");
            //初始化布隆过滤器(数据量，误差率)
            bloomFilter.tryInit(1000000L,0.02);
            //往过滤器中加入数据
            bloomFilter.add(a.getId());
        }
        return 0;
    }

    //布隆过滤器添加数据
    @Override
    public Map<String,Address> bloomFilter(int id) {

        //获取布隆过滤器
        RBloomFilter<Object> bloomFilter = redisson.getBloomFilter("idList");
        //判断数据是否在过滤器中
        boolean flag = bloomFilter.contains(id);
        if(flag){
            String addressList = stringRedisTemplate.opsForValue().get("address:"+id);
            //判断缓存中是否存在
            if (StringUtils.isEmpty(addressList)){
                log.info("缓存未命中");
                //调用查询数据库的方法
                Map<String,Address> map = searchByDb(id);
                //封装查询结果
                String result = JSON.toJSONString(map);
                //以json格式存入redis中
                stringRedisTemplate.opsForValue().set("address:"+id,result);
                return map;
            }else {
                log.info("缓存命中");
                return JSON.parseObject(addressList,new TypeReference<Map<String,Address>>(){});
            }
        }else {
            //缓存不存在且过滤器不命中，过滤请求
            log.info("请求被过滤");
            return null;
        }
    }

}
