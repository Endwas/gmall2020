package com.endwas.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.endwas.gmall.bean.OmsCartItem;
import com.endwas.gmall.cart.mapper.OmsCartItemMapper;
import com.endwas.gmall.service.CartService;
import com.endwas.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    OmsCartItemMapper omsCartItemMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public OmsCartItem getCartFromDb(String memberId, String skuId) {

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        OmsCartItem omsCartItem1 = omsCartItemMapper.selectOne(omsCartItem);

        return omsCartItem1;
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {
        omsCartItemMapper.insertSelective(omsCartItem);
    }

    @Override
    public void updateCart(OmsCartItem omsCartItemFromDb) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId", omsCartItemFromDb.getMemberId()).andEqualTo("productSkuId", omsCartItemFromDb.getProductSkuId());

        omsCartItemMapper.updateByExample(omsCartItemFromDb, example);
    }

    @Override
    public void flushCartCache(String memberId) {

        //查询所有的购物车信息
        OmsCartItem omsCartItem =new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        List<OmsCartItem> omsCartItems = omsCartItemMapper.select(omsCartItem);
        String userCartKey = "user:"+memberId+":cart";
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            jedis.del(userCartKey);
            //保存到缓存中去
            for (OmsCartItem cartItem : omsCartItems) {
                cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
                String cartJson = JSON.toJSONString(cartItem);
                jedis.hset(userCartKey, cartItem.getProductSkuId(), cartJson);
            }
        }finally {
            jedis.close();
        }

    }

    @Override
    public List<OmsCartItem> getUserCart(String memberId) {
        String userCartKey = "user:"+memberId+":cart";
        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        Jedis jedis = null;
        try{
            jedis = redisUtil.getJedis();
            List<String> hvals = jedis.hvals(userCartKey);
            if (hvals.isEmpty()){
                OmsCartItem omsCartItem =new OmsCartItem();
                omsCartItem.setMemberId(memberId);
                omsCartItemList = omsCartItemMapper.select(omsCartItem);
                for (OmsCartItem cartItem : omsCartItemList) {
                    cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
                }
            } else {
                for (String hval : hvals) {
                    OmsCartItem omsCartItem = JSON.parseObject(hval, OmsCartItem.class);
                    omsCartItemList.add(omsCartItem);
                }

            }
        }catch (Exception e){
            // 处理异常，记录系统日志
            e.printStackTrace();
            //String message = e.getMessage();
            //logService.addErrLog(message);
            return null;
        } finally {
            jedis.close();
        }

        return omsCartItemList;
    }

    @Override
    public void checkCart(String isChecked, String skuId, String memberId) {
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setMemberId(memberId);
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setIsChecked(isChecked);

        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId", memberId).andEqualTo("productSkuId", skuId);
        omsCartItemMapper.updateByExampleSelective(omsCartItem, example);
        // 缓存同步
        flushCartCache(memberId);
    }

}
