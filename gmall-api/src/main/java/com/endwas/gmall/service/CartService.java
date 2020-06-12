package com.endwas.gmall.service;

import com.endwas.gmall.bean.OmsCartItem;

import java.util.List;

public interface CartService {
    OmsCartItem getCartFromDb(String memberId, String skuId);

    void addCart(OmsCartItem omsCartItem);

    void updateCart(OmsCartItem omsCartItemFromDb);

    void flushCartCache(String memberId);

    List<OmsCartItem> getUserCart(String memberId);

    void checkCart(String isChecked, String skuId, String memberId);
}
