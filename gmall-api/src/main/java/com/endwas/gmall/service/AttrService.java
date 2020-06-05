package com.endwas.gmall.service;

import com.endwas.gmall.bean.PmsBaseAttrInfo;
import com.endwas.gmall.bean.PmsBaseAttrValue;
import com.endwas.gmall.bean.PmsBaseSaleAttr;

import java.util.List;

public interface AttrService {
    List<PmsBaseAttrInfo> attrInfoList(String catalog3Id);

    String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo);

    List<PmsBaseAttrValue> getAttrValueList(String attrId);

    List<PmsBaseSaleAttr> getBaseSaleAttrList();
}
