package com.srs.modules.workorder.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.common.util.NoGenerator;
import com.srs.modules.sec.entity.SecAlarmEvent;
import com.srs.modules.workorder.entity.BizWorkOrder;
import com.srs.modules.workorder.entity.BizWorkOrderFlow;
import com.srs.modules.workorder.mapper.BizWorkOrderFlowMapper;
import com.srs.modules.workorder.mapper.BizWorkOrderMapper;
import com.srs.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工单中心: 创建 -> 派发 -> 接单 -> 处理 -> 验收 -> 评价 -> 关闭
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderService extends ServiceImpl<BizWorkOrderMapper, BizWorkOrder> {

    private final BizWorkOrderFlowMapper flowMapper;

    public IPage<BizWorkOrder> pageOrders(PageQuery q, Long communityId, String orderType, Integer status,
                                          Integer priority, Long assigneeId, String title) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<BizWorkOrder>()
                        .eq(communityId != null, BizWorkOrder::getCommunityId, communityId)
                        .eq(StringUtils.hasText(orderType), BizWorkOrder::getOrderType, orderType)
                        .eq(status != null, BizWorkOrder::getStatus, status)
                        .eq(priority != null, BizWorkOrder::getPriority, priority)
                        .eq(assigneeId != null, BizWorkOrder::getAssigneeId, assigneeId)
                        .like(StringUtils.hasText(title), BizWorkOrder::getTitle, title)
                        .orderByDesc(BizWorkOrder::getCreateTime));
    }

    public Map<String, Object> detail(Long id) {
        BizWorkOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw BizException.notFound("工单不存在: " + id);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("order", order);
        data.put("flows", flowMapper.selectList(new LambdaQueryWrapper<BizWorkOrderFlow>()
                .eq(BizWorkOrderFlow::getOrderId, id)
                .orderByAsc(BizWorkOrderFlow::getCreateTime)));
        return data;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(BizWorkOrder order) {
        order.setOrderNo(NoGenerator.orderNo());
        order.setStatus(order.getStatus() == null ? 0 : order.getStatus());
        if (order.getPriority() == null) {
            order.setPriority(2);
        }
        baseMapper.insert(order);
        flow(order.getId(), "create", "创建工单");
        return order.getId();
    }

    /** 告警转工单: 建立关联并生成流转记录 */
    @Transactional(rollbackFor = Exception.class)
    public Long createFromAlarm(SecAlarmEvent alarm, int slaMinutes) {
        BizWorkOrder order = new BizWorkOrder();
        order.setCommunityId(alarm.getCommunityId());
        order.setOrderType("alarm");
        order.setTitle("【告警】" + alarm.getTitle());
        order.setContent(alarm.getContent());
        order.setSourceId(alarm.getId());
        order.setSourceType("alarm");
        order.setPriority(alarm.getAlarmLevel() == null ? 2 : alarm.getAlarmLevel());
        order.setStatus(0);
        order.setSlaDeadline(LocalDateTime.now().plusMinutes(slaMinutes));
        order.setOrderNo(NoGenerator.orderNo());
        baseMapper.insert(order);
        flow(order.getId(), "create", "由告警 " + alarm.getAlarmNo() + " 自动生成");
        return order.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void assign(Long id, Long assigneeId, String assigneeName) {
        BizWorkOrder order = getOrder(id);
        order.setAssigneeId(assigneeId);
        order.setAssigneeName(assigneeName);
        order.setStatus(1);
        if (order.getSlaDeadline() == null) {
            order.setSlaDeadline(LocalDateTime.now().plusHours(8));
        }
        baseMapper.updateById(order);
        flow(id, "assign", "派单给 " + assigneeName);
    }

    @Transactional(rollbackFor = Exception.class)
    public void accept(Long id, String detail) {
        BizWorkOrder order = getOrder(id);
        order.setStatus(2);
        if (order.getAssigneeId() == null) {
            order.setAssigneeId(SecurityUtils.getUserId());
            order.setAssigneeName(SecurityUtils.getRealName());
        }
        baseMapper.updateById(order);
        flow(id, "accept", StringUtils.hasText(detail) ? detail : "已接单");
    }

    @Transactional(rollbackFor = Exception.class)
    public void handle(Long id, String detail, String attachments) {
        BizWorkOrder order = getOrder(id);
        if (order.getStatus() == 0 || order.getStatus() == 1) {
            order.setStatus(2);
        }
        baseMapper.updateById(order);
        flow(id, "handle", detail, attachments);
    }

    /** 处理完成提交验收 */
    @Transactional(rollbackFor = Exception.class)
    public void finish(Long id, String detail, String attachments) {
        BizWorkOrder order = getOrder(id);
        order.setStatus(3);
        baseMapper.updateById(order);
        flow(id, "handle", StringUtils.hasText(detail) ? detail : "处理完成, 待验收", attachments);
    }

    @Transactional(rollbackFor = Exception.class)
    public void verify(Long id, boolean pass, String detail) {
        BizWorkOrder order = getOrder(id);
        if (!pass) {
            order.setStatus(2);
            baseMapper.updateById(order);
            flow(id, "verify", "验收不通过: " + detail);
            return;
        }
        order.setStatus(4);
        order.setFinishTime(LocalDateTime.now());
        baseMapper.updateById(order);
        flow(id, "verify", StringUtils.hasText(detail) ? detail : "验收通过");

        // 同步关闭关联告警
        if ("alarm".equals(order.getSourceType()) && order.getSourceId() != null) {
            log.info("[workorder] 工单 {} 完成, 关联告警 {} 待复核", id, order.getSourceId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void evaluate(Long id, Integer score, String content) {
        BizWorkOrder order = getOrder(id);
        order.setEvaluateScore(score);
        order.setEvaluateContent(content);
        baseMapper.updateById(order);
        flow(id, "evaluate", "评分 " + score + " 星");
    }

    @Transactional(rollbackFor = Exception.class)
    public void close(Long id, String reason) {
        BizWorkOrder order = getOrder(id);
        order.setStatus(5);
        baseMapper.updateById(order);
        flow(id, "upgrade", StringUtils.hasText(reason) ? reason : "工单关闭");
    }

    /** 工单统计: 按状态 + 超时数 */
    public Map<String, Object> summary(Long communityId) {
        Map<String, Object> data = new LinkedHashMap<>();
        for (int s = 0; s <= 5; s++) {
            int status = s;
            data.put("status" + s, baseMapper.selectCount(new LambdaQueryWrapper<BizWorkOrder>()
                    .eq(communityId != null, BizWorkOrder::getCommunityId, communityId)
                    .eq(BizWorkOrder::getStatus, status)));
        }
        long overdue = baseMapper.selectCount(new LambdaQueryWrapper<BizWorkOrder>()
                .eq(communityId != null, BizWorkOrder::getCommunityId, communityId)
                .lt(BizWorkOrder::getSlaDeadline, LocalDateTime.now())
                .notIn(BizWorkOrder::getStatus, List.of(4, 5)));
        data.put("overdue", overdue);
        long todayFinished = baseMapper.selectCount(new LambdaQueryWrapper<BizWorkOrder>()
                .eq(communityId != null, BizWorkOrder::getCommunityId, communityId)
                .ge(BizWorkOrder::getFinishTime, LocalDateTime.now().toLocalDate().atStartOfDay()));
        data.put("todayFinished", todayFinished);
        return data;
    }

    public List<BizWorkOrder> myOrders(Long assigneeId) {
        return baseMapper.selectList(new LambdaQueryWrapper<BizWorkOrder>()
                .eq(BizWorkOrder::getAssigneeId, assigneeId)
                .notIn(BizWorkOrder::getStatus, List.of(5))
                .orderByDesc(BizWorkOrder::getCreateTime));
    }

    private BizWorkOrder getOrder(Long id) {
        BizWorkOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw BizException.notFound("工单不存在: " + id);
        }
        return order;
    }

    private void flow(Long orderId, String action, String detail) {
        flow(orderId, action, detail, null);
    }

    private void flow(Long orderId, String action, String detail, String attachments) {
        BizWorkOrderFlow flow = new BizWorkOrderFlow();
        flow.setOrderId(orderId);
        flow.setAction(action);
        flow.setOperatorId(SecurityUtils.getUserId());
        flow.setOperatorName(SecurityUtils.getRealName());
        flow.setActionDetail(detail);
        flow.setAttachments(attachments);
        flowMapper.insert(flow);
    }
}
