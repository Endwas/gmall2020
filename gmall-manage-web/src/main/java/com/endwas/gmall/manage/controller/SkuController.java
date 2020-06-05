package com.endwas.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.endwas.gmall.bean.PmsSkuInfo;
import com.endwas.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@CrossOrigin
public class SkuController {
    @Reference
    SkuService skuService;


    @RequestMapping("saveSkuInfo")
    @ResponseBody
    public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo){
        if(StringUtils.isBlank(pmsSkuInfo.getSkuDefaultImg())&& (pmsSkuInfo.getSkuImageList().size() > 0)){
            pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getSkuImageList().get(0).getImgUrl());
        }

        String status = skuService.saveSkuInfo(pmsSkuInfo);

        return status;
    }
}
