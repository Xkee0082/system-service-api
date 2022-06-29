package com.example.elasticsearchapi.dao.impl;

import com.alibaba.fastjson.JSON;
import com.example.elasticsearchapi.dao.IEsContentDao;
import com.example.elasticsearchapi.entity.ContentDTO;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.*;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: xu_ke
 * @Date: 2022-06-25 8:41
 * @description TODO
 */
@Repository
public class EsContentDaoImpl implements IEsContentDao {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public ContentDTO getContentById(String index, String routing, String id) throws IOException {
        // 构建GetRequest
        GetRequest getRequest = new GetRequest()
                .index(index)
                .routing(routing)
                .id(id);
        // 执行查询，获取GetResponse
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);

        // 判断查询结果，封装数据
        ContentDTO contentDTO = null;
        if (getResponse.isExists()) {
            contentDTO = JSON.parseObject(getResponse.getSourceAsString(), ContentDTO.class);
            contentDTO.setId(getResponse.getId());
        }
        return contentDTO;
    }

    @Override
    public List<ContentDTO> getContentByIds(String index, String routing, String[] ids) throws IOException {
        // 遍历ids，创建MultiGetRequest.Item，并添加到MultiGetRequest
        MultiGetRequest multiGetRequest = new MultiGetRequest();
        for (String id : ids) {
            MultiGetRequest.Item item = new MultiGetRequest.Item(index, id).routing(routing);
            multiGetRequest.add(item);
        }

        // 执行查询，获取MultiGetResponse
        MultiGetResponse multiGetResponse = restHighLevelClient.mget(multiGetRequest, RequestOptions.DEFAULT);
        // 获取结果，封装集合
        List<ContentDTO> contentDTOs = new ArrayList<>();
        for (MultiGetItemResponse multiGetItemResponse : multiGetResponse) {
            String source = multiGetItemResponse.getResponse().getSourceAsString();
            if (source != null) {
                ContentDTO contentDTO = JSON.parseObject(source, ContentDTO.class);
                contentDTO.setId(multiGetItemResponse.getResponse().getId());
                contentDTOs.add(contentDTO);
            }
        }
        return contentDTOs;
    }

    @Override
    public List<ContentDTO> getContentByConditionMap(String index, String routing, Map<String, String> conditionMap) throws IOException {
        // 构建BoolQueryBuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (String key : conditionMap.keySet()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(key, conditionMap.get(key)));
        }

        // 构建SearchRequest
        SearchRequest searchRequest = new SearchRequest()
                .indices(index)
                .routing(routing)
                .source(new SearchSourceBuilder().query(boolQueryBuilder));

        // 执行检索，获取SearchResponse
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // 获取结果封装
        List<ContentDTO> contentDTOs = new ArrayList<>();
        SearchHits searchHits = searchResponse.getHits();
        if (searchHits.getTotalHits().value > 0) {
            SearchHit[] hits = searchHits.getHits();
            for (SearchHit hit : hits) {
                ContentDTO contentDTO = JSON.parseObject(hit.getSourceAsString(), ContentDTO.class);
                contentDTO.setId(hit.getId());
                contentDTOs.add(contentDTO);
            }
        }
        return contentDTOs;
    }

    @Override
    public Boolean deleteById(String index, String routing, String id) throws IOException {
        // 构建DeleteRequest
        DeleteRequest deleteRequest = new DeleteRequest().index(index).routing(routing).id(id);
        // 执行删除，获取DeleteResponse
        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        // 判断结果返回
        String deleted = "DELETED";
        if (deleted.equals(deleteResponse.getResult().toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean updateById(String index, String routing, String id, String source) throws IOException {
        // 构建UpdateRequest
        UpdateRequest updateRequest = new UpdateRequest()
                .index(index)
                .routing(routing)
                .id(id)
                .doc(source, XContentType.JSON);

        // 执行更新，获取UpdateResponse
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        // 判断更新是否成功
        String updated = "UPDATED";
        if (updated.equals(updateResponse.getResult().toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean updateHotspot(String index, String routing, String id, Double hotspot) throws IOException {
        // 构建UpdateRequest
        UpdateRequest updateRequest = new UpdateRequest()
                .index(index)
                .routing(routing)
                .id(id)
                .doc("hotspot", hotspot);

        // 执行更新，获取UpdateResponse
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        // 判断更新是否成功
        String updated = "UPDATED";
        if (updated.equals(updateResponse.getResult().toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean insertBatch(String index, String routing, List<ContentDTO> contentDTOs) throws IOException {
        // 遍历集合，构建BulkRequest
        BulkRequest bulkRequest = new BulkRequest();
        for (ContentDTO contentDTO : contentDTOs) {
            // 判断id
            if (contentDTO.getId() == null || contentDTO.getId().isEmpty()) {
                contentDTO.setId(UUID.randomUUID().toString().replace("-", ""));
            }
            // 构建IndexRequest
            IndexRequest indexRequest = new IndexRequest()
                    .index(index)
                    .routing(routing)
                    .id(contentDTO.getId()).opType(DocWriteRequest.OpType.CREATE)
                    .source(JSON.toJSONString(contentDTO), XContentType.JSON);

            bulkRequest.add(indexRequest);
        }

        // 执行插入
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

        // 判断插入是否成功
        if (!bulkResponse.hasFailures()) {
            return true;
        } else {
            return false;
        }
    }

    private String titleTextFiled = "titleText";
    private String contentTextField = "contentText";
    private String preTags = "<span style=color:#ff4c29>";
    private String postTags = "</span>";
    private String defaultSortField = "createTime";


    @Override
    public List<ContentDTO> search(String index, String routing, int pageNum, int pageSize, Map<String, Object> map, String condition, String... orderFieldType) throws IOException {

        // 构建HighlightBuilder
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        // 设置高亮属性
        highlightBuilder.field(new HighlightBuilder.Field(titleTextFiled));
        highlightBuilder.field(new HighlightBuilder.Field(contentTextField));
        // 设置高亮前后标签
        highlightBuilder.preTags(preTags);
        highlightBuilder.postTags(postTags);

        // 构建BoolQueryBuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (map != null) {
            for (String key : map.keySet()) {
                MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(key, map.get(key));
                boolQueryBuilder.must(matchQueryBuilder);
            }
        }

        // 若检索条件不为空，设置检索匹配
        if (condition != null && !condition.isEmpty()) {
            // 从标题(titleText)和文本(contentText)中检索条件(condition)
            MultiMatchQueryBuilder multiMatchQueryBuilder =
                    QueryBuilders.multiMatchQuery(condition, titleTextFiled, contentTextField).operator(Operator.OR);
            // 添加条件
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }

        // 构建SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 添加高亮设置
        searchSourceBuilder.highlighter(highlightBuilder);
        // 设置起始位置
        searchSourceBuilder.from((pageNum - 1) * pageSize);
        // 设置检索条数
        searchSourceBuilder.size(pageSize);
        // 设置超时时间，60s
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        // 设置查询条件
        searchSourceBuilder.query(boolQueryBuilder);

        // 默认排序，降序
        SortOrder sortOrder = SortOrder.DESC;
        // 判断排序字段，并处理
        if (orderFieldType != null && orderFieldType.length > 0) {
            for (String item : orderFieldType) {
                String orderField = item.split(":")[0];
                String order = item.split(":")[1];
                // 如果为升序，则重新设置
                if ("asc".equals(order)) {
                    sortOrder = SortOrder.ASC;
                }
                // 设置排序字段
                if ("hotspot".equals(orderField)) {
                    searchSourceBuilder.sort(new FieldSortBuilder(orderField).order(sortOrder));
                } else {
                    searchSourceBuilder.sort(new FieldSortBuilder(orderField + ".keyword").order(sortOrder));
                }
            }
        } else {
            // 默认createTime降序
            searchSourceBuilder.sort(defaultSortField + ".keyword", SortOrder.DESC);
        }

        // 构建SearchRequest
        SearchRequest searchRequest = new SearchRequest()
                .indices(index)
                .routing(routing)
                .source(searchSourceBuilder);


        // 执行查询
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();

        // 返回数据对象
        List<ContentDTO> resultContentDTO = new ArrayList<>();

        // 对检索记录遍历，封装返回对象
        for (SearchHit hit : hits) {
            String id = hit.getId();
            String source = hit.getSourceAsString();
            // 将检索字符串转成对象
            ContentDTO searchContentDTO = JSON.parseObject(source, ContentDTO.class);
            // 设置id，与ES的ID保持一致
            searchContentDTO.setId(id);
            // 对高亮字段处理
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            // 对标题高亮处理
            HighlightField hlTitle = highlightFields.get(titleTextFiled);
            if (hlTitle != null) {
                String titleText = "";
                Text[] fragments = hlTitle.fragments();
                for (Text text : fragments) {
                    titleText += text;
                }
                // 设置到对象中
                searchContentDTO.setTitleText(titleText);
            }
            // 对文章高亮处理
            HighlightField cntTitle = highlightFields.get(contentTextField);
            if (cntTitle != null) {
                String contentText = "";
                Text[] fragments = cntTitle.fragments();
                for (Text text : fragments) {
                    contentText += text;
                }
                // 设置到对象中
                searchContentDTO.setContentText(contentText);
            }
            resultContentDTO.add(searchContentDTO);
        }

        return resultContentDTO;
    }

}
