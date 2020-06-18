package com.endwas.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePayRequest;
import com.endwas.gmall.annotations.LoginRequired;
import com.endwas.gmall.bean.OmsOrder;
import com.endwas.gmall.bean.PaymentInfo;
import com.endwas.gmall.payment.config.AlipayConfig;
import com.endwas.gmall.service.OrderService;
import com.endwas.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {
    @Autowired
    PaymentService paymentService;

    @Autowired
    AlipayClient alipayClient;

    @Reference
    OrderService orderService;



    @RequestMapping("index")
    @LoginRequired(loginSuccess = true)
    public String index(ModelMap modelMap ,String outTradeNo, String totalAmount, HttpServletRequest httpServletRequest){
        String memberId = (String) httpServletRequest.getAttribute("memberId");
        String nickname = (String) httpServletRequest.getAttribute("nickname");
        modelMap.put("nickName", nickname);
        modelMap.put("outTradeNo", outTradeNo);
        modelMap.put("totalAmount", totalAmount);
        return "index";
    }
    @RequestMapping("alipay/submit")
    @LoginRequired(loginSuccess = true)
    @ResponseBody
    public String alipay(String outTradeNo, BigDecimal totalAmount, HttpServletRequest httpServletRequest){
        AlipayTradePayRequest alipayTradePayRequest = new AlipayTradePayRequest();
        alipayTradePayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayTradePayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        String form = null;
        Map<String ,String> tradeMap = new HashMap<>();
        tradeMap.put("out_trade_no", outTradeNo);
        tradeMap.put("totalAmount", totalAmount.toString());
        tradeMap.put("product_code", "FAST_INSTANT_TRADE_PAY");
        tradeMap.put("subject", "Gmall商城华为mate10endwas联名特款");
        String trade = JSON.toJSONString(tradeMap);
        alipayTradePayRequest.setBizContent(trade);
        try {
            form = alipayClient.pageExecute(alipayTradePayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        // 生成并且保存用户的支付信息
        OmsOrder omsOrder = orderService.getOrderByOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setOrderSn(outTradeNo);
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("谷粒商城商品一件");
        paymentInfo.setTotalAmount(totalAmount);
        paymentService.savePaymentInfo(paymentInfo);

        paymentService.sendDelayPaymentResultQueue(outTradeNo, 5);
        System.out.println(form);
        return form;
    }

    @RequestMapping("alipay/callback/return")
    @LoginRequired(loginSuccess = true)
    public String aliPayCallBackReturn(HttpServletRequest request, ModelMap modelMap){

        // 回调请求中获取支付宝参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String trade_status = request.getParameter("trade_status");
        String total_amount = request.getParameter("total_amount");
        String subject = request.getParameter("subject");
        String call_back_content = request.getQueryString();


        // 通过支付宝的paramsMap进行签名验证，2.0版本的接口将paramsMap参数去掉了，导致同步请求没法验签
        if(StringUtils.isNotBlank(sign)){
            // 验签成功
            // 更新用户的支付状态

            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOrderSn(out_trade_no);
            paymentInfo.setPaymentStatus("已支付");
            paymentInfo.setAlipayTradeNo(trade_no);// 支付宝的交易凭证号
            paymentInfo.setCallbackContent(call_back_content);//回调请求字符串
            paymentInfo.setCallbackTime(new Date());

            paymentService.updatePayment(paymentInfo);

        }

        return "finish";
    }


}
