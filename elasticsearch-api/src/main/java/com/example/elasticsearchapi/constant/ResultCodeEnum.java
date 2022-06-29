package com.example.elasticsearchapi.constant;

import lombok.Getter;

/**
 * @Author: xu_ke
 * @Date: 2022-06-21 20:36
 * @description TODO
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(200, "操作成功"),
    DATA_NOT_FOUND(400, "未检索到数据"),
    FAILED(500, "操作失败");

    /**
     * 返回结果状态码
     */
    private int code;
    /**
     * 提示信息
     */
    private String message;

    private ResultCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
