package com.srs.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.modules.system.entity.SysDictData;
import com.srs.modules.system.entity.SysDictType;
import com.srs.modules.system.mapper.SysDictDataMapper;
import com.srs.modules.system.mapper.SysDictTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictService extends ServiceImpl<SysDictTypeMapper, SysDictType> {

    private final SysDictDataMapper dictDataMapper;

    public IPage<SysDictType> pageTypes(PageQuery q, String dictName, String dictType) {
        return baseMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<SysDictType>()
                        .like(StringUtils.hasText(dictName), SysDictType::getDictName, dictName)
                        .like(StringUtils.hasText(dictType), SysDictType::getDictType, dictType)
                        .orderByAsc(SysDictType::getId));
    }

    public IPage<SysDictData> pageData(PageQuery q, String dictType, String dictLabel) {
        return dictDataMapper.selectPage(new Page<>(q.getPageNum(), q.getPageSize()),
                new LambdaQueryWrapper<SysDictData>()
                        .eq(StringUtils.hasText(dictType), SysDictData::getDictType, dictType)
                        .like(StringUtils.hasText(dictLabel), SysDictData::getDictLabel, dictLabel)
                        .orderByAsc(SysDictData::getDictSort));
    }

    public List<SysDictData> dataByType(String dictType) {
        return dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getDictSort));
    }

    public void saveData(SysDictData data) {
        if (data.getId() == null) {
            dictDataMapper.insert(data);
        } else {
            dictDataMapper.updateById(data);
        }
    }

    public void removeData(Long id) {
        dictDataMapper.deleteById(id);
    }
}
