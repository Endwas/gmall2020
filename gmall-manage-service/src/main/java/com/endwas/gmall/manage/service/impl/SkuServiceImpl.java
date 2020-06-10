package com.endwas.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.endwas.gmall.bean.PmsSkuAttrValue;
import com.endwas.gmall.bean.PmsSkuImage;
import com.endwas.gmall.bean.PmsSkuInfo;
import com.endwas.gmall.bean.PmsSkuSaleAttrValue;
import com.endwas.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.endwas.gmall.manage.mapper.PmsSkuImageMapper;
import com.endwas.gmall.manage.mapper.PmsSkuInfoMapper;
import com.endwas.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.endwas.gmall.service.SkuService;
import com.endwas.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;

    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;

    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;

    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {
        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());
        pmsSkuInfoMapper.insertSelective(pmsSkuInfo);

        List<PmsSkuImage> skuImageLists = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage skuImageList : skuImageLists) {
            skuImageList.setSkuId(pmsSkuInfo.getId());
            pmsSkuImageMapper.insertSelective(skuImageList);
        }

        List<PmsSkuAttrValue> skuAttrValueLists = pmsSkuInfo.getSkuAttrValueList();
        for (PmsSkuAttrValue skuAttrValue : skuAttrValueLists) {
            skuAttrValue.setSkuId(pmsSkuInfo.getId());
            pmsSkuAttrValueMapper.insertSelective(skuAttrValue);
        }


        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
            pmsSkuSaleAttrValue.setSkuId(pmsSkuInfo.getId());
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);

        }


        return "success";

    }

    @Override
    public PmsSkuInfo getSkuById(String skuId) {
        // 从缓存中查找
        String skuRedisKey = "Sku:"+skuId+":Info";
        String skuRedisLockKey = "Sku:"+skuId+":Lock";
        Jedis jedis = redisUtil.getJedis();
        PmsSkuInfo pmsSkuInfo;

        try{
            // 查询缓存中该sku数据
            String skuJson = jedis.get(skuRedisKey);
            if (StringUtils.isNotBlank(skuJson)){
                //查到了就将数据转回PmsSkuInfo返回
                 pmsSkuInfo =  JSON.parseObject(skuJson, PmsSkuInfo.class);
            }
            else {
                String token = UUID.randomUUID().toString();
                //没查到就去访问数据库，设置锁超时时间10秒钟
                String OK = jedis.set(skuRedisLockKey, token, "nx", "px",   10*1000);
                if (StringUtils.isNotBlank(OK) && OK.equals("OK")){
                    //调用查数据库
                    pmsSkuInfo = getSkuByIdFromDb(skuId);
                    if (pmsSkuInfo != null){
                        //数据库能查到数据，保存到缓存同时返回

                        jedis.set(skuRedisKey, JSON.toJSONString(pmsSkuInfo));


                    } else{
                        //数据库也无法找到的sku即不存在 为防止缓存穿透，给缓存中保存一个空值返回
                        jedis.setex(skuRedisKey, 60*3, JSON.toJSONString(""));
                    }
                    //进行线程校验，避免当前线程执行超时，锁被释放，被另外线程加锁后，该线程把另外线程的锁给删除了，所以要判断当前的锁是否是自己加的。
                    String lockToken = jedis.get(skuRedisLockKey);
                    if(StringUtils.isNotBlank(lockToken)&&lockToken.equals(token)) {
                        //jedis.eval("lua");可与用lua脚本，在查询到key的同  时删除该key，防止高并发下的意外的发生，判断和删除↕ 不是原子性也有可能产生高并发问题。
                        jedis.del(skuRedisLockKey);// 用token确认删除的是自己的sku的锁
                    }
                }else {
                    System.out.println("有用户走到了该分支");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return getSkuById(skuId);

                }
            }

            return pmsSkuInfo;
        }

        finally {
//            jedis.del(skuRedisLockKey);
            jedis.close();
        }

    }

    public PmsSkuInfo getSkuByIdFromDb(String skuId) {
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);

        //保证只能返回一个对象，那么使用selectOne 因为用的是主键 或者用selectByPrimaryKey()
        PmsSkuInfo info = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        //给skuInfo 封装上skuImageList
        if (info != null) {
            PmsSkuImage pmsSkuImage = new PmsSkuImage();
            pmsSkuImage.setSkuId(pmsSkuInfo.getId());
            List<PmsSkuImage> pmsSkuImages = pmsSkuImageMapper.select(pmsSkuImage);
            info.setSkuImageList(pmsSkuImages);
        }
        return info;
//        //给skuInfo 封装上skuAttrValueList
//        PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
//        pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
//        List<PmsSkuAttrValue> pmsSkuAttrValues = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
//        info.setSkuAttrValueList(pmsSkuAttrValues);
//
//        //给skuInfo 封装上skuSaleAttrValueList
//        PmsSkuSaleAttrValue pmsSkuSaleAttrValue = new PmsSkuSaleAttrValue();
//        pmsSkuSaleAttrValue.setSkuId(pmsSkuInfo.getId());
//        List<PmsSkuSaleAttrValue> pmsSkuSaleAttrValues = pmsSkuSaleAttrValueMapper.select(pmsSkuSaleAttrValue);
//        info.setSkuSaleAttrValueList(pmsSkuSaleAttrValues);


    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId) {

        List<PmsSkuInfo> pmsSkuInfos = pmsSkuInfoMapper.selectSkuSaleAttrValueListBySpu(productId);

        return pmsSkuInfos;
    }

    @Override
    public List<PmsSkuInfo> getAllSkuInfo() {
        List<PmsSkuInfo> pmsSkuInfoList = pmsSkuInfoMapper.selectAll();

        for (PmsSkuInfo pmsSkuInfo : pmsSkuInfoList) {
            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(pmsSkuInfo.getId());
            List<PmsSkuAttrValue> select = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);
            pmsSkuInfo.setSkuAttrValueList(select);
        }

        return pmsSkuInfoList;


    }


}
