package com.endwas.gmall.service;

import com.endwas.gmall.bean.OmsCartItem;
import com.endwas.gmall.bean.PmsSkuInfo;
import com.endwas.gmall.bean.PmsSkuSaleAttrValue;

import java.math.BigDecimal;
import java.util.List;

public interface SkuService {
    String saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuById(String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueListBySpu(String productId);

    List<PmsSkuInfo> getAllSkuInfo();

    boolean checkPrice(String productSkuId, BigDecimal price);
}
