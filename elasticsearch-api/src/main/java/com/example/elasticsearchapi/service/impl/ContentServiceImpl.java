package com.example.elasticsearchapi.service.impl;

import com.alibaba.fastjson.JSON;
import com.example.elasticsearchapi.constant.ResultCodeEnum;
import com.example.elasticsearchapi.dao.IEsContentDao;
import com.example.elasticsearchapi.dao.impl.EsContentDaoImpl;
import com.example.elasticsearchapi.entity.ContentDTO;
import com.example.elasticsearchapi.entity.ResultDTO;
import com.example.elasticsearchapi.service.IContentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @Author: xu_ke
 * @Date: 2022-06-21 21:19
 * @description TODO
 */
@Service
@Slf4j
public class ContentServiceImpl implements IContentService {

    @Autowired
    IEsContentDao esContentDao;

    @Override
    public ResultDTO insertBatch(String index, String routing, List<ContentDTO> contentDTOs) {
        log.info("\r\ninsertBatch 执行 ... ... " + index + ", " + routing + ", " + JSON.toJSONString(contentDTOs));

        // 判断数据集合是否为空
        if (contentDTOs == null || contentDTOs.size() <= 0) {
            return ResultDTO.failed("数据集合不能为空");
        }

        try {
            // 调用DAO批量插入数据
            Boolean insertBatch = esContentDao.insertBatch(index, routing, contentDTOs);
            // 判断插入是否成功
            if (insertBatch) {
                return ResultDTO.success("批量插入数据成功");
            } else {
                return ResultDTO.failed("批量插入数据失败");
            }
        } catch (IOException e) {
            log.error("插入数据异常：", e);
            return ResultDTO.failed("插入数据异常：" + e);
        }
    }

    @Override
    public ResultDTO<ContentDTO> insert(String index, String routing, ContentDTO contentDTO) {
        ResultDTO<ContentDTO> resultDTO = this.insertBatch(index, routing, Arrays.asList(contentDTO));
        if (resultDTO.getCode() == ResultCodeEnum.SUCCESS.getCode()) {
            resultDTO.setMessage("数据插入成功");
            resultDTO.setData(contentDTO);
        } else {
            resultDTO.setMessage("插入数据失败");
        }
        return resultDTO;
    }

    @Override
    public ResultDTO<ContentDTO> updateById(String index, String routing, ContentDTO contentDTO) {
        // 判断id是否为空
        if (contentDTO.getId() == null || contentDTO.getId().isEmpty()) {
            return ResultDTO.failed("id不能为空");
        }

        try {
            // 调用DAO更新数据
            Boolean updateById = esContentDao.updateById(index, routing, contentDTO.getId(), JSON.toJSONString(contentDTO));
            if (updateById) {
                return new ResultDTO<>(
                        ResultCodeEnum.SUCCESS.getCode(),
                        ResultCodeEnum.SUCCESS.getMessage(),
                        contentDTO
                );
            } else {
                return ResultDTO.failed("更新数据失败");
            }

        } catch (IOException e) {
            log.error("更新数据异常：", e);
            return ResultDTO.failed("更新数据异常：" + e);
        }
    }

    @Override
    public ResultDTO updateHotspotById(String index, String routing, String id, Double hotspot) {
        // id不能为空
        if (id == null || id.isEmpty()) {
            return ResultDTO.failed("id不能为空");
        }

        ContentDTO contentById;
        try {
            // 根据id查询出对应数据
            contentById = esContentDao.getContentById(index, routing, id);
            // 未查询到数据，返回
            if (contentById == null) {
                return ResultDTO.failed("根据id未检索到数据");
            }
        } catch (IOException e) {
            log.error("检索数据异常：", e);
            return ResultDTO.failed("检索数据异常：" + e);
        }

        // hotspot更新前后相同，直接返回成功
        if (hotspot.equals(contentById.getHotspot())) {
            return ResultDTO.success("数据更新成功");
        }
        // 设置hotspot，并调用DAO更新
        contentById.setHotspot(hotspot);

        try {
            Boolean updateHotspot = esContentDao.updateHotspot(index, routing, contentById.getId(), hotspot);
            if (updateHotspot) {
                return new ResultDTO<ContentDTO>(
                        ResultCodeEnum.SUCCESS.getCode(),
                        ResultCodeEnum.SUCCESS.getMessage(),
                        contentById
                );
            } else {
                return ResultDTO.failed("更新失败");
            }
        } catch (IOException e) {
            log.error("更新数据异常：", e);
            return ResultDTO.failed("更新数据异常：" + e);
        }
    }

    @Override
    public ResultDTO updateHotspotByConditionMap(String index, String routing, Map<String, String> conditionMap, Double hotspot) {
        // 判断map是否为空
        if (conditionMap == null || conditionMap.isEmpty()) {
            return ResultDTO.failed("Map不能为空");
        }

        List<ContentDTO> contentByConditionMap;
        try {
            // 调用DAO，查询结果
            contentByConditionMap = esContentDao.getContentByConditionMap(index, routing, conditionMap);
        } catch (IOException e) {
            log.error("查询数据异常：", e);
            return ResultDTO.failed("查询数据异常：" + e);
        }

        // 判断结果是否唯一
        if (contentByConditionMap.size() == 1) {

            ContentDTO contentDTO = contentByConditionMap.get(0);

            // hotspot更新前后相同，直接返回成功
            if (hotspot.equals(contentDTO.getHotspot())) {
                return ResultDTO.success("数据更新成功");
            }
            // 设置hotspot，并调用DAO更新
            contentDTO.setHotspot(hotspot);

            try {
                Boolean updateHotspot = esContentDao.updateHotspot(index, routing, contentDTO.getId(), hotspot);

                if (updateHotspot) {
                    return new ResultDTO(ResultCodeEnum.SUCCESS.getCode(), "更新数据成功", contentDTO);
                } else {
                    return new ResultDTO(ResultCodeEnum.FAILED.getCode(),"更新数据失败", contentDTO);
                }

            } catch (IOException e) {
                log.error("更新数据异常：", e);
                return ResultDTO.failed("更新数据异常：" + e);
            }

        } else if (contentByConditionMap.size() <= 0) {
            return ResultDTO.failed("根据联合条件未查询到数据");
        } else {
            return ResultDTO.failed("根据联合条件查询到多条数据");
        }
    }

    @Override
    public ResultDTO deleteById(String index, String routing, String id) {
        // 判断id是否为空
        if (id == null || id.isEmpty()) {
            return ResultDTO.failed("id不能空");
        }

        try {
            // 调用DAO删除数据
            Boolean deleteById = esContentDao.deleteById(index, routing, id);

            ResultDTO<String> resultDTO = null;
            resultDTO.setData(id);
            if (deleteById) {
                resultDTO.setCode(ResultCodeEnum.SUCCESS.getCode());
                resultDTO.setMessage("删除数据成功");
            } else {
                resultDTO.setCode(ResultCodeEnum.FAILED.getCode());
                resultDTO.setMessage("删除数据失败");
            }
            return resultDTO;
        } catch (IOException e) {
            log.error("删除数据异常：", e);
            return ResultDTO.failed("删除数据异常：" + e);
        }
    }

    @Override
    public ResultDTO<String> deleteByConditionMap(String index, String routing, Map<String, String> conditionMap) {
        // 判断map不为空
        if (conditionMap == null || conditionMap.isEmpty()) {
            return ResultDTO.failed("Map不能为空");
        }

        List<ContentDTO> contentByConditionMap;
        try {
            // 调用DAO查询数据
            contentByConditionMap = esContentDao.getContentByConditionMap(index, routing, conditionMap);
        } catch (IOException e) {
            log.error("查询数据异常：", e);
            return ResultDTO.failed("查询数据异常：" + e);
        }

        // 判断查询结果是否唯一
        if (conditionMap.size() == 1) {

            // 调用DAO删除数据
            String id = contentByConditionMap.get(0).getId();
            try {
                Boolean deleteById = esContentDao.deleteById(index, routing, id);
                if (deleteById) {
                    return new ResultDTO<String>(
                            ResultCodeEnum.SUCCESS.getCode(),
                            "删除数据成功",
                            id
                    );
                } else {
                    return new ResultDTO<String>(
                            ResultCodeEnum.FAILED.getCode(),
                            "删除数据失败",
                            id
                    );
                }
            } catch (IOException e) {
                log.error("删除数据异常：", e);
                return ResultDTO.failed("删除数据异常：" + e);
            }

        } else if (conditionMap.size() <= 0) {
            return ResultDTO.failed("根据联合条件未查询到数据");
        } else {
            return ResultDTO.failed("根据联合条件查询到多条数据");
        }
    }

    @Override
    public ResultDTO<ContentDTO> getContentById(String index, String routing, String id) {
        // 判断id是否为空
        if (id == null || id.isEmpty()) {
            return ResultDTO.failed("id不能空");
        }

        try {
            // 调用DAO查询数据
            ContentDTO contentById = esContentDao.getContentById(index, routing, id);

            if (contentById != null) {
                return new ResultDTO<ContentDTO>(
                        ResultCodeEnum.SUCCESS.getCode(),
                        ResultCodeEnum.SUCCESS.getMessage(),
                        contentById
                );
            } else {
                return ResultDTO.failed("根据ID检索到数据");
            }
        } catch (IOException e) {
            log.error("检索数据异常：", e);
            return ResultDTO.failed("检索数据异常：" + e);
        }
    }

    @Override
    public ResultDTO<List<ContentDTO>> getDataByIds(String index, String routing, String[] ids) {
        // 判断id是否为空
        if (ids == null || ids.length <= 0) {
            return ResultDTO.failed("id数组不能为空");
        }

        try {
            // 调用DAO查询数据
            List<ContentDTO> contentByIds = esContentDao.getContentByIds(index, routing, ids);

            if (contentByIds.size() > 0) {
                return new ResultDTO<List<ContentDTO>>(
                        ResultCodeEnum.SUCCESS.getCode(),
                        ResultCodeEnum.SUCCESS.getMessage(),
                        contentByIds
                );
            } else {
                return ResultDTO.failed("检索数据失败");
            }
        } catch (IOException e) {
            log.error("检索数据异常：", e);
            return ResultDTO.failed("检索数据异常：" + e);
        }
    }

    @Override
    public ResultDTO<ContentDTO> getDataByConditionMap(String index, String routing, Map<String, String> conditionMap) {
        // 判断map不为空
        if (conditionMap == null || conditionMap.isEmpty()) {
            return ResultDTO.failed("Map不能为空");
        }

        try {
            // 调用DAO检索数据
            List<ContentDTO> contentByConditionMap = esContentDao.getContentByConditionMap(index, routing, conditionMap);

            // 判断查询结果
            if (contentByConditionMap.size() == 1) {
                return new ResultDTO<ContentDTO>(
                        ResultCodeEnum.SUCCESS.getCode(),
                        ResultCodeEnum.SUCCESS.getMessage(),
                        contentByConditionMap.get(0)
                );
            } else if (contentByConditionMap.size() <= 0) {
                return ResultDTO.failed("根据联合条件未查询到数据");
            } else {
                return ResultDTO.failed("根据联合条件查询到多条数据");
            }

        } catch (IOException e) {
            log.error("检索数据异常：", e);
            return ResultDTO.failed("检索数据异常：" + e);
        }
    }

    @Override
    public ResultDTO<List<ContentDTO>> search(String index, String routing, int pageNum, int pageSize, Map<String, Object> condition,
                                   String keyword, List<String> orderFieldType) {

        try {
            List<ContentDTO> search = esContentDao.search(index, routing, pageNum, pageSize, condition, keyword, orderFieldType);
            if (search != null || search.size() > 0) {
                return new ResultDTO<>(
                        ResultCodeEnum.SUCCESS.getCode(),
                        ResultCodeEnum.SUCCESS.getMessage(),
                        search
                );
            } else {
                return new ResultDTO<>(
                        ResultCodeEnum.DATA_NOT_FOUND.getCode(),
                        ResultCodeEnum.DATA_NOT_FOUND.getMessage(),
                        new ArrayList<>()
                );
            }
        } catch (IOException e) {
            log.error("检索数据异常：", e);
            return ResultDTO.failed("检索数据异常：" + e);
        }
    }
}
