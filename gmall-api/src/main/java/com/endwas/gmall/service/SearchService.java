package com.endwas.gmall.service;

import com.endwas.gmall.bean.PmsSearchParam;
import com.endwas.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> searchList(PmsSearchParam pmsSearchParam);
}
