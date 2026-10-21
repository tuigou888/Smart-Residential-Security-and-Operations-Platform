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
@TableName("base_room_resident")
@Schema(description = "人房关系")
public class BaseRoomResident extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "房屋ID")
    private Long roomId;

    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "1户主 2家属 3租客")
    private Integer relation;

    @Schema(description = "入住日期")
    private LocalDate moveInDate;

    @Schema(description = "迁出日期")
    private LocalDate moveOutDate;

    @Schema(description = "1在住 0迁出")
    private Integer status;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String ownerName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String roomName;
}
