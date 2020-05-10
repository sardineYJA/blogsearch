package com.example.blogsearch.Service;

import com.example.blogsearch.Entity.BlogEntity;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ReadMarkdownServiceTest {
    @Autowired
    ReadMarkdownService readMarkdownService;

    @Autowired
    BlogEsService blogEsService;

    // md文件路径
    String mdFilePath = "E:\\GitHub\\sardineYJA.github.io\\_posts\\";
    String fileName = "0000-0-0-Other-Linux-Proxy.md";

    @Test
    public void testReadMarkdownService() {
        // 测试读取Markdown文件列表
        List<String> files = readMarkdownService.getMarkdownList(mdFilePath);
        System.out.println(files.size());
        System.out.println(files);
    }

    @Test
    public void testIsIndexExists() {
        // 测试索引检测与删除
        System.out.println(blogEsService.isIndexExists("blog"));
        System.out.println(blogEsService.deleteIndex("blog"));
        System.out.println(blogEsService.isIndexExists("blog"));
    }

    @Test
    public void testGetBlogEntityByParseFile() {
        // 测试插入单条数据到ES
        BlogEntity blogEntity = readMarkdownService.getBlogEntityByParseFile(mdFilePath, fileName);
        System.out.println(blogEntity);
        blogEntity.setId("1");
        blogEsService.insertOrUpdateOne("blog", blogEntity);
    }

    @Test
    public void testBatchInsert() {
        // 测试批量插入
        BlogEntity b3 = readMarkdownService.getBlogEntityByParseFile(mdFilePath, fileName);
        b3.setId("31");
        BlogEntity b4 = readMarkdownService.getBlogEntityByParseFile(mdFilePath, fileName);
        b4.setId("41");
        BlogEntity b5 = readMarkdownService.getBlogEntityByParseFile(mdFilePath, fileName);
        b5.setId("51");

        List<BlogEntity> blist = new ArrayList<>();
        blist.add(b3);
        blist.add(b4);
        blist.add(b5);
        blogEsService.insertBath("blog", blist);
    }

    @Test
    public void testDeleteByQuery() {
        // 删除指定文档，字段:字段值
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("_id", "14");
        blogEsService.deleteByQuery("blog", termQueryBuilder);
    }

    @Test
    public void testSearch() {
        // 测试SearchSourceBuilder
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.from(1);     // 下标0开始
        builder.size(3);     // 查询个数
        builder.sort("_id", SortOrder.DESC);  // 排序
        List<BlogEntity> blogList = blogEsService.search("blog", builder, BlogEntity.class);
        System.out.println(blogList);
    }

    @Test
    public void testSearch2() {
        // 测试精确查询
        SearchSourceBuilder builder = new SearchSourceBuilder();

        // keyword 精确查询
        // builder.query(QueryBuilders.termQuery("id", "101"));

        // 精确查询，查询条件不会进行分词，但是查询内容可能会分词。title为text时最后加.keyword
        // builder.query(QueryBuilders.termQuery("title.keyword", "Ubuntu设置代理"));

        // 多个内容在一个字段中进行查询
        builder.query(QueryBuilders.termsQuery("title.keyword", "101TT", "102TT"));

        List<BlogEntity> blogList = blogEsService.search("blog", builder, BlogEntity.class);
        System.out.println(blogList);
    }

    @Test
    public void testSearch3() {
        // 测试匹配查询
        SearchSourceBuilder builder = new SearchSourceBuilder();

        // 通配符查询，*表示0个或多个字符，?表示单个字符
        //builder.query(QueryBuilders.wildcardQuery("title", "*代理"));

        // 匹配查询
        builder.query(QueryBuilders.matchQuery("content", "设"));

        // 多字段匹配查询
        // builder.query(QueryBuilders.multiMatchQuery("测试",  "title", "content"));

        List<BlogEntity> blogList = blogEsService.search("blog", builder, BlogEntity.class);
        System.out.println(blogList.size());
    }

    @Test
    public void testSearch4() {
        // 测试模糊查询
        SearchSourceBuilder builder = new SearchSourceBuilder();
        // 模糊查询，字段包含某个字符(单个中文或单词)
        // builder.query(QueryBuilders.fuzzyQuery("content",  "本"));
        builder.query(QueryBuilders.fuzzyQuery("title",  "Ubuntu"));
        List<BlogEntity> blogList = blogEsService.search("blog", builder, BlogEntity.class);
        System.out.println(blogList);
    }

    @Test
    public void testSearch5() {
        // 测试范围查询
        SearchSourceBuilder builder = new SearchSourceBuilder();

        // 年龄 >= 30
        // builder.query(QueryBuilders.rangeQuery("age").gte(30));


        // [年(y)、月(M)、星期(w)、天(d)、小时(h)、分钟(m)、秒(s)]
        // now-1h 查询一小时内范围
        // now-1d 查询一天内时间范围
        // now-1y 查询最近一年内的时间范围

        // 范围查询，近2年
        // builder.query(QueryBuilders.rangeQuery("createDate").gte("now-2y").includeLower(true).includeUpper(true));

        // 指定范围
        builder.query(QueryBuilders.rangeQuery("createDate").from("1900-01-01").to("2000-01-01").includeLower(true).includeUpper(true));

        List<BlogEntity> blogList = blogEsService.search("blog", builder, BlogEntity.class);
        System.out.println(blogList);
    }

    @Test
    public void testSearch6() {
        // 测试scroll查询，这边测试数据需要超过10000条
        SearchSourceBuilder builder = new SearchSourceBuilder();
        List<BlogEntity> blogList = blogEsService.scrollSearch("blog", builder, 100L, BlogEntity.class);
        System.out.println(blogList.size());
    }

    @Test
    public void testSearch7() {
        List<BlogEntity> blogList = blogEsService.searchAll("blog", BlogEntity.class);
        System.out.println(blogList);
    }
}