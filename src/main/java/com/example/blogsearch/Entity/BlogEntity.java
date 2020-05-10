package com.example.blogsearch.Entity;

import lombok.Data;

import java.io.Serializable;

// Blog实体类
@Data
public class BlogEntity implements Serializable {
    private String id;             // keyword
    private String title;          // text
    private String createDate;     // date
    private String tag;            // keyword
    private String content;        // text
}
