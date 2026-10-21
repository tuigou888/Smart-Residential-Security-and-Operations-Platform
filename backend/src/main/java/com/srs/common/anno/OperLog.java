package com.srs.common.anno;

import java.lang.annotation.*;

/**
 * 操作日志注解: 标注在 Controller 方法上, 由 OperLogAspect 记录到 sys_oper_log
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {

    /** 操作模块 */
    String module() default "";

    /** 0其他 1新增 2修改 3删除 4导出 5登录 */
    int businessType() default 0;
}
