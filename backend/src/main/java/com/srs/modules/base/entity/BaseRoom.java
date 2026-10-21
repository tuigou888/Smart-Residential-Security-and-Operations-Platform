package com.srs.modules.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_room")
@Schema(description = "房屋")
public class BaseRoom extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "楼栋ID")
    private Long buildingId;

    @Schema(description = "单元ID")
    private Long unitId;

    @Schema(description = "房号")
    private String roomNo;

    @Schema(description = "楼层")
    private Integer floor;

    @Schema(description = "建筑面积(㎡)")
    private BigDecimal area;

    @Schema(description = "1住宅 2商铺 3车位 4储藏室 5其他")
    private Integer roomType;

    @Schema(description = "0未入住 1已入住")
    private Integer ownerStatus;

    @Schema(description = "1启用 0停用")
    private Integer status;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String buildingName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String unitName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String communityName;
}
