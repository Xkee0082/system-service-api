package com.example.elasticsearchapi.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.elasticsearchapi.constant.ResultCodeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: xu_ke
 * @Date: 2022-06-21 20:31
 * @description TODO 封装返回结果
 */
@Data
@ApiModel("返回结果实体类")
public class ResultDTO<T> implements Serializable {
    /**
     * 操作结果状态码
     */
    @ApiModelProperty(value = "code", example = "200")
    private int code;
    /**
     * 提示信息
     */
    @ApiModelProperty(value = "message", example = "操作成功")
    private String message;
    /**
     * 响应数据
     */
    @ApiModelProperty(value = "data")
    private T data;

    /**
     * 构造器
     */
    public ResultDTO() {}
    public ResultDTO(int code, String message) {
        this.code = code;
        this.message = message;
    }
    public ResultDTO(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 操作成功，没有响应数据
     */
    public static ResultDTO success() {
        JSONObject jsonObject = JSON.parseObject(
                JSON.toJSONString(new ResultDTO<>(ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMessage())));
        jsonObject.remove("data");
        return jsonObject.toJavaObject(ResultDTO.class);
    }

    /**
     * 操作成功
     * @param message 自定义提示信息
     */
    public static ResultDTO success(String message) {
        JSONObject jsonObject = JSON.parseObject(
                JSON.toJSONString(new ResultDTO<>(ResultCodeEnum.SUCCESS.getCode(), message)));
        jsonObject.remove("data");
        return jsonObject.toJavaObject(ResultDTO.class);
    }

    /**
     * 操作失败
     */
    public static ResultDTO failed() {
        JSONObject jsonObject = JSON.parseObject(
                JSON.toJSONString(new ResultDTO<>(ResultCodeEnum.FAILED.getCode(), ResultCodeEnum.FAILED.getMessage())));
        jsonObject.remove("data");
        return jsonObject.toJavaObject(ResultDTO.class);
    }

    /**
     * 操作成功
     * @param message 自定义提示信息
     */
    public static ResultDTO failed(String message) {
        JSONObject jsonObject = JSON.parseObject(
                JSON.toJSONString(new ResultDTO<>(ResultCodeEnum.FAILED.getCode(), message)));
        jsonObject.remove("data");
        return jsonObject.toJavaObject(ResultDTO.class);
    }

}
