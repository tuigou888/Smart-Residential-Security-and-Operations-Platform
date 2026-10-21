package com.srs.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
@Schema(description = "系统角色")
public class SysRole extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色标识")
    private String roleKey;

    @Schema(description = "排序")
    private Integer roleSort;

    @Schema(description = "数据范围 1全部 2自定义 3本部门 4本小区")
    private Integer dataScope;

    @Schema(description = "1启用 0停用")
    private Integer status;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    @Schema(description = "菜单ID集合")
    private java.util.List<Long> menuIds;
}
