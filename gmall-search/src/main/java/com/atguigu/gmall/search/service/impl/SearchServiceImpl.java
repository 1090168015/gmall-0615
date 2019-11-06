package com.atguigu.gmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.search.service.SearchService;
import com.atguigu.gmall.search.vo.GoodsVO;
import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponse;
import com.atguigu.gmall.search.vo.SearchResponseAttrVO;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.CardinalityAggregation;
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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private JestClient jestClient;

    @Override//响应的请求查询数据处理后的结果
    public SearchResponse search(SearchParamVO searchParamVO) {
        System.out.println(searchParamVO);
        try {
        String dsl = buildDSL(searchParamVO);
        System.out.println(dsl);
        Search search = new Search.Builder(dsl).addIndex("goods").addType("info").build();

            SearchResult searchResult = this.jestClient.execute(search);
            System.out.println("search:"+searchResult);
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

    //解析请求数据            //SearchResult为ES封装的对象，用于获取ES检索数据的获取域封装
    public SearchResponse parseResult(SearchResult result){
        SearchResponse response = new SearchResponse();
        //获取所有聚合
        MetricAggregation aggregations = result.getAggregations();//获取所有聚合结果集
        //1.解析品牌聚合结果集
        //获取品牌聚合,（一个聚合对象里可以有多个桶，可以包括多个品牌桶对象，多个分组桶对象等，这里从聚合里获取品牌桶桶对象，一个桶代表一个品牌对象）
        TermsAggregation brandAgg = aggregations.getTermsAggregation("brandAgg");
        //获取品牌聚合中的所有桶
        List<TermsAggregation.Entry> buckets = brandAgg.getBuckets();
        // 判断品牌聚合是否为空
        if (!CollectionUtils.isEmpty(buckets)){
            // 初始化品牌vo对象
            SearchResponseAttrVO attrVO = new SearchResponseAttrVO();
            attrVO.setName("品牌");// 写死品牌聚合名称
            List<String> brandValues = buckets.stream().map(bucket -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", bucket.getKeyAsString());//将品牌桶id获取并封装为map
                TermsAggregation brandNameAgg = bucket.getTermsAggregation("brandNameAgg");//获取品桶的名称
                map.put("name", brandNameAgg.getBuckets().get(0).getKeyAsString());//将品牌桶名称获取并封装为map
                return JSON.toJSONString(map);//将map集合转化为json字符串
            }).collect(Collectors.toList());
            attrVO.setValue(brandValues);// 设置品牌的所有聚合值
            response.setBrand(attrVO);
        }
        //2. 解析分类的聚合结果集
        TermsAggregation categoryAgg = aggregations.getTermsAggregation("categoryAgg");
        //获取分类聚合中的所有桶
        List<TermsAggregation.Entry> categoryAggBuckets = categoryAgg.getBuckets();
        if ( !CollectionUtils.isEmpty(categoryAggBuckets)){
            //初始化分类VO
            SearchResponseAttrVO categoryVO = new SearchResponseAttrVO();
            List<String> actegoryValues = categoryAggBuckets.stream().map(categoryBucket -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", categoryBucket.getKeyAsString());
                TermsAggregation categoryNameAgg = categoryBucket.getTermsAggregation("categoryNameAgg");
                map.put("name", categoryNameAgg.getBuckets().get(0).getKeyAsString());
                return JSON.toJSONString(map);
            }).collect(Collectors.toList());
            categoryVO.setValue(actegoryValues);
            response.setCatelog(categoryVO);
        }
        //3.解析搜索属性的聚合结果集
        //获取商品搜索属性商品父聚合对象
        ChildrenAggregation attrAgg = aggregations.getChildrenAggregation("attrAgg");
        //由父聚合对象获取子聚合对象，
        TermsAggregation attrIdAgg = attrAgg.getTermsAggregation("attrIdAgg");
        //获取搜索属性中所有的桶，获取名为“attrIdAgg”子聚合内的所有桶
       // List<TermsAggregation.Entry> attrIdAggBuckets = attrIdAgg.getBuckets();
        List<SearchResponseAttrVO> attrVOS=  attrIdAgg.getBuckets().stream().map(attrBucket-> {
                //初始化搜索VO对象
            SearchResponseAttrVO attrVO = new SearchResponseAttrVO();
            attrVO.setProductAttributeId(Long.valueOf(attrBucket.getKeyAsString()));
            // 获取搜索属性的子聚合（搜索属性名）||||搜索属性的商品名字桶，获取子聚合对象的名字为（attrNameAgg）子聚合对象（孙子聚合对象）
            TermsAggregation attrNameAgg = attrBucket.getTermsAggregation("attrNameAgg");
            attrVO.setName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            // 获取搜索属性的子聚合（搜索属性名）||||搜索属性的商品名字桶，获取子聚合对象的名字为（attrvalueAgg）子聚合对象（孙子聚合对象）
            TermsAggregation attrvalueAgg = attrBucket.getTermsAggregation("attrValueAgg");
            List<String> attrValues = attrvalueAgg.getBuckets().stream().map(attrValueBucket ->
                    attrValueBucket.getKeyAsString()
            ).collect(Collectors.toList());

            attrVO.setValue(attrValues);
            return attrVO;
        }).collect(Collectors.toList());
        response.setAttrs(attrVOS);
        // 解析商品列表的结果集           List<GoodsVO> goodsVoS 检索出来的商品信息
        List<GoodsVO> goodsVoS = result.getSourceAsObjectList(GoodsVO.class, false);
        response.setProducts(goodsVoS);
        return response;
    }
    @Override
    public String buildDSL(SearchParamVO searchParamVO) {//由接收的请求数据模型，构建dsl查询语句，用于 基本检索
//        1.构建查询过滤条件
        //构建查询条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();//SearchSourceBuilder搜索条件构建器，辅助构建dsl语句
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();//QueryBuilders工具类，用于构建各种查询
        SearchSourceBuilder searchSourceBuilder = sourceBuilder.query(boolQueryBuilder);
        String keyword = searchParamVO.getKeyword();
        if (StringUtils.isNotEmpty(keyword)){
            boolQueryBuilder.must(QueryBuilders.matchQuery("name",keyword).operator(Operator.AND));
        }
        //构建过滤条件
        String[] brands = searchParamVO.getBrand();//品牌过滤
        if (ArrayUtils.isNotEmpty(brands)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId",brands));
        }
        String[] catelog3 = searchParamVO.getCatelog3();//分类过滤
        if (ArrayUtils.isNotEmpty(catelog3)){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("productCategoryId",catelog3));
        }
        //搜索的规格属性过滤
        String[] props = searchParamVO.getProps();
        if (ArrayUtils.isNotEmpty(props)){
            for (String prop : props) {
                String[] attr = StringUtils.split(prop, ":");
                if (attr != null && attr.length==2) {
                    BoolQueryBuilder propBoolQuery = QueryBuilders.boolQuery();//构建获取组合查询对象
                    propBoolQuery.must(QueryBuilders.termsQuery("attrValueList.productAttributeId",attr[0]));
                    String[] values = StringUtils.split(attr[1], "-");
                    propBoolQuery.must(QueryBuilders.termsQuery("attrValueList.value",values));
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("attrValueList",propBoolQuery, ScoreMode.None));
                }
            }
        }
        sourceBuilder.query(boolQueryBuilder);
//        2.完成分页的构建
        Integer pageNum = searchParamVO.getPageNum();
        Integer pageSize = searchParamVO.getPageSize();
        sourceBuilder.from((pageNum-1)*pageSize);
        sourceBuilder.size(pageSize);
//        3.完成排序的构建       排序 0：综合排序  1：销量  2：价格 order=1:asc
        String order = searchParamVO.getOrder();
        if (order != null ) {
            String[] orders = StringUtils.split(order, ":");//将order字符串转化为orders集合，以字符串的：为分割符，将字符串拆分为字符串集合
            if (orders != null && orders.length==2) {                   //索引0为排序代号，索引1为排序代号对应的排序方式
                SortOrder sortOrder = StringUtils.equals("asc", orders[1]) ? SortOrder.ASC : SortOrder.DESC;
                switch (orders[0]){
                    case "0":sourceBuilder.sort("_score",sortOrder);break;
                    case "1":sourceBuilder.sort("sale",sortOrder);break;
                    case "2":sourceBuilder.sort("price",sortOrder);break;
                }
            }
        }

//        4.完成高亮的构建
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name");
        highlightBuilder.preTags("<font color='red'>");
        highlightBuilder.postTags("</font>");
        sourceBuilder.highlighter(highlightBuilder);
//       5.完成聚合的构建
            //品牌聚合
        sourceBuilder.aggregation(AggregationBuilders.terms("brandAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName")));
        //聚合分类
        sourceBuilder.aggregation(
                AggregationBuilders.terms("categoryAgg").field("productCategoryId")
                    .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("productCategoryName"))
        );
        //聚合搜索属性
        sourceBuilder.aggregation(
                AggregationBuilders.nested("attrAgg","attrValueList")
                    .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrValueList.productAttributeId")
                            .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrValueList.name"))
                            .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrValueList.value"))));

//        BoolQuerybuilders    //构建查询工具类
//        Operator    构建符枚举类AND/OR
//        构建查询条件
        return sourceBuilder.toString();

    }
}
