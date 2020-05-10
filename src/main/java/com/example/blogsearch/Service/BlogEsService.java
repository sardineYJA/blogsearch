package com.example.blogsearch.Service;

import com.alibaba.fastjson.JSON;
import com.example.blogsearch.Entity.BlogEntity;
import com.example.blogsearch.Util.EsClientUtil;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


// 操作ES函数
@Service
public class BlogEsService implements Serializable {

    @Autowired
    EsClientUtil esClientUtil;


    // 判断索引是否存在
    public boolean isIndexExists(String indexName) {
        RestHighLevelClient client = esClientUtil.getClient();
        boolean ack = false;
        try {
            GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
            getIndexRequest.humanReadable(true);
            ack = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ack;
    }

    // 删除索引
    public boolean deleteIndex(String indexName) {
        RestHighLevelClient client = esClientUtil.getClient();
        boolean ack = false;
        if (!isIndexExists(indexName)) {
            return ack;
        }
        try {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
            deleteIndexRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
            AcknowledgedResponse delete = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
            ack = delete.isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ack;
    }

    // 插入或更新一条记录
    public void insertOrUpdateOne(String index, BlogEntity entity) {
        RestHighLevelClient client = esClientUtil.getClient();
        IndexRequest request = new IndexRequest(index);
        request.id(entity.getId());      // request.id是_id字段，对象里面的id是_source字段里面又会创建id字段
        // request.routing("RouteName"); // 可以设置路由
        request.source(JSON.toJSONString(entity), XContentType.JSON);  // 需要转换成Json格式的字符串
        try {
            client.index(request, RequestOptions.DEFAULT);   // 插入更新操作
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 批量插入
    public void insertBath(String index, List<BlogEntity> list) {
        RestHighLevelClient client = esClientUtil.getClient();
        BulkRequest request = new BulkRequest();
        list.forEach(item -> request.add(
                new IndexRequest(index).id(item.getId()).source(JSON.toJSONString(item),XContentType.JSON)
        ));
        try {
            client.bulk(request, RequestOptions.DEFAULT);   // 批量插入
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 条件删除文档
    public void deleteByQuery(String index, QueryBuilder builder) {
        RestHighLevelClient client = esClientUtil.getClient();
        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        request.setQuery(builder);
        try {
            client.deleteByQuery(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 查询所有数据
    public <T> List<T> searchAll(String index, Class<T> c) {

        RestHighLevelClient client = esClientUtil.getClient();
        SearchRequest request = new SearchRequest(index);
        try {  // 这样只会获取10条，问题待处理
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            List<T> result = new ArrayList<>(hits.length);
            for (SearchHit hit: hits) {
                result.add(JSON.parseObject(hit.getSourceAsString(), c));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 条件查询
    public <T> List<T> search(String index, SearchSourceBuilder builder, Class<T> c) {
        RestHighLevelClient client = esClientUtil.getClient();
        SearchRequest request = new SearchRequest(index);
        request.source(builder);
        try {
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            List<T> result = new ArrayList<>(hits.length);
            for (SearchHit hit: hits) {
                result.add(JSON.parseObject(hit.getSourceAsString(), c));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 使用游标方式搜索获取
    public <T> List<T> scrollSearch(String index, SearchSourceBuilder builder, Long scrollTimeOut, Class<T> c) {
        RestHighLevelClient client = esClientUtil.getClient();
        SearchRequest request = new SearchRequest(index);
        request.source(builder);

        Scroll scroll = new Scroll(TimeValue.timeValueMillis(scrollTimeOut));  // 快照保存时间
        request.scroll(scroll);

        try {
            SearchResponse searchResponse = client.search(request, RequestOptions.DEFAULT);
            String scrollId = searchResponse.getScrollId();

            SearchHit[] hits = searchResponse.getHits().getHits();
            List<T> result = new ArrayList<>();
            while (hits.length != 0) {
                for (SearchHit hit: hits) {
                    result.add(JSON.parseObject(hit.getSourceAsString(), c));
                }
                SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId);
                searchScrollRequest.scroll(scroll);

                SearchResponse response = client.scroll(searchScrollRequest, RequestOptions.DEFAULT);
                scrollId = response.getScrollId();
                hits = response.getHits().getHits();
            }

            // 及时清理es快照，释放资源
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);

            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



}
