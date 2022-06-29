package com.example.elasticsearchapi.dao;

import com.example.elasticsearchapi.entity.ContentDTO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author: xu_ke
 * @Date: 2022-06-25 8:12
 * @description TODO ES检索服务接口
 */
public interface IEsContentDao {

    /**
     * 根据ES索引ID，检索数据
     *
     * @param index 索引名称
     * @param routing 路由
     * @param id ES索引ID
     * @return 成功返回数据，失败返回null
     */
    ContentDTO getContentById(String index, String routing, String id) throws IOException;

    /**
     * 根据ES索引ID列表，检索数据
     *
     * @param index 索引名称
     * @param routing 路由
     * @param ids ES索引ID列表
     * @return 成功返回数据集合，失败返回空集合
     */
    List<ContentDTO> getContentByIds(String index, String routing, String[] ids) throws IOException;

    /**
     * 根据联合查询条件，检索数据
     *
     * @param index 索引名称
     * @param routing 路由
     * @param conditionMap 联合查询条件，key为条件，value为值，构建查询条件
     * @return 成功返回数据对象，失败返回空集合
     */
    List<ContentDTO> getContentByConditionMap(String index, String routing, Map<String, String> conditionMap) throws IOException;

    /**
     * 根据ES的索引ID，删除数据
     *
     * @param index 索引名称
     * @param routing 路由
     * @param id ES的索引ID
     * @return 成功返回true，失败返回false
     */
    Boolean deleteById(String index, String routing, String id) throws IOException;

    /**
     * 根据ES索引ID，更新数据
     *
     * @param index 索引名称
     * @param routing 路由
     * @param id ES索引ID
     * @param source JSON数据
     * @return 成功返回true，失败返回false
     */
    Boolean updateById(String index, String routing, String id, String source) throws IOException;

    /**
     * 根据ID，更新热度值（只针对hotspot字段更新）
     *
     * @param index 索引名称
     * @param routing 路由
     * @param id ES索引ID
     * @param hotspot 热度值
     * @return 成功返回true，失败返回false
     */
    Boolean updateHotspot(String index, String routing, String id, Double hotspot) throws IOException;

    /**
     * 批量插入数据
     * 注：数据中获取的id为空，则由ES自动生成；否则使用数据的id作为ES索引id
     *
     * @param index 索引名称
     * @param routing 路由
     * @param contentDTOs 数据集合
     * @return 成功返回true，失败返回false
     */
    Boolean insertBatch(String index, String routing, List<ContentDTO> contentDTOs) throws IOException;

    /**
     * 根据条件检索数据，实现分页和按条件排序
     *
     * @param index 索引名称
     * @param routing 路由
     * @param pageNum 页码，从1开始
     * @param pageSize 每页长度，默认10
     * @param map 隐含检索条件，如 labelText:achievements
     * @param condition 搜索条件
     * @param orderFieldType 排序字段和排序方式，如 createTime:asc
     * @return
     */
    List<ContentDTO> search(String index, String routing, int pageNum, int pageSize, Map<String, Object> map, String condition, String... orderFieldType) throws IOException;
}
