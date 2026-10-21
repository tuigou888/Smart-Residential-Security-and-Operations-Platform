package com.srs.modules.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_vehicle")
@Schema(description = "车辆档案")
public class BaseVehicle extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "车主ID")
    private Long ownerId;

    @Schema(description = "车牌号")
    private String plateNo;

    @Schema(description = "车牌颜色")
    private String plateColor;

    @Schema(description = "1固定车 2月租车 3临时车")
    private Integer vehicleType;

    @Schema(description = "车辆品牌")
    private String vehicleBrand;

    @Schema(description = "车辆颜色")
    private String vehicleColor;

    @Schema(description = "有效期开始")
    private LocalDate startDate;

    @Schema(description = "有效期结束")
    private LocalDate endDate;

    @Schema(description = "1启用 0停用")
    private Integer status;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String ownerName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String communityName;
}
