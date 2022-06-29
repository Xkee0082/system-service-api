package com.example.elasticsearchapi.service;

import com.example.elasticsearchapi.entity.ContentDTO;
import com.example.elasticsearchapi.entity.ResultDTO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author: xu_ke
 * @Date: 2022-06-21 20:53
 * @description TODO ES检索服务接口
 */
public interface IContentService {

    /**
     * 批量存储数据
     *
     * @param index 索引名称
     * @param routing 路由
     * @param contentDTOs 数据集合
     * @return
     */
    ResultDTO insertBatch(String index, String routing, List<ContentDTO> contentDTOs);

    /**
     * 单条存储数据
     *
     * @param index 索引名称
     * @param routing 路由
     * @param contentDTO 数据对象
     * @return
     */
    ResultDTO<ContentDTO> insert(String index, String routing, ContentDTO contentDTO);

    /**
     * 根据ID更新数据（检索成功，则更新；检索失败，则提示）
     *
     * @param index 索引名称
     * @param routing 路由
     * @param contentDTO 数据对象
     * @return
     */
    ResultDTO<ContentDTO> updateById(String index, String routing, ContentDTO contentDTO);

    /**
     * 根据ID更新数据的热度值
     *
     * @param index 索引名称
     * @param routing 路由
     * @param id ES的索引ID
     * @param hotspot 热度值
     * @return
     */
    ResultDTO updateHotspotById(String index, String routing, String id, Double hotspot);

    /**
     * 根据联合查询条件，查询对应数据，然后更新数据热度值
     * 注：查询结果唯一时，才更新；否则提示
     *
     * @param index 索引名称
     * @param routing 路由
     * @param conditionMap 联合查询条件，key为条件，value为值，构建查询条件
     * @param hotspot 热度值
     * @return
     */
    ResultDTO updateHotspotByConditionMap(String index, String routing, Map<String, String> conditionMap, Double hotspot);

    /**
     * 根据ES的索引ID删除数据
     *
     * @param index 索引名称
     * @param routing 路由
     * @param id ES的索引ID
     * @return
     */
    ResultDTO deleteById(String index, String routing, String id);

    /**
     * 根据联合查询条件，删除资源
     * 注：查询结果唯一时，才更新；否则提示
     *
     * @param index 索引名称
     * @param routing 路由
     * @param conditionMap 联合查询条件，key为条件，value为值，构建查询条件
     * @return
     */
    ResultDTO deleteByConditionMap(String index, String routing, Map<String, String> conditionMap);

    /**
     * 根据ES索引ID查询数据
     *
     * @param index 索引名称
     * @param routing 路由
     * @param id ES索引ID
     * @return
     */
    ResultDTO<ContentDTO> getContentById(String index, String routing, String id);

    /**
     * 根据ES索引ID列表查询数据集合
     *
     * @param index 索引名称
     * @param routing 路由
     * @param ids ES索引ID列表
     * @return
     */
    ResultDTO<List<ContentDTO>> getDataByIds(String index, String routing, String[] ids);

    /**
     * 根据联合查询条件，检索数据
     * 注：结果唯一，否则提示
     *
     * @param index 索引名称
     * @param routing 路由
     * @param conditionMap 联合查询条件，key为条件，value为值，构建查询条件
     * @return
     */
    ResultDTO<ContentDTO> getDataByConditionMap(String index, String routing, Map<String, String> conditionMap);

    /**
     *
     * 根据条件检索数据，实现分页和按条件排序
     *
     * @param index 索引名称
     * @param routing 路由
     * @param pageNum 页码，从1开始
     * @param pageSize 每页长度，默认10
     * @param condition 隐含检索条件，如 labelText:achievements
     * @param keyword 搜索条件
     * @param orderFieldType 排序字段和排序方式，如 createTime:asc
     * @return 成功返回资源数据列表，没有检索到数据返回空集合
     */
    ResultDTO<List<ContentDTO>> search(String index, String routing, int pageNum, int pageSize, Map<String, Object> condition,
                                       String keyword, List<String> orderFieldType);

}
