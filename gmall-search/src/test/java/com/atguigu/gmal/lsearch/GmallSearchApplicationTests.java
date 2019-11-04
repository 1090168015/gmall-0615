package com.atguigu.gmal.lsearch;

import io.searchbox.client.JestClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallSearchApplicationTests {
    @Autowired
    private JestClient jestClient;
    @Test
    void contextLoads() {
    }

}
