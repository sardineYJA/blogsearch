package com.example.blogsearch.Service;

import com.example.blogsearch.Entity.BlogEntity;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

// 读取Markdown文件
@Service
public class ReadMarkdownService {

    // 读取目录得到所有md文件名
    public List<String> getMarkdownList(String mdFilePath) {
        List<String> list = new ArrayList<>();
        System.out.println(mdFilePath);
        File[] listFiles = new File(mdFilePath).listFiles();
        for (File file : listFiles) {
            list.add(file.getName());
        }
        return list;
    }

    // 解析md文件获取BlogEntity实体类
    public BlogEntity getBlogEntityByParseFile(String mdFilePath, String fileName) {
        StringBuilder title = new StringBuilder();
        StringBuilder createDate = new StringBuilder();
        StringBuilder tag = new StringBuilder();
        StringBuilder content = new StringBuilder();
        BlogEntity blogEntity = new BlogEntity();

        try {
            BufferedReader br = new BufferedReader(new FileReader(mdFilePath+fileName));
            String s = null;
            int rowNum = 0;
            while((s=br.readLine()) != null) {
                rowNum++;
                if (rowNum == 3) {
                    title.append(s.split(":")[1].replace("\"","").trim());
                }
                if (rowNum == 4) {
                    createDate.append(s.split(":")[1].trim());
                }
                if (rowNum == 6) {
                    tag.append(s.split(":")[1].trim());
                }
                if (rowNum > 8) {
                    if (!s.trim().equals("")) {
                        content.append(System.lineSeparator()+s.trim());
                    }

                }
            }
            blogEntity.setTitle(title.toString());
            blogEntity.setCreateDate(createDate.toString());
            blogEntity.setTag(tag.toString());
            blogEntity.setContent(content.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return blogEntity;
    }
}
