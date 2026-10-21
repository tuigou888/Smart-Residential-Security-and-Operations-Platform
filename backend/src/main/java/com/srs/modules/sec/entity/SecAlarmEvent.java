package com.srs.modules.sec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sec_alarm_event")
@Schema(description = "告警事件")
public class SecAlarmEvent extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "告警编号")
    private String alarmNo;

    @Schema(description = "告警类型 intrusion/occupy/ebike/smoke/door_force等")
    private String alarmType;

    @Schema(description = "1紧急 2重要 3一般")
    private Integer alarmLevel;

    @Schema(description = "1设备 2AI 3人工")
    private Integer sourceType;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "通道ID")
    private Long channelId;

    @Schema(description = "告警标题")
    private String title;

    @Schema(description = "告警内容")
    private String content;

    @Schema(description = "抓拍图")
    private String snapshotUrl;

    @Schema(description = "告警片段")
    private String clipUrl;

    @Schema(description = "告警位置")
    private String location;

    @Schema(description = "0未处理 1处理中 2已处理 3误报")
    private Integer status;

    @Schema(description = "发生时间")
    private LocalDateTime occurTime;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "处理人姓名")
    private String handlerName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String communityName;
}
