package com.endwas.gmall.order.mq;

import com.endwas.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class OrderServiceListener {
    @Autowired
    OrderService orderService;

    @JmsListener(destination = "PAYMENT_SUCCESS_QUEUE" ,containerFactory = "jmsQueueListener")
    public void OrderServiceListener(MapMessage mapMessage) throws JMSException {
        // 更新order
        String out_trade_no = mapMessage.getString("out_trade_no");
        orderService.updateOrder(out_trade_no);


    }

}
