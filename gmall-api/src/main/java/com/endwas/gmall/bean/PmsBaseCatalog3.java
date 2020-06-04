package com.endwas.gmall.bean;

import javax.persistence.Id;
import java.io.Serializable;
//bean和数据库能够关联，必须保证bean类名为全大写，而数据库表名为全部小写，并以"_"隔开多个单词
public class PmsBaseCatalog3 implements Serializable {
    @Id
    private String id;
    private String name;

// bean类要和数据库进行查询，必须保证其成员变量和数据库内的字段一一对应，
// 即bean类以驼峰定义、数据库以"_"隔开多个单词，否则检测不予通过，报错

    private String catalog2Id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCatalog2Id() {
        return catalog2Id;
    }

    public void setCatalog2Id(String catalog2Id) {
        this.catalog2Id = catalog2Id;
    }
}
