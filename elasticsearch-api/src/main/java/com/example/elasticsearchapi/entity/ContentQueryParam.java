package com.example.elasticsearchapi.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @Author: xu_ke
 * @Date: 2022-06-29 21:02
 * @description TODO 分页检索参数对象
 */
@Data
@ApiModel("分页检索参数对象")
public class ContentQueryParam {

    /**
     * 索引名
     */
    @ApiModelProperty(value = "索引名", example = "index_es")
    private String index;
    /**
     * 路由
     */
    @ApiModelProperty(value = "路由", example = "DOS_TENACY_ID")
    private String routing;
    /**
     * 页码，从1开始
     */
    @ApiModelProperty(value = "页码", example = "3")
    private int pageNum = 1;
    /**
     * 页长度，默认10条数据
     */
    @ApiModelProperty(value = "页长度", example = "10")
    private int pageSize = 10;
    /**
     * 隐含检索条件，例：标签labelText等
     */
    @ApiModelProperty(value = "隐含检索条件，例：标签labelText等")
    private Map<String, Object> condition;
    /**
     * 用户搜索的关键词
     */
    @ApiModelProperty(value = "用户搜索的关键词", example = "科技产能")
    private String keyword;
    /**
     * 字段和排序，格式为 createTime:asc
     */
    @ApiModelProperty(value = "字段和排序", example = "createTime:asc")
    private List<String> orderFiledType;
}
