package com.srs.modules.patrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pat_record")
@Schema(description = "巡更记录")
public class PatRecord extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "计划ID")
    private Long planId;

    @Schema(description = "路线ID")
    private Long routeId;

    @Schema(description = "点位ID")
    private Long pointId;

    @Schema(description = "巡更人员ID")
    private Long userId;

    @Schema(description = "巡更人员姓名")
    private String userName;

    @Schema(description = "1NFC 2二维码 3GPS")
    private Integer checkMethod;

    @Schema(description = "打卡时间")
    private LocalDateTime checkTime;

    @Schema(description = "打卡经度")
    private BigDecimal longitude;

    @Schema(description = "打卡纬度")
    private BigDecimal latitude;

    @Schema(description = "1正常 2异常 3漏巡")
    private Integer status;

    @Schema(description = "异常描述")
    private String exceptionDesc;

    @Schema(description = "现场图片")
    private String images;
}
