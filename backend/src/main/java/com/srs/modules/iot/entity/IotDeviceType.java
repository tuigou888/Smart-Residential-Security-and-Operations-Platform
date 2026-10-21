package com.srs.modules.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("iot_device_type")
@Schema(description = "设备类型")
public class IotDeviceType extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "类型编码")
    private String typeCode;

    @Schema(description = "类型名称")
    private String typeName;

    @Schema(description = "父类型ID")
    private Long parentId;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序")
    private Integer orderNum;

    @Schema(description = "1启用 0停用")
    private Integer status;

    @Schema(description = "小区ID(为空表示全局类型)")
    private Long communityId;
}
