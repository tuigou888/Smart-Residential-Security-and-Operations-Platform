package com.srs.modules.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("iot_device_status_log")
@Schema(description = "设备状态日志")
public class IotDeviceStatusLog implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "1上线 2离线 3故障 4恢复")
    private Integer eventType;

    @Schema(description = "事件时间")
    private LocalDateTime eventTime;

    @Schema(description = "附加信息")
    private String extra;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String deviceName;
}
