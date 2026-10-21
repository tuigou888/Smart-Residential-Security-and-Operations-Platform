package com.srs.modules.base.entity;

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
@TableName("base_community")
@Schema(description = "小区")
public class BaseCommunity extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区编码")
    private String communityCode;

    @Schema(description = "小区名称")
    private String communityName;

    @Schema(description = "地址")
    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    @Schema(description = "联系人")
    private String contactPerson;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "占地面积(㎡)")
    private BigDecimal coverArea;

    @Schema(description = "楼栋数")
    private Integer buildingCount;

    @Schema(description = "房屋数")
    private Integer roomCount;

    @Schema(description = "封面图")
    private String coverImg;

    @Schema(description = "1启用 0停用")
    private Integer status;
}
