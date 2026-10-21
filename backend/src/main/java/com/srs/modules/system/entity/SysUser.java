package com.srs.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Schema(description = "系统用户")
public class SysUser extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "用户名")
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(description = "密码(BCrypt)")
    private String password;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "1平台用户 2物业 3安保 4业主")
    private Integer userType;

    @Schema(description = "1启用 0停用")
    private Integer status;

    @Schema(description = "最后登录IP")
    private String loginIp;

    @Schema(description = "最后登录时间")
    private LocalDateTime loginTime;

    /** 非数据库字段: 角色ID集合(用于新增/编辑) */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    @Schema(description = "角色ID集合")
    private java.util.List<Long> roleIds;

    /** 非数据库字段: 部门名称 */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    @Schema(description = "部门名称")
    private String deptName;
}
