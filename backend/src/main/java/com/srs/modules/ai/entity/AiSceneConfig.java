package com.srs.modules.ai.entity;

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
@TableName("ai_scene_config")
@Schema(description = "AI场景配置")
public class AiSceneConfig extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "场景编码")
    private String sceneCode;

    @Schema(description = "场景名称")
    private String sceneName;

    @Schema(description = "关联通道ID(逗号分隔)")
    private String channelIds;

    @Schema(description = "算法类型")
    private String algorithmType;

    @Schema(description = "置信度阈值")
    private BigDecimal threshold;

    @Schema(description = "1紧急 2重要 3一般")
    private Integer alarmLevel;

    @Schema(description = "启用开始时间 HH:mm")
    private String enableTimeStart;

    @Schema(description = "启用结束时间 HH:mm")
    private String enableTimeEnd;

    @Schema(description = "1启用 0停用")
    private Integer status;
}
