package com.endwas.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.endwas.gmall.bean.OmsOrder;
import com.endwas.gmall.bean.OmsOrderItem;
import com.endwas.gmall.mq.ActiveMQUtil;
import com.endwas.gmall.order.mapper.OmsOrderItemMapper;
import com.endwas.gmall.order.mapper.OmsOrderMapper;
import com.endwas.gmall.service.OrderService;
import com.endwas.gmall.util.RedisUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.rmi.activation.Activatable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    ActiveMQUtil activeMQUtil;

    @Autowired
    OmsOrderMapper omsOrderMapper;

    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;

    @Override
    public String genTradeCode(String memberId) {
        Jedis jedis = null;
        String tradeKey = "user:"+memberId+":tradeCode";
        String tradeCode = UUID.randomUUID().toString();

        try {
            jedis = redisUtil.getJedis();
            jedis.setex(tradeKey,60*15, tradeCode);
            return tradeCode;

        }finally {
            jedis.close();
        }
    }

    @Override
    public String checkTradeCode(String memberId, String tradeCode) {
        Jedis jedis = null;
        String tradeKey ="user:"+memberId+":tradeCode";
        try{
            jedis = redisUtil.getJedis();
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            Long eval = (Long) jedis.eval(script, Collections.singletonList(tradeKey), Collections.singletonList(tradeCode));
            if (eval != null && eval != 0){
                jedis.del(tradeKey);
                return "success";
            } else {
                return "fail";
            }


        }finally {
            jedis.close();
        }
    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {
        omsOrderMapper.insertSelective(omsOrder);
        String orderId = omsOrder.getId();
        // 保存订单详情
        List<OmsOrderItem> omsOrderItems = omsOrder.getOmsOrderItems();
        for (OmsOrderItem omsOrderItem : omsOrderItems) {
            omsOrderItem.setOrderId(orderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);
            // 删除购物车数据
            // cartService.delCart();
        }
    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {
        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeNo);
        OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);

        return omsOrder1;
    }

    @Override
    public void updateOrder(String out_trade_no) {
        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn", out_trade_no);
        OmsOrder orderUpdate = new OmsOrder();
        orderUpdate.setStatus("1");
        Connection connection =null;
        Session session = null;
        try{
            ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();
            connection  = connectionFactory.createConnection();
            session = connection.createSession(true, Session.SESSION_TRANSACTED);

        } catch (JMSException e) {
            e.printStackTrace();
        }
        Queue order_pay_queue = null;
        try {
            omsOrderMapper.updateByExampleSelective(orderUpdate, example);

            order_pay_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(order_pay_queue);
//            MapMessage mapMessage = new ActiveMQMapMessage();
            // 查询订单的对象，转化成json字符串，存入ORDER_PAY_QUEUE的消息队列

            TextMessage textMessage=new ActiveMQTextMessage();//字符串文本
            //MapMessage mapMessage = new ActiveMQMapMessage();// hash结构

            OmsOrder omsOrderParam = new OmsOrder();
            omsOrderParam.setOrderSn(out_trade_no);
            OmsOrder omsOrderResponse = omsOrderMapper.selectOne(omsOrderParam);

            OmsOrderItem omsOrderItemParam = new OmsOrderItem();
            omsOrderItemParam.setOrderSn(omsOrderParam.getOrderSn());
            List<OmsOrderItem> select = omsOrderItemMapper.select(omsOrderItemParam);
            omsOrderResponse.setOmsOrderItems(select);
            textMessage.setText(JSON.toJSONString(omsOrderResponse));
            producer.send(textMessage);
            session.commit();
        } catch (Exception e) {
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                session.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }



    }
}
