package com.srs.modules.patrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pat_route")
@Schema(description = "巡更路线")
public class PatRoute extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "路线名称")
    private String routeName;

    @Schema(description = "路线编码")
    private String routeCode;

    @Schema(description = "路线描述")
    private String description;

    @Schema(description = "点位数量")
    private Integer pointCount;

    @Schema(description = "1启用 0停用")
    private Integer status;
}
