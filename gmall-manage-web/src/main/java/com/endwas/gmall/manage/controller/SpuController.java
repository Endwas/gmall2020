package com.endwas.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.endwas.gmall.bean.PmsBaseSaleAttr;
import com.endwas.gmall.bean.PmsProductImage;
import com.endwas.gmall.bean.PmsProductInfo;
import com.endwas.gmall.bean.PmsProductSaleAttr;
import com.endwas.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@CrossOrigin
public class SpuController {
    @Reference
    SpuService spuService;

    @RequestMapping("spuList")
    @ResponseBody
    public List<PmsProductInfo> spuList(String catalog3Id){
        List<PmsProductInfo> pmsProductInfos = spuService.getSpuList(catalog3Id);
        return pmsProductInfos;
    }


    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){
        List<PmsProductSaleAttr> pmsProductSaleAttrs = spuService.spuSaleAttrList(spuId);
        return pmsProductSaleAttrs;
    }

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<PmsProductImage> spuImageList(String spuId){
        List<PmsProductImage> pmsProductImages = spuService.spuImageList(spuId);
        return pmsProductImages;
    }

    @RequestMapping("saveSpuInfo")
    @ResponseBody
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){
        String status = spuService.saveSpuInfo(pmsProductInfo);
        return status;
    }

    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){
        // 使用FastDfs上传图片，并生成返回一个图片地址

        return "https://img13.360buyimg.com/n1/s450x450_jfs/t1/120774/35/4128/121871/5ed8e0efEdc75cc57/684d631c813745c4.jpg";
    }




}
