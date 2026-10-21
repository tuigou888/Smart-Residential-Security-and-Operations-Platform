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
@TableName("sec_visitor_invite")
@Schema(description = "访客邀请")
public class SecVisitorInvite extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "邀请编号")
    private String inviteNo;

    @Schema(description = "邀请业主ID")
    private Long inviterId;

    @Schema(description = "邀请人姓名")
    private String inviterName;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "访客电话")
    private String visitorPhone;

    @Schema(description = "访客人脸")
    private String visitorFaceUrl;

    @Schema(description = "访客二维码")
    private String qrCode;

    @Schema(description = "到访开始时间")
    private LocalDateTime visitStart;

    @Schema(description = "到访结束时间")
    private LocalDateTime visitEnd;

    @Schema(description = "可通行范围")
    private String passScope;

    @Schema(description = "来访目的")
    private String visitPurpose;

    @Schema(description = "0待到访 1已到访 2已过期 3已撤销")
    private Integer status;

    @Schema(description = "实际到访时间")
    private LocalDateTime arriveTime;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String communityName;
}
