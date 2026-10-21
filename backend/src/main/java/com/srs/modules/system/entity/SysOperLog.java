package com.srs.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_oper_log")
@Schema(description = "操作日志")
public class SysOperLog implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "0其他 1新增 2修改 3删除 4导出 5登录")
    private Integer businessType;

    @Schema(description = "方法名")
    private String method;

    @Schema(description = "请求方式")
    private String requestMethod;

    @Schema(description = "操作人")
    private String operName;

    @Schema(description = "请求URL")
    private String operUrl;

    @Schema(description = "操作IP")
    private String operIp;

    @Schema(description = "请求参数")
    private String operParam;

    @Schema(description = "返回结果")
    private String jsonResult;

    @Schema(description = "1成功 0失败")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "操作时间")
    private LocalDateTime operTime;
}
