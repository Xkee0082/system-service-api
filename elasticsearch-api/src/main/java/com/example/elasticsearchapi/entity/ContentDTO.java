package com.example.elasticsearchapi.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: xu_ke
 * @Date: 2022-06-21 20:19
 * @description TODO ES数据对象
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@ApiModel("文章实体类")
public class ContentDTO {
    /**
     * 主键ID，uuid
     */
    @ApiModelProperty(value = "id", example = "K4jdg4EBzkf9KQ0B1xKS")
    private String id;
    /**
     * 平台ID
     */
    @ApiModelProperty(value = "平台ID", example = "d763c2911aff42c8b45dcebbdcff0592")
    private String platformId;
    /**
     * 数据ID
     */
    @ApiModelProperty(value = "数据ID", example = "a95da8b8b63c451f9fdcc340e14ca6a1")
    private String dataId;
    /**
     * 标签
     */
    @ApiModelProperty(value = "标签", example = "finance")
    private String labelText;
    /**
     * 标题
     */
    @ApiModelProperty(value = "标题", example = "阿里云原生技术公开课")
    private String titleText;
    /**
     * 文章内容，分词
     */
    @ApiModelProperty(value = "文章内容", example = "")
    private String contentText;
    /**
     * 热度值
     */
    @ApiModelProperty(value = "热度值", example = "8.5")
    private Double hotspot;
    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间", example = "2022-06-20 11:39:54")
    private String createTime;
}
