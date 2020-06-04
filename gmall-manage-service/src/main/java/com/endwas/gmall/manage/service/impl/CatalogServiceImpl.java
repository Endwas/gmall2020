package com.endwas.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.endwas.gmall.bean.PmsBaseCatalog1;
import com.endwas.gmall.bean.PmsBaseCatalog2;
import com.endwas.gmall.bean.PmsBaseCatalog3;
import com.endwas.gmall.manage.mapper.PmsBaseCatalog1Mapper;
import com.endwas.gmall.manage.mapper.PmsBaseCatalog2Mapper;
import com.endwas.gmall.manage.mapper.PmsBaseCatalog3Mapper;
import com.endwas.gmall.service.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class CatalogServiceImpl implements CatalogService {
    @Autowired
    PmsBaseCatalog1Mapper pmsBaseCatalog1Mapper;

    @Autowired
    PmsBaseCatalog2Mapper pmsBaseCatalog2Mapper;

    @Autowired
    PmsBaseCatalog3Mapper pmsBaseCatalog3Mapper;


    @Override
    public List<PmsBaseCatalog1> getCatalog1() {
        return pmsBaseCatalog1Mapper.selectAll();
    }

    @Override
    public List<PmsBaseCatalog2> getCatalog2(String catalog1Id) {
        Example e = new Example(PmsBaseCatalog2.class);
        e.createCriteria().andEqualTo("catalog1Id", catalog1Id);
        List<PmsBaseCatalog2> catalog2s = pmsBaseCatalog2Mapper.selectByExample(e);
        return catalog2s;

    }

    @Override
    public List<PmsBaseCatalog3> getCatalog3(String catalog2Id) {
        Example e = new Example(PmsBaseCatalog3.class);
        e.createCriteria().andEqualTo("catalog2Id", catalog2Id);
        List<PmsBaseCatalog3> catalog3s = pmsBaseCatalog3Mapper.selectByExample(e);
        return catalog3s;
    }


}
