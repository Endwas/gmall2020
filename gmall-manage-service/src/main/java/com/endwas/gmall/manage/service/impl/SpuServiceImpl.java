package com.endwas.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.endwas.gmall.bean.PmsProductImage;
import com.endwas.gmall.bean.PmsProductInfo;
import com.endwas.gmall.bean.PmsProductSaleAttr;
import com.endwas.gmall.bean.PmsProductSaleAttrValue;
import com.endwas.gmall.manage.mapper.PmsProductImageMapper;
import com.endwas.gmall.manage.mapper.PmsProductInfoMapper;
import com.endwas.gmall.manage.mapper.PmsProductSaleAttrMapper;
import com.endwas.gmall.manage.mapper.PmsProductSaleAttrValueMapper;
import com.endwas.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    PmsProductInfoMapper pmsProductInfoMapper;

    @Autowired
    PmsProductSaleAttrMapper pmsProductSaleAttrMapper;

    @Autowired
    PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;

    @Autowired
    PmsProductImageMapper pmsProductImageMapper;

    @Override
    public List<PmsProductInfo> getSpuList(String catalog3Id) {
        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);
        List<PmsProductInfo> pmsProductInfos = pmsProductInfoMapper.select(pmsProductInfo);
        return pmsProductInfos;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {
        PmsProductSaleAttr pmsProductSaleAttr = new PmsProductSaleAttr();
        pmsProductSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> pmsProductSaleAttrs= pmsProductSaleAttrMapper.select(pmsProductSaleAttr);
        for (PmsProductSaleAttr productSaleAttr : pmsProductSaleAttrs) {
            PmsProductSaleAttrValue pmsProductSaleAttrValue = new PmsProductSaleAttrValue();
            pmsProductSaleAttrValue.setProductId(spuId);
            pmsProductSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
            List<PmsProductSaleAttrValue> pmsProductSaleAttrValues = pmsProductSaleAttrValueMapper.select(pmsProductSaleAttrValue);
            productSaleAttr.setSpuSaleAttrValueList(pmsProductSaleAttrValues);
        }
        return pmsProductSaleAttrs;
    }

    @Override
    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {
        pmsProductInfoMapper.insertSelective(pmsProductInfo);

        List<PmsProductSaleAttr> spuSaleAttrLists = pmsProductInfo.getSpuSaleAttrList();

        for (PmsProductSaleAttr spuSaleAttr : spuSaleAttrLists) {
            spuSaleAttr.setProductId(pmsProductInfo.getId());
            pmsProductSaleAttrMapper.insertSelective(spuSaleAttr);

            List<PmsProductSaleAttrValue> spuSaleAttrValueLists = spuSaleAttr.getSpuSaleAttrValueList();
            for (PmsProductSaleAttrValue spuSaleAttrValue: spuSaleAttrValueLists) {
                spuSaleAttrValue.setProductId(pmsProductInfo.getId());
                pmsProductSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }

        }

        List<PmsProductImage> spuImageLists = pmsProductInfo.getSpuImageList();
        for (PmsProductImage spuImage : spuImageLists) {
            spuImage.setProductId(pmsProductInfo.getId());
            pmsProductImageMapper.insertSelective(spuImage);
        }
        

        return "success";


    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {
        PmsProductImage pmsProductImage = new PmsProductImage();
        pmsProductImage.setProductId(spuId);
        List<PmsProductImage> pmsProductImages = pmsProductImageMapper.select(pmsProductImage);
        return pmsProductImages;

    }

    /**
     *
     *  因为使用tkmapper查询将返回该商品属性和属性值，所以需要将当前查询的sku属性值准确区分
     *  需要使用联合查询，将spuSaleAttrList查询的结果中包含transient字段isChecked
     *  所以不使用tkmapper改为传统mybatis
     *
     */
    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId, String skuId) {
        List<PmsProductSaleAttr> pmsProductSaleAttrs = pmsProductSaleAttrMapper.spuSaleAttrListCheckBySku(productId,skuId);


        return pmsProductSaleAttrs;

    }


}
