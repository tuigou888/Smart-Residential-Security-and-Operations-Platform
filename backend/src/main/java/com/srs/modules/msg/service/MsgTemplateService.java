package com.srs.modules.msg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.msg.entity.MsgTemplate;
import com.srs.modules.msg.mapper.MsgTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息模板: 维护与变量渲染
 */
@Service
@RequiredArgsConstructor
public class MsgTemplateService extends ServiceImpl<MsgTemplateMapper, MsgTemplate> {

    /** 变量占位符: {key} */
    private static final Pattern VAR_PATTERN = Pattern.compile("\\{(\\w+)\\}");

    public IPage<MsgTemplate> page(PageQuery q, String templateName, Integer channelType) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<MsgTemplate>()
                        .like(StringUtils.hasText(templateName), MsgTemplate::getTemplateName, templateName)
                        .eq(channelType != null, MsgTemplate::getChannelType, channelType)
                        .orderByDesc(MsgTemplate::getId));
    }

    public void saveTemplate(MsgTemplate template) {
        if (!StringUtils.hasText(template.getTemplateCode())) {
            throw BizException.badRequest("模板编码不能为空");
        }
        if (!StringUtils.hasText(template.getTemplateName())) {
            throw BizException.badRequest("模板名称不能为空");
        }
        if (!StringUtils.hasText(template.getContentTemplate())) {
            throw BizException.badRequest("模板内容不能为空");
        }
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<MsgTemplate>()
                .eq(MsgTemplate::getTemplateCode, template.getTemplateCode())
                .ne(template.getId() != null, MsgTemplate::getId, template.getId()));
        if (count != null && count > 0) {
            throw BizException.conflict("模板编码已存在");
        }
        if (template.getStatus() == null) {
            template.setStatus(1);
        }
        if (template.getId() == null) {
            baseMapper.insert(template);
        } else {
            baseMapper.updateById(template);
        }
    }

    public void removeTemplates(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw BizException.badRequest("请选择要删除的消息模板");
        }
        baseMapper.deleteBatchIds(ids);
    }

    /**
     * 渲染模板: 将 {key} 替换为参数值
     * <p>
     * 参数缺失时保留原占位符, 不做任何表达式求值。
     */
    public String render(Long templateId, Map<String, Object> params) {
        MsgTemplate template = baseMapper.selectById(templateId);
        if (template == null) {
            throw BizException.notFound("消息模板不存在: " + templateId);
        }
        String source = template.getContentTemplate();
        if (source == null || params == null || params.isEmpty()) {
            return source;
        }
        Matcher matcher = VAR_PATTERN.matcher(source);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = params.containsKey(key) ? String.valueOf(params.get(key)) : matcher.group();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
