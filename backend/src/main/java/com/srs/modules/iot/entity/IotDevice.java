package com.srs.modules.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_device")
@Schema(description = "物联网设备")
public class IotDevice extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "设备编码")
    private String deviceCode;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型 camera/guard/vehicle_gate/smoke/edge")
    private String deviceType;

    @Schema(description = "类型ID")
    private Long typeId;

    @Schema(description = "厂商")
    private String vendor;

    @Schema(description = "型号")
    private String model;

    @Schema(description = "固件版本")
    private String firmwareVersion;

    @Schema(description = "安装位置")
    private String installLocation;

    private BigDecimal longitude;

    private BigDecimal latitude;

    @Schema(description = "接入协议 gb28181/onvif/sdk/mqtt/modbus")
    private String accessProtocol;

    @Schema(description = "接入配置(JSON)")
    private String accessConfig;

    @Schema(description = "0离线 1在线")
    private Integer onlineStatus;

    @Schema(description = "1正常 2故障 3维保中")
    private Integer runStatus;

    @Schema(description = "最后在线时间")
    private LocalDateTime lastOnlineTime;

    @Schema(description = "安装日期")
    private LocalDate installDate;

    @Schema(description = "保修截止")
    private LocalDate warrantyDate;

    @Schema(description = "1启用 0停用")
    private Integer status;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String communityName;
}
