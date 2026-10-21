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
@TableName("pat_route_point")
@Schema(description = "路线点位关联")
public class PatRoutePoint extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "路线ID")
    private Long routeId;

    @Schema(description = "点位ID")
    private Long pointId;

    @Schema(description = "巡更顺序")
    private Integer orderNum;

    @Schema(description = "小区ID")
    private Long communityId;
}
