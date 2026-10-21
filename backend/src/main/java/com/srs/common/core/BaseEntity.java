package com.srs.common.core;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 业务实体公共字段(审计字段): create_by/create_time/update_by/update_time/remark
 * <p>
 * 注意: 不包含 del_flag —— 仅"存在 del_flag 列"的表才使用 {@link DelEntity},
 * 避免 MyBatis-Plus 对 sys_menu / sys_config 等无该列的表自动追加逻辑删除条件。
 */
@Data
public class BaseEntity implements Serializable {

    @Schema(description = "创建者")
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新者")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "备注")
    private String remark;
}
