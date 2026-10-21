package com.srs.modules.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_unit")
@Schema(description = "单元")
public class BaseUnit extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "楼栋ID")
    private Long buildingId;

    @Schema(description = "单元名称")
    private String unitName;

    @Schema(description = "楼层数")
    private Integer floorCount;

    @Schema(description = "房屋数")
    private Integer roomCount;

    @Schema(description = "1启用 0停用")
    private Integer status;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    @Schema(description = "楼栋名称")
    private String buildingName;
}
