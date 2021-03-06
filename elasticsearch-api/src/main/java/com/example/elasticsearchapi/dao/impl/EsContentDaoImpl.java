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
        // ??????GetRequest
        GetRequest getRequest = new GetRequest()
                .index(index)
                .routing(routing)
                .id(id);
        // ?????????????????????GetResponse
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);

        // ?????????????????????????????????
        ContentDTO contentDTO = null;
        if (getResponse.isExists()) {
            contentDTO = JSON.parseObject(getResponse.getSourceAsString(), ContentDTO.class);
            contentDTO.setId(getResponse.getId());
        }
        return contentDTO;
    }

    @Override
    public List<ContentDTO> getContentByIds(String index, String routing, String[] ids) throws IOException {
        // ??????ids?????????MultiGetRequest.Item???????????????MultiGetRequest
        MultiGetRequest multiGetRequest = new MultiGetRequest();
        for (String id : ids) {
            MultiGetRequest.Item item = new MultiGetRequest.Item(index, id).routing(routing);
            multiGetRequest.add(item);
        }

        // ?????????????????????MultiGetResponse
        MultiGetResponse multiGetResponse = restHighLevelClient.mget(multiGetRequest, RequestOptions.DEFAULT);
        // ???????????????????????????
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
        // ??????BoolQueryBuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (String key : conditionMap.keySet()) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(key, conditionMap.get(key)));
        }

        // ??????SearchRequest
        SearchRequest searchRequest = new SearchRequest()
                .indices(index)
                .routing(routing)
                .source(new SearchSourceBuilder().query(boolQueryBuilder));

        // ?????????????????????SearchResponse
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // ??????????????????
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
        // ??????DeleteRequest
        DeleteRequest deleteRequest = new DeleteRequest().index(index).routing(routing).id(id);
        // ?????????????????????DeleteResponse
        DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        // ??????????????????
        String deleted = "DELETED";
        if (deleted.equals(deleteResponse.getResult().toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean updateById(String index, String routing, String id, String source) throws IOException {
        // ??????UpdateRequest
        UpdateRequest updateRequest = new UpdateRequest()
                .index(index)
                .routing(routing)
                .id(id)
                .doc(source, XContentType.JSON);

        // ?????????????????????UpdateResponse
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        // ????????????????????????
        String updated = "UPDATED";
        if (updated.equals(updateResponse.getResult().toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean updateHotspot(String index, String routing, String id, Double hotspot) throws IOException {
        // ??????UpdateRequest
        UpdateRequest updateRequest = new UpdateRequest()
                .index(index)
                .routing(routing)
                .id(id)
                .doc("hotspot", hotspot);

        // ?????????????????????UpdateResponse
        UpdateResponse updateResponse = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        // ????????????????????????
        String updated = "UPDATED";
        if (updated.equals(updateResponse.getResult().toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean insertBatch(String index, String routing, List<ContentDTO> contentDTOs) throws IOException {
        // ?????????????????????BulkRequest
        BulkRequest bulkRequest = new BulkRequest();
        for (ContentDTO contentDTO : contentDTOs) {
            // ??????id
            if (contentDTO.getId() == null || contentDTO.getId().isEmpty()) {
                contentDTO.setId(UUID.randomUUID().toString().replace("-", ""));
            }
            // ??????IndexRequest
            IndexRequest indexRequest = new IndexRequest()
                    .index(index)
                    .routing(routing)
                    .id(contentDTO.getId()).opType(DocWriteRequest.OpType.CREATE)
                    .source(JSON.toJSONString(contentDTO), XContentType.JSON);

            bulkRequest.add(indexRequest);
        }

        // ????????????
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);

        // ????????????????????????
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

        // ??????HighlightBuilder
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        // ??????????????????
        highlightBuilder.field(new HighlightBuilder.Field(titleTextFiled));
        highlightBuilder.field(new HighlightBuilder.Field(contentTextField));
        // ????????????????????????
        highlightBuilder.preTags(preTags);
        highlightBuilder.postTags(postTags);

        // ??????BoolQueryBuilder
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (map != null) {
            for (String key : map.keySet()) {
                MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(key, map.get(key));
                boolQueryBuilder.must(matchQueryBuilder);
            }
        }

        // ?????????????????????????????????????????????
        if (condition != null && !condition.isEmpty()) {
            // ?????????(titleText)?????????(contentText)???????????????(condition)
            MultiMatchQueryBuilder multiMatchQueryBuilder =
                    QueryBuilders.multiMatchQuery(condition, titleTextFiled, contentTextField).operator(Operator.OR);
            // ????????????
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }

        // ??????SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // ??????????????????
        searchSourceBuilder.highlighter(highlightBuilder);
        // ??????????????????
        searchSourceBuilder.from((pageNum - 1) * pageSize);
        // ??????????????????
        searchSourceBuilder.size(pageSize);
        // ?????????????????????60s
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        // ??????????????????
        searchSourceBuilder.query(boolQueryBuilder);

        // ?????????????????????
        SortOrder sortOrder = SortOrder.DESC;
        // ??????????????????????????????
        if (orderFieldType != null && orderFieldType.length > 0) {
            for (String item : orderFieldType) {
                String orderField = item.split(":")[0];
                String order = item.split(":")[1];
                // ?????????????????????????????????
                if ("asc".equals(order)) {
                    sortOrder = SortOrder.ASC;
                }
                // ??????????????????
                if ("hotspot".equals(orderField)) {
                    searchSourceBuilder.sort(new FieldSortBuilder(orderField).order(sortOrder));
                } else {
                    searchSourceBuilder.sort(new FieldSortBuilder(orderField + ".keyword").order(sortOrder));
                }
            }
        } else {
            // ??????createTime??????
            searchSourceBuilder.sort(defaultSortField + ".keyword", SortOrder.DESC);
        }

        // ??????SearchRequest
        SearchRequest searchRequest = new SearchRequest()
                .indices(index)
                .routing(routing)
                .source(searchSourceBuilder);


        // ????????????
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits searchHits = searchResponse.getHits();
        SearchHit[] hits = searchHits.getHits();

        // ??????????????????
        List<ContentDTO> resultContentDTO = new ArrayList<>();

        // ??????????????????????????????????????????
        for (SearchHit hit : hits) {
            String id = hit.getId();
            String source = hit.getSourceAsString();
            // ??????????????????????????????
            ContentDTO searchContentDTO = JSON.parseObject(source, ContentDTO.class);
            // ??????id??????ES???ID????????????
            searchContentDTO.setId(id);
            // ?????????????????????
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            // ?????????????????????
            HighlightField hlTitle = highlightFields.get(titleTextFiled);
            if (hlTitle != null) {
                String titleText = "";
                Text[] fragments = hlTitle.fragments();
                for (Text text : fragments) {
                    titleText += text;
                }
                // ??????????????????
                searchContentDTO.setTitleText(titleText);
            }
            // ?????????????????????
            HighlightField cntTitle = highlightFields.get(contentTextField);
            if (cntTitle != null) {
                String contentText = "";
                Text[] fragments = cntTitle.fragments();
                for (Text text : fragments) {
                    contentText += text;
                }
                // ??????????????????
                searchContentDTO.setContentText(contentText);
            }
            resultContentDTO.add(searchContentDTO);
        }

        return resultContentDTO;
    }

}
