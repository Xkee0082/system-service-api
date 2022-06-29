package com.example.elasticsearchapi.controller;

import com.example.elasticsearchapi.entity.ContentDTO;
import com.example.elasticsearchapi.entity.ContentQueryParam;
import com.example.elasticsearchapi.entity.ResultDTO;
import com.example.elasticsearchapi.service.IContentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Author: xu_ke
 * @Date: 2022-06-26 21:42
 * @description TODO
 */
@Api(tags = "文章查询")
@RestController
@RequestMapping("/content")
public class ContentSearchController {

    @Autowired
    private IContentService contentService;

    /**
     * 批量添加
     */
    @ApiOperation(value = "批量添加数据")
    @PostMapping("/insertBatch")
    ResultDTO insertBatch(String index, String routing, @RequestBody List<ContentDTO> contentDTOs) {
        return contentService.insertBatch(index, routing, contentDTOs);
    }

    /**
     * 单条数据添加
     */
    @ApiOperation(value = "单条数据添加")
    @PostMapping("/insert")
    ResultDTO<ContentDTO> insert(String index, String routing, @RequestBody ContentDTO contentDTO) {
        return contentService.insert(index, routing, contentDTO);
    }

    /**
     * 根据ID，更新数据
     */
    @ApiOperation(value = "根据ID，更新数据")
    @PostMapping("/updateById")
    ResultDTO<ContentDTO> updateById(String index, String routing, @RequestBody ContentDTO contentDTO) {
        return contentService.updateById(index, routing, contentDTO);
    }

    /**
     * 根据ID，更新hotspot
     */
    @ApiOperation(value = "根据ID，更新hotspot")
    @GetMapping("/updateHotspotById")
    ResultDTO updateHotspotById(String index, String routing, String id, Double hotspot) {
        return contentService.updateHotspotById(index, routing, id, hotspot);
    }

    /**
     * 根据联合查询条件Map，更新hotspot
     */
    @ApiOperation(value = "根据联合查询条件Map，更新hotspot")
    @PostMapping("/updateHotspotByConditionMap")
    ResultDTO updateHotspotByConditionMap(String index, String routing, @RequestBody Map<String, String> conditionMap, Double hotspot) {
        return contentService.updateHotspotByConditionMap(index, routing, conditionMap, hotspot);
    }

    /**
     * 根据ID，删除数据
     */
    @ApiOperation(value = "根据ID，删除数据")
    @DeleteMapping("/deleteById")
    ResultDTO deleteById(String index, String routing, String id) {
        return contentService.deleteById(index, routing, id);
    }

    /**
     * 根据联合查询条件，删除数据
     */
    @ApiOperation(value = "根据联合查询条件，删除数据")
    @DeleteMapping("/deleteByConditionMap")
    ResultDTO deleteByConditionMap(String index, String routing, @RequestBody Map<String, String> conditionMap) {
        return contentService.deleteByConditionMap(index, routing, conditionMap);
    }

    /**
     * 根据ID，查询数据
     */
    @ApiOperation(value = "根据ID，查询数据")
    @GetMapping("/getContentById")
    ResultDTO<ContentDTO> getContentById(String index, String routing, String id) {
        return contentService.getContentById(index, routing, id);
    }

    /**
     * 根据ID列表，查询数据集合
     */
    @ApiOperation(value = "根据ID列表，查询数据集合")
    @GetMapping("/getDataByIds")
    ResultDTO<List<ContentDTO>> getDataByIds(String index, String routing, String[] ids) {
        return contentService.getDataByIds(index, routing, ids);
    }

    /**
     * 根据联合查询条件Map，查询数据集合
     */
    @ApiOperation(value = "根据联合查询条件Map，查询数据集合")
    @PostMapping("/getDataByConditionMap")
    ResultDTO<ContentDTO> getDataByConditionMap(String index, String routing, @RequestBody Map<String, String> conditionMap) {
        return contentService.getDataByConditionMap(index, routing, conditionMap);
    }

    /**
     * 根据条件分页检索数据
     */
    @ApiOperation(value = "根据条件分页检查数据")
    @PostMapping("/search")
    ResultDTO<List<ContentDTO>> search(@RequestBody ContentQueryParam contentQueryParam) {

        String index = contentQueryParam.getIndex();
        String routing = contentQueryParam.getRouting();
        int pageNum = contentQueryParam.getPageNum();
        int pageSize = contentQueryParam.getPageSize();
        Map<String, Object> condition = contentQueryParam.getCondition();
        String keyword = contentQueryParam.getKeyword();
        List<String> orderFiledType = contentQueryParam.getOrderFiledType();

        return contentService.search(index, routing, pageNum, pageSize, condition, keyword, orderFiledType);
    }
}
