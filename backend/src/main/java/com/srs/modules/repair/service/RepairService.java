package com.srs.modules.repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.common.util.NoGenerator;
import com.srs.modules.repair.entity.BizRepair;
import com.srs.modules.repair.mapper.BizRepairMapper;
import com.srs.modules.workorder.entity.BizWorkOrder;
import com.srs.modules.workorder.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 报修投诉: 受理 -> 派单(生成工单) -> 处理 -> 完成 -> 评价
 */
@Service
@RequiredArgsConstructor
public class RepairService extends ServiceImpl<BizRepairMapper, BizRepair> {

    private final WorkOrderService workOrderService;

    public IPage<BizRepair> page(PageQuery q, Long communityId, Integer status, Integer repairType,
                                 String keyword) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<BizRepair>()
                        .eq(communityId != null, BizRepair::getCommunityId, communityId)
                        .eq(status != null, BizRepair::getStatus, status)
                        .eq(repairType != null, BizRepair::getRepairType, repairType)
                        .and(StringUtils.hasText(keyword),
                                w -> w.like(BizRepair::getTitle, keyword)
                                        .or().like(BizRepair::getRepairNo, keyword))
                        .orderByDesc(BizRepair::getCreateTime));
    }

    public BizRepair detail(Long id) {
        BizRepair repair = baseMapper.selectById(id);
        if (repair == null) {
            throw BizException.notFound("报修记录不存在: " + id);
        }
        return repair;
    }

    /**
     * 受理报修: 生成报修编号并落库
     * <p>
     * 编号使用"时间戳+随机数", 极端并发下仍可能重复; 依赖 uk_repair_no 唯一索引兜底,
     * 冲突时重新生成一次。
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createRepair(BizRepair repair) {
        if (repair.getCommunityId() == null) {
            throw BizException.badRequest("小区ID不能为空");
        }
        if (repair.getOwnerId() == null) {
            throw BizException.badRequest("报修业主不能为空");
        }
        if (!StringUtils.hasText(repair.getTitle())) {
            throw BizException.badRequest("报修标题不能为空");
        }
        repair.setRepairNo(NoGenerator.repairNo());
        repair.setStatus(0);
        try {
            baseMapper.insert(repair);
        } catch (DuplicateKeyException e) {
            repair.setId(null);
            repair.setRepairNo(NoGenerator.repairNo());
            baseMapper.insert(repair);
        }
        return repair.getId();
    }

    /** 派单: 报修状态 0 -> 1, 同时生成关联工单 */
    @Transactional(rollbackFor = Exception.class)
    public Long dispatch(Long id, Long assigneeId, String assigneeName) {
        BizRepair repair = requireStatus(id, 0);
        BizWorkOrder order = new BizWorkOrder();
        order.setCommunityId(repair.getCommunityId());
        order.setOrderType("repair");
        order.setTitle("【报修】" + repair.getTitle());
        order.setContent(repair.getContent());
        order.setSourceId(repair.getId());
        order.setSourceType("repair");
        order.setPriority(2);
        order.setStatus(1);
        order.setAssigneeId(assigneeId);
        order.setAssigneeName(assigneeName);
        Long orderId = workOrderService.create(order);
        if (assigneeId != null) {
            workOrderService.assign(orderId, assigneeId, assigneeName);
        }
        repair.setWorkOrderId(orderId);
        repair.setStatus(1);
        baseMapper.updateById(repair);
        return orderId;
    }

    /** 开始处理: 1 -> 2 */
    @Transactional(rollbackFor = Exception.class)
    public void start(Long id) {
        BizRepair repair = requireStatus(id, 1);
        repair.setStatus(2);
        baseMapper.updateById(repair);
    }

    /** 完成: 2 -> 3 */
    @Transactional(rollbackFor = Exception.class)
    public void finish(Long id, String remark) {
        BizRepair repair = requireStatus(id, 2);
        repair.setStatus(3);
        if (StringUtils.hasText(remark)) {
            repair.setRemark(remark);
        }
        baseMapper.updateById(repair);
    }

    /** 评价: 3 -> 4 */
    @Transactional(rollbackFor = Exception.class)
    public void evaluate(Long id, String content) {
        BizRepair repair = requireStatus(id, 3);
        repair.setStatus(4);
        if (StringUtils.hasText(content)) {
            repair.setRemark(content);
        }
        baseMapper.updateById(repair);
    }

    private BizRepair requireStatus(Long id, int expected) {
        BizRepair repair = detail(id);
        if (repair.getStatus() == null || repair.getStatus() != expected) {
            throw BizException.badRequest("当前状态不允许该操作");
        }
        return repair;
    }
}
