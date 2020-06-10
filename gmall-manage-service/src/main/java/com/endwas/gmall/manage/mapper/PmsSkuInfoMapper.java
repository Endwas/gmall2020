package com.endwas.gmall.manage.mapper;

import com.endwas.gmall.bean.PmsSkuInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PmsSkuInfoMapper extends Mapper<PmsSkuInfo> {


    List<PmsSkuInfo> selectSkuSaleAttrValueListBySpu(@Param("id") String productId);
}
