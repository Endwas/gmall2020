package com.endwas.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.endwas.gmall.annotations.LoginRequired;
import com.endwas.gmall.bean.OmsCartItem;
import com.endwas.gmall.bean.PmsSkuInfo;
import com.endwas.gmall.service.CartService;
import com.endwas.gmall.service.SkuService;
import com.endwas.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {
    @Reference
    CartService cartService;
    
    @Reference
    SkuService skuService;




    @RequestMapping("checkCart")
    @LoginRequired(loginSuccess = false)
    public String checkCart(String isChecked, String skuId, ModelMap modelMap, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        List<OmsCartItem> omsCartItemList;
        String memberId = (String) httpServletRequest.getAttribute("memberId");
        String nickname = (String) httpServletRequest.getAttribute("nickname");
        //判断用户是否登录,未登录就从缓存中取，然后根据用户操作异步ajax修改Cookie后返回
        if (StringUtils.isBlank(memberId)){
            String cartListCookie = CookieUtil.getCookieValue(httpServletRequest, "cartListCookie", true);
            omsCartItemList = JSON.parseArray(cartListCookie, OmsCartItem.class);
            for (OmsCartItem omsCartItem : omsCartItemList) {
                if (omsCartItem.getProductSkuId().equals(skuId)){
                    omsCartItem.setIsChecked(isChecked);
                }
            }
            String cartJson = JSON.toJSONString(omsCartItemList);
            CookieUtil.setCookie(httpServletRequest,httpServletResponse, "cartListCookie",cartJson, 60*60*72, true);
        }else {
            //调用checkCart修改数据库服务
            cartService.checkCart(isChecked, skuId, memberId);
            // 缓存同步
            cartService.flushCartCache(memberId);
            //获得用户最新的购物车(redis中获取)
            omsCartItemList = cartService.getUserCart(memberId);

        }

        for (OmsCartItem cartItem : omsCartItemList) {
            cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
        }
        modelMap.put("totalAmount",getTotalAmout(omsCartItemList));
        modelMap.put("cartList", omsCartItemList);

        return "cartListInner";
    }

    @LoginRequired(loginSuccess = false)
    @RequestMapping("cartList")
    public String cartList(ModelMap modelMap, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        List<OmsCartItem> omsCartItemList;
        String memberId = (String) httpServletRequest.getAttribute("memberId");
        String nickname = (String) httpServletRequest.getAttribute("nickname");
        // 未登录从Cookie中取出购物车数据
        if (StringUtils.isBlank(memberId)){
            String cartListCookie = CookieUtil.getCookieValue(httpServletRequest, "cartListCookie", true);
            omsCartItemList = JSON.parseArray(cartListCookie, OmsCartItem.class);
        } else {
        // 登录从缓存或数据库中取出数据
            omsCartItemList = cartService.getUserCart(memberId);

        }
        for (OmsCartItem cartItem : omsCartItemList) {
            cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
        }
        modelMap.put("totalAmount",getTotalAmout(omsCartItemList));
        modelMap.put("cartList", omsCartItemList);
        return "cartList";
    }



    @RequestMapping("addToCart")
    @LoginRequired(loginSuccess = false)
    public ModelAndView addToCart(String skuId, int quantity, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId);
        // 判断当前用户登录状态，登录就读取数据库/缓存中的数据，没登陆就读取Cookie中的数据
        String memberId = (String) httpServletRequest.getAttribute("memberId");
        String nickname = (String) httpServletRequest.getAttribute("nickname");

        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setProductId(pmsSkuInfo.getProductId());
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setMemberId(memberId);
        omsCartItem.setQuantity(new BigDecimal(quantity));
        omsCartItem.setPrice(pmsSkuInfo.getPrice());
        omsCartItem.setProductPic(pmsSkuInfo.getSkuDefaultImg());
        omsCartItem.setProductName(pmsSkuInfo.getSkuName());
        omsCartItem.setProductSkuCode("2020061000000000");
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setProductCategoryId(pmsSkuInfo.getCatalog3Id());
        omsCartItem.setIsChecked("1");

        //判断当前用户是否登录
        if (StringUtils.isBlank(memberId)){
            //读取Cookie中的数据
            String cartListCookie = CookieUtil.getCookieValue(httpServletRequest, "cartListCookie", true);
            //判断读取的Cookie是否为空，若空直接插入，否则更新Cookie
            if (StringUtils.isBlank(cartListCookie)){
                omsCartItemList.add(omsCartItem);
                String cartJson = JSON.toJSONString(omsCartItemList);
                CookieUtil.setCookie(httpServletRequest,httpServletResponse, "cartListCookie",cartJson, 60*60*72, true);
            } else {
                //将cookie数据取出转为List
                omsCartItemList = JSON.parseArray(cartListCookie, OmsCartItem.class);
                //result用作判断 当前cookie中是否添加该产品，结果为true则在循环中加入该数量，否则在列表加入该产品
                boolean result = isCartExisted(omsCartItemList, omsCartItem);
                if ( !result ){
                    omsCartItemList.add(omsCartItem);
                }
                String cartJson = JSON.toJSONString(omsCartItemList);
                CookieUtil.setCookie(httpServletRequest, httpServletResponse, "cartListCookie", cartJson, 60*60*72, true);
            }
        }else {
            //当前用户已经登陆了
            OmsCartItem omsCartItemFromDb = cartService.getCartFromDb(memberId, skuId);
            if (omsCartItemFromDb != null){
                omsCartItemFromDb.setQuantity(omsCartItemFromDb.getQuantity().add(omsCartItem.getQuantity()));
                cartService.updateCart(omsCartItemFromDb);
            } else{
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname(nickname);
                cartService.addCart(omsCartItem);
            }
            cartService.flushCartCache(memberId);
        }

        ModelAndView modelAndView =new ModelAndView();
        modelAndView.setViewName("success");
        modelAndView.addObject("skuInfo", pmsSkuInfo);
        modelAndView.addObject("skuNum", quantity);
        return modelAndView;
    }

    private boolean isCartExisted(List<OmsCartItem> omsCartItemList, OmsCartItem omsCartItem) {
        boolean result = false;
        for (OmsCartItem cartItem : omsCartItemList) {
            if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())){
                cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                result = true;
            }
        }
        return result;


    }

    private BigDecimal getTotalAmout(List<OmsCartItem> omsCartItemList) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsCartItem omsCartItem : omsCartItemList) {
            BigDecimal totalPrice = omsCartItem.getTotalPrice();
            if(omsCartItem.getIsChecked().equals("1")){
                totalAmount = totalAmount.add(totalPrice);
            }
        }

        return totalAmount;
    }


}
