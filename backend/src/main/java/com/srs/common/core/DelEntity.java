package com.srs.common.core;

import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 含逻辑删除标志的实体基类(仅用于表结构中存在 del_flag 列的表)
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DelEntity extends BaseEntity {

    @Schema(description = "删除标志 0正常 1删除")
    @TableLogic
    private Integer delFlag;
}
