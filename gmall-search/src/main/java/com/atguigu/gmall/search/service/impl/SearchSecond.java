package com.atguigu.gmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponse;
import com.atguigu.gmall.search.vo.SearchResponseAttrVO;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.AvgAggregation;
import io.searchbox.core.search.aggregation.ChildrenAggregation;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.swing.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/*private String[] catelog3;//三级分类id
private String[] brand;//品牌id
private String keyword;//检索的关键字
private String order;// 0：综合排序  1：销量  2：价格
private Integer pageNum = 1;//分页信息
//props=2:全高清&  如果前端想传入很多值    props=2:青年-老人-女士
//2:win10-android-
//3:4g
//4:5.5
private String[] props;//页面提交的数组,搜索规格的属性
private Integer pageSize = 12;
private Integer priceFrom;//价格区间开始
private Integer priceTo;//价格区间结束*/
public class SearchSecond {
    @Autowired
    private JestClient jestClient;

    public SearchResponse search(SearchParamVO searchParamVO){
        try {
        String dsl = buildDsl(searchParamVO);
        Search search = new Search.Builder(dsl).addIndex("goods").addType("info").build();

            SearchResult searchResult = this.jestClient.execute(search);
            SearchResponse searchResponse = parseResult(searchResult);
            searchResponse.setPageSize(searchParamVO.getPageSize());
            searchResponse.setPageNum(searchParamVO.getPageNum());
            searchResponse.setTotal(searchResult.getTotal());
            return searchResponse;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }



    public String buildDsl(SearchParamVO searchParamVO){
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //构建bool查询条件，因为bool查询可以融合各种查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        String keyword = searchParamVO.getKeyword();
        if (!keyword.isEmpty()){
            boolQuery.must(QueryBuilders.matchQuery("name",keyword).operator(Operator.AND));
        }
        //构建过滤条件
        //品牌过滤
        String[] brands = searchParamVO.getBrand();
        if (ArrayUtils.isNotEmpty(brands)){
            boolQuery.filter(QueryBuilders.termQuery("brandId",brands));
        }
        //分类过滤
        String[] catelog3 = searchParamVO.getCatelog3();
        if (ArrayUtils.isNotEmpty(catelog3)){
            boolQuery.filter(QueryBuilders.termsQuery("productCategoryId",catelog3));
        }
        //属性过滤
        String[] props = searchParamVO.getProps();
        if (ArrayUtils.isNotEmpty(props)){
            for (String prop : props) {
                String[] attr = StringUtils.split(prop, ":");
                if (attr != null && attr.length==2) {
                    BoolQueryBuilder propBoolQuery = QueryBuilders.boolQuery();
                    propBoolQuery.must(QueryBuilders.termQuery("attrValueList.productAttributeId",attr[0]));
                    String[] values = StringUtils.split(attr[1], "-");
                    propBoolQuery.must(QueryBuilders.termsQuery("attrValueList.value",values));
                    boolQuery.filter(QueryBuilders.nestedQuery("attrValueList",propBoolQuery, ScoreMode.None));
                }
            }
        }
        sourceBuilder.query(boolQuery);

        //分页构建
        Integer pageSize = searchParamVO.getPageSize();
        Integer pageNum = searchParamVO.getPageNum();
        sourceBuilder.from((pageNum-1)*pageSize);
        sourceBuilder.size(pageSize);
        //构建排序
        String order = searchParamVO.getOrder();
        if (order != null) {
            String[] orders = StringUtils.split(order, ":");
            if (orders!=null&&orders.length==2){
                SortOrder sortOrder = StringUtils.equals("asc", orders[1]) ? SortOrder.ASC : SortOrder.DESC;
                switch (orders[0]){
                    case "0" : sourceBuilder.sort("_score",sortOrder);break;
                    case "1" : sourceBuilder.sort("sale",sortOrder);break;
                    case "2" : sourceBuilder.sort("price",sortOrder);break;
                }
            }
        }
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name");
        highlightBuilder.preTags("<font color='red'>");
        highlightBuilder.postTags("</font>");
        sourceBuilder.highlighter(highlightBuilder);

        //构建聚合
        //品牌聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("brandAgg").field("brandId")
                    .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName")));
        //聚合分类
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryAgg").field("productCategoryId")
                    .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("productCategoryName")));
        //聚合搜索属性
        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","attrValueList")
                    .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrValueList.productAttributeId")
                            .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrValueList.name"))
                            .subAggregation(AggregationBuilders.terms("attrvalueAgg").field("attrValueList.value"))));
        return  sourceBuilder.toString();
    }

    public SearchResponse parseResult(SearchResult result){
        SearchResponse searchResponse = new SearchResponse();
        MetricAggregation aggregations = result.getAggregations();
        TermsAggregation brandAgg = aggregations.getTermsAggregation("brandAgg");
        List<TermsAggregation.Entry> buckets = brandAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)){
            SearchResponseAttrVO attrVO = new SearchResponseAttrVO();
            attrVO.setName("品牌");
            List<String> brandValues = buckets.stream().map(bucket -> {
                HashMap<String, Object> map = new HashMap<>();
                map.put("id", bucket.getKeyAsString());
                TermsAggregation brandNameAgg = bucket.getTermsAggregation("brandNameAgg");
                map.put("name", brandNameAgg.getBuckets().get(0).getKeyAsString());
                return JSON.toJSONString(map);

            }).collect(Collectors.toList());
            attrVO.setValue(brandValues);
            searchResponse.setBrand(attrVO);

        }
        //2. 解析分类的聚合结果集
        TermsAggregation categoryAgg = aggregations.getTermsAggregation("categoryAgg");
        List<TermsAggregation.Entry> categoryAggBuckets = categoryAgg.getBuckets();
        if (!CollectionUtils.isEmpty(categoryAggBuckets)){
            SearchResponseAttrVO categoryVO = new SearchResponseAttrVO();
            categoryVO.setName("分类");
            List<String> categoryValues = categoryAggBuckets.stream().map(categoryAggBucket -> {
                HashMap<String, Object> map = new HashMap<>();
                map.put("id", categoryAggBucket.getKeyAsString());
                TermsAggregation categoryNameAgg = categoryAggBucket.getTermsAggregation("categoryNameAgg");
                map.put("name", categoryNameAgg.getBuckets().get(0).getKeyAsString());
                return JSON.toJSONString(map);
            }).collect(Collectors.toList());
            categoryVO.setValue(categoryValues);
            searchResponse.setCatelog(categoryVO);
        }
        //3.解析搜索属性的聚合结果集
        ChildrenAggregation attrAgg = aggregations.getChildrenAggregation("attrAgg");
        TermsAggregation attrIdAgg = attrAgg.getTermsAggregation("attrIdAgg");

       // List<TermsAggregation.Entry> attrIdAggBuckets = attrIdAgg.getBuckets();
        List<SearchResponseAttrVO> attrVOS = attrIdAgg.getBuckets().stream().map(attrBucket -> {
            SearchResponseAttrVO attrVO = new SearchResponseAttrVO();
            attrVO.setProductAttributeId(Long.valueOf(attrBucket.getKeyAsString()));
            TermsAggregation attrNameAgg = attrBucket.getTermsAggregation("attrNameAgg");
            attrVO.setName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            TermsAggregation attrValueAgg = attrBucket.getTermsAggregation("attrValueAgg");
            List<String> attrValues = attrValueAgg.getBuckets().stream().map(attrValueBucket ->
                    attrValueBucket.getKeyAsString()
            ).collect(Collectors.toList());

            attrVO.setValue(attrValues);
            return attrVO;
        }).collect(Collectors.toList());
        searchResponse.setAttrs(attrVOS);
        List<GoodsVO> goodsVOS = result.getSourceAsObjectList(GoodsVO.class, false);
        searchResponse.setProducts(goodsVOS);
        return searchResponse ;
    }

}
