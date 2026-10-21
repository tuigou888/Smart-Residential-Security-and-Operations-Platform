package com.srs.modules.sec.entity;

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
@TableName("sec_blacklist")
@Schema(description = "黑名单")
public class SecBlacklist extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "1人员 2车辆")
    private Integer blacklistType;

    @Schema(description = "姓名/车牌")
    private String targetName;

    @Schema(description = "身份证/车牌号")
    private String targetId;

    @Schema(description = "人脸图片")
    private String faceImg;

    @Schema(description = "拉黑原因")
    private String reason;

    @Schema(description = "生效开始")
    private LocalDate effectiveStart;

    @Schema(description = "生效结束")
    private LocalDate effectiveEnd;

    @Schema(description = "1启用 0停用")
    private Integer status;
}
