package com.srs.modules.patrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pat_point")
@Schema(description = "巡更点位")
public class PatPoint extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "点位名称")
    private String pointName;

    @Schema(description = "点位编码")
    private String pointCode;

    @Schema(description = "位置描述")
    private String location;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "NFC编码")
    private String nfcCode;

    @Schema(description = "二维码")
    private String qrCode;

    @Schema(description = "1启用 0停用")
    private Integer status;
}
