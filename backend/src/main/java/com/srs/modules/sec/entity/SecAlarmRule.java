package com.srs.modules.sec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sec_alarm_rule")
@Schema(description = "告警规则")
public class SecAlarmRule extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "小区ID(NULL=全局)")
    private Long communityId;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "适用告警类型")
    private String alarmType;

    @Schema(description = "匹配后告警级别")
    private Integer alarmLevel;

    @Schema(description = "是否自动派单")
    private Integer autoDispatch;

    @Schema(description = "自动派单目标角色标识")
    private String autoDispatchRole;

    @Schema(description = "通知渠道 1站内信 2短信 3推送 4语音")
    private String notifyChannels;

    @Schema(description = "处置时限(分钟)")
    private Integer slaMinutes;

    @Schema(description = "1启用 0停用")
    private Integer status;
}
