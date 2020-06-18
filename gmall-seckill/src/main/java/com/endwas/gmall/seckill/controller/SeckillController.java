package com.endwas.gmall.seckill.controller;

import com.endwas.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

@Controller
public class SeckillController {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    RedissonClient redissonClient;

    @RequestMapping("secKill")
    @ResponseBody
    public String secKill(){
        // 分布锁无法解决高并发数据安全的问题，他只能保证在高请求的情况下安全的问题 要使用watch
        Jedis jedis =null;
        try{
            jedis = redisUtil.getJedis();
            String OK = jedis.set("Lock", "Lock", "nx", "px",10*1000);
            if (StringUtils.isNotBlank(OK) && OK.equals("OK")){
                String stock = jedis.get("stock");
                if (Integer.parseInt(stock) > 0){
                    jedis.incrBy("stock",-1);
                    System.out.println("当前抢购的数量为：" + stock);
                }
            }
        }finally {
            jedis.del("Lock");
            jedis.close();
        }

        return "endwasSecKill";
    }

    @RequestMapping("secWatch")
    @ResponseBody
    public String secWatch(){
        Jedis jedis = null;
        try{
            jedis = redisUtil.getJedis();
            jedis.watch("stock");
            String stock = jedis.get("stock");
            Transaction transaction = jedis.multi();
            if (Integer.parseInt(stock) > 0) {

                transaction.incrBy("stock", -1);
                List<Object> exec = transaction.exec();
                if (exec != null && exec.size() > 0){
                    System.out.println("当前抢购剩余数量为:"+stock);
                    // 用消息队列发出订单消息
                }
            }
        }finally {
            jedis.close();
        }



        return "null";
    }


    @RequestMapping("kill")
    @ResponseBody
    public String kill(){
        Jedis jedis = null;
        try{
            jedis = redisUtil.getJedis();
            RSemaphore stock = redissonClient.getSemaphore("stock");
            boolean b = stock.tryAcquire();
            String goods = jedis.get("stock");
            if (b){
                System.out.println("当前抢购剩余数量为:"+goods);
            }
        } finally {
            jedis.close();
        }

        return "happy";
    }

}
