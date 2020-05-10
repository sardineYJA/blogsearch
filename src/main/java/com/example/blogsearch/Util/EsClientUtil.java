package com.example.blogsearch.Util;

import lombok.Data;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.Serializable;


// es客户端单例模式
@Data
@Service
@ConfigurationProperties(prefix = "es.search", ignoreInvalidFields=false, ignoreUnknownFields=true)
public class EsClientUtil implements Serializable {
    private RestClientBuilder restClientBuilder;
    private RestHighLevelClient client;

    private String node;
    private int port;
    private int connectTimeOut;
    private int socketTimeOut;
    private int connectRequestTimeOut;
    private int maxConnectNum;
    private int maxConnectPerRoute;
    private boolean uniqueConnectTimeConfig;
    private boolean uniqueConnectNumConfig;

    // 双重校验模式
    public RestHighLevelClient getClient() {
        if (client == null) {
            synchronized (RestHighLevelClient.class) {
                if (client == null) {
                    buildClient();
                }
            }
        }
        return client;
    }

    private void buildClient() {
        restClientBuilder = RestClient.builder(new HttpHost(node, port, "http"));
        if (uniqueConnectTimeConfig) {
            setConnectTimeOutConfig();
        }
        if (uniqueConnectNumConfig) {
            setMultiConnectConfig();
        }
        client = new RestHighLevelClient(restClientBuilder);
    }

    // 异步httpclient的连接延时配置
    private void setConnectTimeOutConfig() {
        restClientBuilder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder builder) {
                builder.setConnectTimeout(connectTimeOut);
                builder.setSocketTimeout(socketTimeOut);
                builder.setConnectionRequestTimeout(connectRequestTimeOut);
                return builder;
            }
        });
    }

    // 异步httpclient的连接数配置
    private void setMultiConnectConfig() {
        restClientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
            @Override
            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                httpAsyncClientBuilder.setMaxConnTotal(maxConnectNum);
                httpAsyncClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
                return httpAsyncClientBuilder;
            }
        });
    }
}
