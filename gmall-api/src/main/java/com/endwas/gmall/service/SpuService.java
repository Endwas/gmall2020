package com.endwas.gmall.service;

import com.endwas.gmall.bean.PmsProductImage;
import com.endwas.gmall.bean.PmsProductInfo;
import com.endwas.gmall.bean.PmsProductSaleAttr;

import java.util.List;

public interface SpuService {
    List<PmsProductInfo> getSpuList(String catalog3Id);

    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    String saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductImage> spuImageList(String spuId);
}
