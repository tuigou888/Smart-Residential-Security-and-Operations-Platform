package com.srs.modules.ai.entity;

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
@TableName("ai_event_record")
@Schema(description = "AI事件记录")
public class AiEventRecord extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "场景ID")
    private Long sceneId;

    @Schema(description = "通道ID")
    private Long channelId;

    @Schema(description = "事件类型")
    private String eventType;

    @Schema(description = "置信度")
    private BigDecimal confidence;

    @Schema(description = "抓拍图")
    private String snapshotUrl;

    @Schema(description = "视频片段")
    private String clipUrl;

    @Schema(description = "检测框数据(JSON)")
    private String bboxData;

    @Schema(description = "事件时间")
    private LocalDateTime eventTime;

    @Schema(description = "0未处理 1已处理 2误报")
    private Integer handleStatus;
}
