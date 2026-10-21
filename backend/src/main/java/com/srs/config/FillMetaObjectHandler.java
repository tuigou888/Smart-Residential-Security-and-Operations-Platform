package com.srs.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.srs.security.SecurityUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 公共字段自动填充: create_by / create_time / update_by / update_time
 */
@Component
public class FillMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        String username = Objects.requireNonNullElse(SecurityUtils.getUsername(), "system");
        this.strictInsertFill(metaObject, "createBy", String.class, username);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateBy", String.class, username);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        String username = Objects.requireNonNullElse(SecurityUtils.getUsername(), "system");
        this.strictUpdateFill(metaObject, "updateBy", String.class, username);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
