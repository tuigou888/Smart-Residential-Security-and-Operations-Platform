package com.srs.modules.msg.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.msg.entity.MsgTemplate;
import com.srs.modules.msg.service.MsgTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "消息模板")
@RestController
@RequestMapping("/api/v1/msg/template")
@RequiredArgsConstructor
public class MsgTemplateController {

    private final MsgTemplateService templateService;

    @Operation(summary = "消息模板列表")
    @PreAuthorize("@ss.has('msg:template:list')")
    @GetMapping("/list")
    public R<PageResult<MsgTemplate>> list(PageQuery query,
                                           @RequestParam(required = false) String templateName,
                                           @RequestParam(required = false) Integer channelType) {
        return R.data(PageResult.of(templateService.page(query, templateName, channelType)));
    }

    @Operation(summary = "新增消息模板")
    @PreAuthorize("@ss.has('msg:template:add')")
    @OperLog(module = "消息模板", businessType = 1)
    @PostMapping
    public R<Void> save(@RequestBody MsgTemplate template) {
        template.setId(null);
        templateService.saveTemplate(template);
        return R.ok("保存成功");
    }

    @Operation(summary = "修改消息模板")
    @PreAuthorize("@ss.has('msg:template:edit')")
    @OperLog(module = "消息模板", businessType = 2)
    @PutMapping
    public R<Void> update(@RequestBody MsgTemplate template) {
        if (template.getId() == null) {
            return R.fail(400, "模板ID不能为空");
        }
        templateService.saveTemplate(template);
        return R.ok("保存成功");
    }

    @Operation(summary = "删除消息模板")
    @PreAuthorize("@ss.has('msg:template:remove')")
    @OperLog(module = "消息模板", businessType = 3)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        templateService.removeTemplates(ids);
        return R.ok("删除成功");
    }

    @Operation(summary = "渲染消息模板")
    @PreAuthorize("@ss.has('msg:template:list')")
    @PostMapping("/render")
    public R<String> render(@RequestBody RenderBody body) {
        return R.data(templateService.render(body.templateId(), body.params()));
    }

    /** 渲染请求 */
    public record RenderBody(Long templateId, Map<String, Object> params) {
    }
}
