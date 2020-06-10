package com.endwas.gmall.search.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.endwas.gmall.bean.*;
import com.endwas.gmall.service.AttrService;
import com.endwas.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class SearchController {
    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("index")
    public String index() {
        return "index";
    }

    //注意！！数据太少，很容易出现数据库查找到空报错的情况，但正常电商不会出现，所以就不加空值校验了
    @RequestMapping("list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) {
        //查询符合条件的商品
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = searchService.searchList(pmsSearchParam);
        modelMap.put("skuLsInfoList", pmsSearchSkuInfoList);

        String keyword = pmsSearchParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            modelMap.put("keyword", keyword);
        }

        //返回页面当前查询的商品所有的attrValueId平台属性值
        //使用HashSet避免里面的平台属性值重复，让它去重。
        Set<String> valueIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
            List<PmsSkuAttrValue> skuAttrValueList = pmsSearchSkuInfo.getSkuAttrValueList();
            for (PmsSkuAttrValue pmsSkuAttrValue : skuAttrValueList) {
                valueIdSet.add(pmsSkuAttrValue.getValueId());
            }
        }
        String attrId = StringUtils.join(valueIdSet, ",");
        List<PmsBaseAttrInfo> baseAttrInfoList = attrService.getBaseAttrInfoByAttrId(attrId);


        //移除重复的平台属性
        //同时生成面包屑功能，需要注意循环嵌套！！很容易就赋空值了
        List<PmsSearchCrumb> pmsSearchCrumbList = new ArrayList<>();
        String[] delValueIds = pmsSearchParam.getValueId();
        if (delValueIds != null) {
            for (String delId : delValueIds) {
                // 迭代器需要每循环一次delId就生成一次，否则第二次delID就无法拿到crumbName了
                Iterator<PmsBaseAttrInfo> iterator = baseAttrInfoList.iterator();
                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                pmsSearchCrumb.setValueId(delId);
                pmsSearchCrumb.setUrlParam(getUrlParamOfCrumb(pmsSearchParam, delId));
                while (iterator.hasNext()) {
                    PmsBaseAttrInfo next = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = next.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                        if (pmsBaseAttrValue.getId().equals(delId)) {
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            iterator.remove();
                        }
                    }
                }
                pmsSearchCrumbList.add(pmsSearchCrumb);

            }
        }
        modelMap.put("attrValueSelectedList", pmsSearchCrumbList);
        modelMap.put("attrList", baseAttrInfoList);

        //返回页面当前url字符串
        String urlParam = getUrlParam(pmsSearchParam);
        modelMap.put("urlParam", urlParam);
        return "list";
    }

    private String getUrlParamOfCrumb(PmsSearchParam pmsSearchParam, String delId) {
        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueId = pmsSearchParam.getValueId();


        String urlCrumb = "";
        if (StringUtils.isNotBlank(catalog3Id)) {
            urlCrumb += "catalog3Id=" + catalog3Id;
        }
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlCrumb)) {
                urlCrumb = urlCrumb + "&";
            }
            urlCrumb += "keyword=" + keyword;
        }
        if (valueId != null) {
            for (String id : valueId) {
                if (delId != id){
                    urlCrumb += "&valueId=" + id;

                }
            }
        }
        return urlCrumb;
    }


    private String getUrlParam(PmsSearchParam pmsSearchParam) {
        String urlParam = "";
        if (StringUtils.isNotBlank(pmsSearchParam.getCatalog3Id())) {
            urlParam += "catalog3Id=" + pmsSearchParam.getCatalog3Id();
        }
        if (StringUtils.isNotBlank(pmsSearchParam.getKeyword())) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&";
            }
            urlParam += "keyword=" + pmsSearchParam.getKeyword();
        }
        if (pmsSearchParam.getValueId() != null) {
            for (String id : pmsSearchParam.getValueId()) {
                urlParam += "&valueId=" + id;
            }
        }
        return urlParam;


    }
}
