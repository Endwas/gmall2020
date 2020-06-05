package com.endwas.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.endwas.gmall.bean.PmsBaseAttrInfo;
import com.endwas.gmall.bean.PmsBaseAttrValue;
import com.endwas.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.endwas.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.endwas.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class AttrServiceImpl implements AttrService {
    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;

    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> pmsBaseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);
        return pmsBaseAttrInfos;
    }

    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {
        String id = pmsBaseAttrInfo.getId();
        if (StringUtils.isBlank(id)){
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {
                PmsBaseAttrValue pmsBaseAttrValue1 = new PmsBaseAttrValue();
                pmsBaseAttrValue1.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValue1.setValueName(pmsBaseAttrValue.getValueName());
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue1);
            }
        }else {
            Example example = new Example(PmsBaseAttrInfo.class);
            example.createCriteria().andEqualTo("id", id);
            pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo,example);

            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(id);
            pmsBaseAttrValueMapper.delete(pmsBaseAttrValue);

            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue attrValue : attrValueList) {
                PmsBaseAttrValue pmsBaseAttrValue1 = new PmsBaseAttrValue();
                pmsBaseAttrValue1.setAttrId(id);
                pmsBaseAttrValue1.setValueName(attrValue.getValueName());
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue1);

            }




        }


        return "success";
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {
        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        List<PmsBaseAttrValue> pmsBaseAttrValues = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);
        return pmsBaseAttrValues;

    }


}
