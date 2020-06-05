package com.endwas.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.endwas.gmall.bean.PmsSkuAttrValue;
import com.endwas.gmall.bean.PmsSkuImage;
import com.endwas.gmall.bean.PmsSkuInfo;
import com.endwas.gmall.bean.PmsSkuSaleAttrValue;
import com.endwas.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.endwas.gmall.manage.mapper.PmsSkuImageMapper;
import com.endwas.gmall.manage.mapper.PmsSkuInfoMapper;
import com.endwas.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.endwas.gmall.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
}
