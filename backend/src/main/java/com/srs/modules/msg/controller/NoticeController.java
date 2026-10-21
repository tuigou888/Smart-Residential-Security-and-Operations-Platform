package com.srs.modules.msg.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.msg.entity.BizNotice;
import com.srs.modules.msg.entity.MsgSendLog;
import com.srs.modules.msg.service.NoticeService;
import com.srs.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "消息通知")
@RestController
@RequestMapping("/api/v1/msg")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "公告列表")
    @PreAuthorize("@ss.has('msg:notice:list')")
    @GetMapping("/notice/list")
    public R<PageResult<BizNotice>> list(PageQuery query,
                                         @RequestParam(required = false) Long communityId,
                                         @RequestParam(required = false) String title,
                                         @RequestParam(required = false) Integer noticeType,
                                         @RequestParam(required = false) Integer publishStatus) {
        return R.data(PageResult.of(noticeService.pageNotices(query, communityId, title, noticeType, publishStatus)));
    }

    @Operation(summary = "公告详情")
    @PreAuthorize("@ss.has('msg:notice:list')")
    @GetMapping("/notice/{id}")
    public R<BizNotice> detail(@PathVariable Long id) {
        return R.data(noticeService.detail(id));
    }

    @Operation(summary = "新增/修改公告")
    @PreAuthorize("@ss.has('msg:notice:add')")
    @OperLog(module = "消息通知", businessType = 1)
    @PostMapping("/notice")
    public R<Void> save(@RequestBody BizNotice notice) {
        noticeService.saveNotice(notice);
        return R.ok("保存成功");
    }

    @Operation(summary = "修改公告")
    @PreAuthorize("@ss.has('msg:notice:edit')")
    @PutMapping("/notice")
    public R<Void> update(@RequestBody BizNotice notice) {
        noticeService.saveNotice(notice);
        return R.ok("修改成功");
    }

    @Operation(summary = "发布公告")
    @PreAuthorize("@ss.has('msg:notice:edit')")
    @OperLog(module = "消息通知", businessType = 2)
    @PutMapping("/notice/publish/{id}")
    public R<Void> publish(@PathVariable Long id) {
        noticeService.publish(id);
        return R.ok("已发布");
    }

    @Operation(summary = "删除公告")
    @PreAuthorize("@ss.has('msg:notice:remove')")
    @OperLog(module = "消息通知", businessType = 3)
    @DeleteMapping("/notice/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        noticeService.removeNotices(ids);
        return R.ok("删除成功");
    }

    @Operation(summary = "我的消息")
    @GetMapping("/my/list")
    public R<List<MsgSendLog>> myMessages() {
        return R.data(noticeService.myMessages(SecurityUtils.getUserId()));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/my/read/{id}")
    public R<Void> read(@PathVariable Long id) {
        noticeService.readMessage(id);
        return R.ok("已读");
    }

    @Operation(summary = "未读消息数")
    @GetMapping("/my/unread")
    public R<Map<String, Object>> unread() {
        return R.data(Map.of("count", noticeService.unreadCount()));
    }

    @Operation(summary = "消息发送记录")
    @PreAuthorize("@ss.has('msg:notice:list')")
    @GetMapping("/log/list")
    public R<PageResult<MsgSendLog>> sendLogs(PageQuery query,
                                              @RequestParam(required = false) String receiverName,
                                              @RequestParam(required = false) Integer channelType) {
        return R.data(PageResult.of(noticeService.pageSendLogs(query, receiverName, channelType)));
    }
}
