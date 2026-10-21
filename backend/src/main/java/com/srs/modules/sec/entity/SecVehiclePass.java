package com.srs.modules.sec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sec_vehicle_pass")
@Schema(description = "车辆通行记录")
public class SecVehiclePass implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "道闸设备ID")
    private Long gateDeviceId;

    @Schema(description = "车牌号")
    private String plateNo;

    @Schema(description = "1进 2出")
    private Integer direction;

    @Schema(description = "1固定 2月租 3临时 4访客")
    private Integer vehicleType;

    @Schema(description = "抓拍图")
    private String snapshotUrl;

    @Schema(description = "通行时间")
    private LocalDateTime passTime;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String gateName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String communityName;
}
