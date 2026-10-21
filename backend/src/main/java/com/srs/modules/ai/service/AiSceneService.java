package com.srs.modules.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.ai.entity.AiSceneConfig;
import com.srs.modules.ai.mapper.AiSceneConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI 场景配置
 */
@Service
@RequiredArgsConstructor
public class AiSceneService extends ServiceImpl<AiSceneConfigMapper, AiSceneConfig> {

    public IPage<AiSceneConfig> page(PageQuery q, Long communityId, String sceneName) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<AiSceneConfig>()
                        .eq(communityId != null, AiSceneConfig::getCommunityId, communityId)
                        .like(StringUtils.hasText(sceneName), AiSceneConfig::getSceneName, sceneName)
                        .orderByDesc(AiSceneConfig::getId));
    }

    public void saveScene(AiSceneConfig scene) {
        if (scene.getCommunityId() == null) {
            throw BizException.badRequest("小区ID不能为空");
        }
        if (!StringUtils.hasText(scene.getSceneCode())) {
            throw BizException.badRequest("场景编码不能为空");
        }
        if (!StringUtils.hasText(scene.getSceneName())) {
            throw BizException.badRequest("场景名称不能为空");
        }
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<AiSceneConfig>()
                .eq(AiSceneConfig::getCommunityId, scene.getCommunityId())
                .eq(AiSceneConfig::getSceneCode, scene.getSceneCode())
                .ne(scene.getId() != null, AiSceneConfig::getId, scene.getId()));
        if (count != null && count > 0) {
            throw BizException.conflict("场景编码在该小区已存在");
        }
        if (scene.getThreshold() == null) {
            scene.setThreshold(new BigDecimal("0.80"));
        }
        if (scene.getAlarmLevel() == null) {
            scene.setAlarmLevel(2);
        }
        if (scene.getStatus() == null) {
            scene.setStatus(1);
        }
        if (scene.getId() == null) {
            baseMapper.insert(scene);
        } else {
            baseMapper.updateById(scene);
        }
    }

    public void removeScenes(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw BizException.badRequest("请选择要删除的 AI 场景");
        }
        baseMapper.deleteBatchIds(ids);
    }
}
