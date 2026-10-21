package com.srs.modules.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.srs.common.core.DelEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_owner")
@Schema(description = "业主/住户")
public class BaseOwner extends DelEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "姓名")
    private String ownerName;

    @Schema(description = "手机号(AES加密存储)")
    private String phone;

    @Schema(description = "手机号脱敏")
    private String phoneMask;

    @Schema(description = "身份证(AES加密存储)")
    private String idCard;

    @Schema(description = "身份证脱敏")
    private String idCardMask;

    @Schema(description = "0未知 1男 2女")
    private Integer gender;

    @Schema(description = "人脸库标识")
    private String faceToken;

    @Schema(description = "人脸照片URL")
    private String faceImg;

    @Schema(description = "1业主 2家属 3租客")
    private Integer userType;

    @Schema(description = "实名认证 0未认证 1已认证")
    private Integer authStatus;

    @Schema(description = "1启用 0停用")
    private Integer status;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    @Schema(description = "绑定房屋ID集合")
    private List<Long> roomIds;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    @Schema(description = "房屋名称(楼栋-单元-房号)")
    private List<String> roomNames;
}
