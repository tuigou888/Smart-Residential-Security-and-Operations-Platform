package com.srs.modules.msg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_notice")
@Schema(description = "通知公告")
public class BizNotice extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID")
    private Long communityId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "1公告 2通知 3活动")
    private Integer noticeType;

    @Schema(description = "0草稿 1已发布")
    private Integer publishStatus;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "是否置顶")
    private Integer topFlag;

    @Schema(description = "浏览次数")
    private Integer viewCount;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String communityName;
}
