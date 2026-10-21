package com.srs.datascope;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.srs.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 小区级 / 部门级数据权限: 依据登录用户 dataScope 自动追加过滤条件
 * <p>
 * 1全部 -> 不加条件; 4本小区 -> community_id IN (...); 3本部门 -> dept_id IN (...)
 * <p>
 * 约定: MyBatis-Plus 的 {@code BaseMultiTableInnerInterceptor} 会把返回值与原始 where 做 AND 拼接,
 * 因此"无需限制"时必须返回 null, 不能原样返回 where(否则会生成 where AND where)。
 */
@Slf4j
@Component
public class SrsDataPermissionHandler implements MultiDataPermissionHandler {

    private static final Set<String> COMMUNITY_TABLES = Set.of(
            "base_community", "base_building", "base_unit", "base_room", "base_room_resident", "base_vehicle",
            "iot_device", "iot_device_status_log", "iot_device_event",
            "vs_channel", "sec_alarm_event", "sec_access_record", "sec_access_permission",
            "sec_visitor_invite", "sec_vehicle_pass", "sec_blacklist", "sec_alarm_rule",
            "biz_work_order", "biz_notice", "biz_repair",
            "pat_point", "pat_route", "pat_plan", "pat_record",
            "ai_scene_config", "ai_event_record", "stat_daily_summary"
    );

    private static final Set<String> DEPT_TABLES = Set.of("sys_user", "sys_dept");

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        try {
            if (table == null || table.getName() == null) {
                return null;
            }
            if (SecurityUtils.isAdmin() || SecurityUtils.getDataScope() == null
                    || SecurityUtils.getDataScope() == 1) {
                return null;
            }
            String tableName = table.getName().replace("`", "").toLowerCase();
            String column;
            Set<Long> scopeIds;

            if (COMMUNITY_TABLES.contains(tableName)) {
                column = "community_id";
                scopeIds = SecurityUtils.getCommunityIds();
            } else if (DEPT_TABLES.contains(tableName) && SecurityUtils.getDataScope() == 3) {
                column = "dept_id";
                Long deptId = SecurityUtils.getDeptId();
                scopeIds = deptId == null ? Set.of() : Set.of(deptId);
            } else {
                return null;
            }

            if (scopeIds == null || scopeIds.isEmpty() || scopeIds.size() > 500) {
                return null;
            }
            return CCJSqlParserUtil.parseCondExpression(
                    column + " IN (" + String.join(",", scopeIds.stream().map(String::valueOf).toList()) + ")");
        } catch (Exception e) {
            log.warn("[datascope] 数据权限处理异常, 本次查询不加限制: {}", e.getMessage());
            return null;
        }
    }

    /** 单表场景(非多表解析器)不做限制, 保持原 where */
    @Override
    public Expression getSqlSegment(Expression where, String mappedStatementId) {
        return where;
    }
}
