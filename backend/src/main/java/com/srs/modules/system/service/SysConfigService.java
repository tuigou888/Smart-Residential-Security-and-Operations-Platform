package com.srs.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.modules.system.entity.SysConfig;
import com.srs.modules.system.mapper.SysConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysConfigService extends ServiceImpl<SysConfigMapper, SysConfig> {

    public IPage<SysConfig> pageConfigs(PageQuery q, String configName, String configKey) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<SysConfig>()
                        .like(StringUtils.hasText(configName), SysConfig::getConfigName, configName)
                        .like(StringUtils.hasText(configKey), SysConfig::getConfigKey, configKey)
                        .orderByAsc(SysConfig::getId));
    }

    public String valueByKey(String key) {
        SysConfig config = baseMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        return config == null ? null : config.getConfigValue();
    }
}
