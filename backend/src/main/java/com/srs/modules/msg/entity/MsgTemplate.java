package com.srs.modules.msg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("msg_template")
@Schema(description = "消息模板")
public class MsgTemplate extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "模板编码")
    private String templateCode;

    @Schema(description = "模板名称")
    private String templateName;

    @Schema(description = "1站内信 2短信 3推送 4语音")
    private Integer channelType;

    @Schema(description = "内容模板")
    private String contentTemplate;

    @Schema(description = "1启用 0停用")
    private Integer status;

    @Schema(description = "小区ID(为空表示全局模板)")
    private Long communityId;
}
