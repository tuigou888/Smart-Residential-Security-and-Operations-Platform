package com.srs.modules.msg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("msg_send_log")
@Schema(description = "消息发送日志/站内信")
public class MsgSendLog implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "1站内信 2短信 3推送 4语音")
    private Integer channelType;

    @Schema(description = "接收人ID")
    private Long receiverId;

    @Schema(description = "接收人姓名")
    private String receiverName;

    @Schema(description = "联系方式")
    private String receiverContact;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "0待发送 1已发送 2失败")
    private Integer sendStatus;

    @Schema(description = "是否已读(站内信)")
    private Integer readFlag;

    @Schema(description = "发送时间")
    private LocalDateTime sendTime;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
