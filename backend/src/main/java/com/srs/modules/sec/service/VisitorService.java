package com.srs.modules.sec.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.common.util.NoGenerator;
import com.srs.modules.sec.entity.SecVisitorInvite;
import com.srs.modules.sec.mapper.SecVisitorInviteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitorService extends ServiceImpl<SecVisitorInviteMapper, SecVisitorInvite> {

    public IPage<SecVisitorInvite> pageInvites(PageQuery q, Long communityId, String visitorName,
                                               Long inviterId, Integer status) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<SecVisitorInvite>()
                        .eq(communityId != null, SecVisitorInvite::getCommunityId, communityId)
                        .like(StringUtils.hasText(visitorName), SecVisitorInvite::getVisitorName, visitorName)
                        .eq(inviterId != null, SecVisitorInvite::getInviterId, inviterId)
                        .eq(status != null, SecVisitorInvite::getStatus, status)
                        .orderByDesc(SecVisitorInvite::getId));
    }

    /** 创建邀请并生成二维码凭证 */
    public SecVisitorInvite create(SecVisitorInvite invite) {
        if (invite.getVisitStart() == null) {
            invite.setVisitStart(LocalDateTime.now());
        }
        if (invite.getVisitEnd() == null) {
            invite.setVisitEnd(invite.getVisitStart().plusDays(1));
        }
        if (invite.getVisitEnd().isBefore(invite.getVisitStart())) {
            throw BizException.badRequest("到访结束时间不能早于开始时间");
        }
        invite.setInviteNo(NoGenerator.inviteNo());
        invite.setQrCode("VIS-" + invite.getInviteNo());
        invite.setStatus(0);
        baseMapper.insert(invite);
        return invite;
    }

    public void modify(SecVisitorInvite invite) {
        baseMapper.updateById(invite);
    }

    public void cancel(Long id) {
        SecVisitorInvite invite = baseMapper.selectById(id);
        if (invite == null) {
            throw BizException.notFound("邀请不存在: " + id);
        }
        invite.setStatus(3);
        baseMapper.updateById(invite);
    }

    /** 访客扫码签到: 校验凭证与有效期, 记录通行 */
    public SecVisitorInvite checkIn(String qrCode) {
        if (!StringUtils.hasText(qrCode)) {
            throw BizException.badRequest("二维码凭证不能为空");
        }
        SecVisitorInvite invite = baseMapper.selectOne(new LambdaQueryWrapper<SecVisitorInvite>()
                .eq(SecVisitorInvite::getQrCode, qrCode));
        if (invite == null) {
            throw BizException.notFound("无效的访客凭证");
        }
        LocalDateTime now = LocalDateTime.now();
        if (invite.getStatus() == 3) {
            throw BizException.badRequest("该邀请已撤销");
        }
        if (now.isBefore(invite.getVisitStart()) || now.isAfter(invite.getVisitEnd())) {
            invite.setStatus(2);
            baseMapper.updateById(invite);
            throw BizException.badRequest("访客凭证已过期");
        }
        invite.setStatus(1);
        invite.setArriveTime(now);
        baseMapper.updateById(invite);
        log.info("[visitor] 访客 {} 到访签到成功", invite.getVisitorName());
        return invite;
    }

    /** 将过期邀请批量置为已过期 */
    public int expireOverdue() {
        List<SecVisitorInvite> list = baseMapper.selectList(new LambdaQueryWrapper<SecVisitorInvite>()
                .eq(SecVisitorInvite::getStatus, 0));
        int count = 0;
        for (SecVisitorInvite invite : list) {
            if (LocalDateTime.now().isAfter(invite.getVisitEnd())) {
                invite.setStatus(2);
                baseMapper.updateById(invite);
                count++;
            }
        }
        return count;
    }
}
