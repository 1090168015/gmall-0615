package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.vo.SearchParamVO;
import com.atguigu.gmall.search.vo.SearchResponse;
import io.searchbox.core.SearchResult;


import java.security.PrivateKey;

public interface SearchService {
    SearchResponse search(SearchParamVO searchParamVO);
    public String buildDSL(SearchParamVO searchParamVO);
    public SearchResponse parseResult(SearchResult result);
}
