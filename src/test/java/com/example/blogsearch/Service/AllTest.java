package com.example.blogsearch.Service;

import com.example.blogsearch.Entity.BlogEntity;
import lombok.extern.slf4j.Slf4j;
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
public class AllTest {
    @Autowired
    ReadMarkdownService readMarkdownService;

    @Autowired
    BlogEsService blogEsService;

    // md文件路径
    String mdFilePath = "E:\\GitHub\\sardineYJA.github.io\\_posts\\";

    @Test
    public void createIndex(){
        if (blogEsService.isIndexExists("blog")) {
            blogEsService.deleteIndex("blog");
        }
    }

    @Test
    public void importAllData(){    // 将所有blog导入ES
        List<String> markdownList = readMarkdownService.getMarkdownList(mdFilePath);
        int id = 1;
        List<BlogEntity> list = new ArrayList<>();
        for (String name : markdownList) {
            BlogEntity blog = readMarkdownService.getBlogEntityByParseFile(mdFilePath, name);
            blog.setId(Integer.toString(id));
            list.add(blog);
            id = id + 1;
        }
        System.out.println(list.size());
        blogEsService.insertBath("blog", list);
    }
}
