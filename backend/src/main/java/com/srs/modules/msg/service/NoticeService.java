package com.srs.modules.msg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.msg.entity.BizNotice;
import com.srs.modules.msg.entity.MsgSendLog;
import com.srs.modules.msg.mapper.BizNoticeMapper;
import com.srs.modules.msg.mapper.MsgSendLogMapper;
import com.srs.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知公告 + 站内信
 */
@Service
@RequiredArgsConstructor
public class NoticeService extends ServiceImpl<BizNoticeMapper, BizNotice> {

    private final MsgSendLogMapper msgMapper;

    public IPage<BizNotice> pageNotices(PageQuery q, Long communityId, String title, Integer noticeType,
                                        Integer publishStatus) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<BizNotice>()
                        .eq(communityId != null, BizNotice::getCommunityId, communityId)
                        .like(StringUtils.hasText(title), BizNotice::getTitle, title)
                        .eq(noticeType != null, BizNotice::getNoticeType, noticeType)
                        .eq(publishStatus != null, BizNotice::getPublishStatus, publishStatus)
                        .orderByDesc(BizNotice::getTopFlag)
                        .orderByDesc(BizNotice::getCreateTime));
    }

    public void saveNotice(BizNotice notice) {
        if (notice.getId() == null) {
            if (notice.getPublishStatus() != null && notice.getPublishStatus() == 1) {
                notice.setPublishTime(LocalDateTime.now());
            }
            baseMapper.insert(notice);
        } else {
            baseMapper.updateById(notice);
        }
    }

    public void publish(Long id) {
        BizNotice notice = baseMapper.selectById(id);
        if (notice == null) {
            throw BizException.notFound("公告不存在: " + id);
        }
        notice.setPublishStatus(1);
        notice.setPublishTime(LocalDateTime.now());
        baseMapper.updateById(notice);
    }

    public void removeNotices(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            baseMapper.deleteByIds(ids);
        }
    }

    public BizNotice detail(Long id) {
        BizNotice notice = baseMapper.selectById(id);
        if (notice != null) {
            notice.setViewCount((notice.getViewCount() == null ? 0 : notice.getViewCount()) + 1);
            baseMapper.updateById(notice);
        }
        return notice;
    }

    /** 我的消息(站内信) */
    public List<MsgSendLog> myMessages(Long userId) {
        return msgMapper.selectList(new LambdaQueryWrapper<MsgSendLog>()
                .eq(MsgSendLog::getReceiverId, userId)
                .eq(MsgSendLog::getChannelType, 1)
                .orderByDesc(MsgSendLog::getCreateTime));
    }

    public void readMessage(Long id) {
        MsgSendLog msg = msgMapper.selectById(id);
        if (msg != null) {
            msg.setReadFlag(1);
            msgMapper.updateById(msg);
        }
    }

    /** 发送站内信(告警/工单/访客等业务复用) */
    public void sendStationMessage(Long receiverId, String receiverName, String title, String content) {
        MsgSendLog log = new MsgSendLog();
        log.setChannelType(1);
        log.setReceiverId(receiverId);
        log.setReceiverName(receiverName);
        log.setTitle(title);
        log.setContent(content);
        log.setSendStatus(1);
        log.setReadFlag(0);
        log.setSendTime(LocalDateTime.now());
        log.setCreateTime(LocalDateTime.now());
        msgMapper.insert(log);
    }

    public IPage<MsgSendLog> pageSendLogs(PageQuery q, String receiverName, Integer channelType) {
        return msgMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<MsgSendLog>()
                        .like(StringUtils.hasText(receiverName), MsgSendLog::getReceiverName, receiverName)
                        .eq(channelType != null, MsgSendLog::getChannelType, channelType)
                        .orderByDesc(MsgSendLog::getCreateTime));
    }

    public long unreadCount() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return 0;
        }
        return msgMapper.selectCount(new LambdaQueryWrapper<MsgSendLog>()
                .eq(MsgSendLog::getReceiverId, userId)
                .eq(MsgSendLog::getChannelType, 1)
                .eq(MsgSendLog::getReadFlag, 0));
    }
}
