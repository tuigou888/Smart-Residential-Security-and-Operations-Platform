package com.srs.modules.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_device_event")
@Schema(description = "设备事件")
public class IotDeviceEvent extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "事件编码")
    private String eventCode;

    @Schema(description = "事件名称")
    private String eventName;

    @Schema(description = "事件数据(JSON)")
    private String eventData;

    @Schema(description = "事件时间")
    private LocalDateTime eventTime;

    @Schema(description = "0未处理 1已处理")
    private Integer handleStatus;
}
