package com.srs.modules.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("vs_channel")
@Schema(description = "视频通道")
public class VsChannel extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "通道号")
    private Integer channelNo;

    @Schema(description = "通道名称")
    private String channelName;

    @Schema(description = "国标编码")
    private String gbCode;

    @Schema(description = "拉流地址")
    private String streamUrl;

    @Schema(description = "是否支持云台")
    private Integer hasPtz;

    @Schema(description = "是否支持音频")
    private Integer hasAudio;

    @Schema(description = "录像计划ID")
    private Long recordPlanId;

    @Schema(description = "1启用 0停用")
    private Integer status;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String communityName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String deviceName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private Integer onlineStatus;
}
