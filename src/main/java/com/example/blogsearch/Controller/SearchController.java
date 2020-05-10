package com.example.blogsearch.Controller;

import com.example.blogsearch.Entity.BlogEntity;
import com.example.blogsearch.Service.BlogEsService;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
@RequestMapping("blog")
public class SearchController {

    @Autowired
    BlogEsService blogEsService;

    @RequestMapping("index")
    public String index(Model model) {
        List<BlogEntity> blogList = blogEsService.searchAll("blog", BlogEntity.class);
        model.addAttribute("blogs", blogList);
        return "/index";    // 跳转页面：index.html
    }

    @GetMapping("/{id}")
    public String searchById(@RequestParam(value = "id") String id, Model model) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.termQuery("id", id));
        List<BlogEntity> blogList = blogEsService.search("blog", builder, BlogEntity.class);
        model.addAttribute("blogs", blogList);
        return "/details";    // 跳转页面：details.html?id=XX
    }

    @GetMapping("/searchByKeyword")
    public String searchByKeyword(@RequestParam(value = "keyword") String keyword, Model model) {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.matchQuery("content", keyword));
        List<BlogEntity> blogList = blogEsService.search("blog", builder, BlogEntity.class);
        model.addAttribute("blogs", blogList);
        return "/index";    // 跳转页面：details.html?id=XX
    }

}
