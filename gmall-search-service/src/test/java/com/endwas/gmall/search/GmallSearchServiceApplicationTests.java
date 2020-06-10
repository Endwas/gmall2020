package com.endwas.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.endwas.gmall.bean.PmsSearchSkuInfo;
import com.endwas.gmall.bean.PmsSkuInfo;
import com.endwas.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceApplicationTests {

    @Reference
    SkuService skuService;

    @Autowired
    JestClient jestClient;

    @Test
    public void contextLoads() throws IOException {

//        searchFromElasticSearch();
        putDataInElasticSearch();




    }
    public void searchFromElasticSearch() throws IOException {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //bool
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        // must
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName", "华为");
        boolQueryBuilder.must(matchQueryBuilder);
        //filter
        TermsQueryBuilder termsQueryBuilder = new TermsQueryBuilder("", "12");
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.attrId", "12");
        boolQueryBuilder.filter(termQueryBuilder);
        //from
        searchSourceBuilder.from(0);

        //size
        searchSourceBuilder.size(20);

        //highlight
        searchSourceBuilder.highlight(null);
        searchSourceBuilder.query(boolQueryBuilder);
        //执行生成dsl语句
        String dsl = searchSourceBuilder.toString();
        System.out.println(dsl);
        Search search = new Search.Builder(dsl).addIndex("gmall2020").addType("PmsSkuInfo").build();
        SearchResult execute = jestClient.execute(search);
        List<SearchResult.Hit<PmsSearchSkuInfo, Void>> hits = execute.getHits(PmsSearchSkuInfo.class);
        List<PmsSearchSkuInfo> searchList = new ArrayList<>();
        for (SearchResult.Hit<PmsSearchSkuInfo, Void> hit : hits) {
            PmsSearchSkuInfo source = hit.source;
            searchList.add(source);
        }

        System.out.println(searchList);


    }


    public void putDataInElasticSearch() throws IOException {
        List<PmsSkuInfo> skuInfoList = skuService.getAllSkuInfo();
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();


        for (PmsSkuInfo pmsSkuInfo : skuInfoList) {
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSkuInfo, pmsSearchSkuInfo);
            pmsSearchSkuInfo.setId(Long.parseLong(pmsSkuInfo.getId()));
            pmsSearchSkuInfoList.add(pmsSearchSkuInfo);

        }
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
            Index build = new Index.Builder(pmsSearchSkuInfo).index("gmall2020").type("PmsSkuInfo").id(pmsSearchSkuInfo.getId()+"").build();
            jestClient.execute(build);
        }
    }


}
