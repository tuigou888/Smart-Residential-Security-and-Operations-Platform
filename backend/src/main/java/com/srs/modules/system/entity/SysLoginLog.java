package com.srs.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_login_log")
@Schema(description = "登录日志")
public class SysLoginLog implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "登录IP")
    private String loginIp;

    @Schema(description = "登录地点")
    private String loginLocation;

    @Schema(description = "浏览器")
    private String browser;

    @Schema(description = "操作系统")
    private String os;

    @Schema(description = "1成功 0失败")
    private Integer status;

    @Schema(description = "提示信息")
    private String msg;

    @Schema(description = "登录时间")
    private LocalDateTime loginTime;
}
