package com.srs.modules.sec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sec_access_record")
@Schema(description = "门禁通行记录")
public class SecAccessRecord implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "业主ID")
    private Long ownerId;

    @Schema(description = "人员姓名")
    private String personName;

    @Schema(description = "1人脸 2二维码 3刷卡 4蓝牙 5远程 6密码")
    private Integer openMethod;

    @Schema(description = "1通过 0拒绝")
    private Integer passResult;

    @Schema(description = "拒绝原因")
    private String denyReason;

    @Schema(description = "抓拍图")
    private String snapshotUrl;

    @Schema(description = "通行时间")
    private LocalDateTime passTime;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String deviceName;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String communityName;
}
