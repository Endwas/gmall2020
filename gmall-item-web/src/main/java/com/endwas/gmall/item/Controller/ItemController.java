package com.endwas.gmall.item.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.endwas.gmall.bean.PmsProductSaleAttr;
import com.endwas.gmall.bean.PmsSkuInfo;
import com.endwas.gmall.bean.PmsSkuSaleAttrValue;
import com.endwas.gmall.service.SkuService;
import com.endwas.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {
    @Reference
    SkuService skuService;
    
    @Reference
    SpuService spuService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap modelMap){
        PmsSkuInfo pmsSkuInfo = skuService.getSkuById(skuId);

        if (pmsSkuInfo == null) {
           return "error";
        }

        List<PmsProductSaleAttr> spuSaleAttrListCheckBySku = spuService.spuSaleAttrListCheckBySku(pmsSkuInfo.getProductId(), pmsSkuInfo.getId());

        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        List<PmsSkuInfo> pmsSkuInfos = skuService.getSkuSaleAttrValueListBySpu(pmsSkuInfo.getProductId());
        for (PmsSkuInfo skuInfo : pmsSkuInfos) {
            String k = "";
            String v = skuInfo.getId();
            List<PmsSkuSaleAttrValue> attrValueList = skuInfo.getSkuSaleAttrValueList();
            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : attrValueList) {
                k += pmsSkuSaleAttrValue.getSaleAttrValueId() + "|";
            }
            hashMap.put(k, v);
        }
        String skuSaleAttrHashJsonStr = JSON.toJSONString(hashMap);
        modelMap.put("skuInfo", pmsSkuInfo);
        modelMap.put("spuSaleAttrListCheckBySku", spuSaleAttrListCheckBySku);
        modelMap.put("skuSaleAttrHashJsonStr", skuSaleAttrHashJsonStr);
        return "item";

    }
}
