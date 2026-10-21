package com.srs.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.exception.BizException;
import com.srs.modules.system.entity.SysDept;
import com.srs.modules.system.mapper.SysDeptMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SysDeptService extends ServiceImpl<SysDeptMapper, SysDept> {

    public List<SysDept> listDepts(String deptName, Integer status) {
        List<SysDept> all = baseMapper.selectList(new LambdaQueryWrapper<SysDept>()
                .like(StringUtils.hasText(deptName), SysDept::getDeptName, deptName)
                .eq(status != null, SysDept::getStatus, status)
                .orderByAsc(SysDept::getOrderNum));
        return buildTree(all);
    }

    public List<SysDept> buildTree(List<SysDept> list) {
        Map<Long, SysDept> map = new LinkedHashMap<>();
        for (SysDept d : list) {
            map.put(d.getId(), d);
        }
        List<SysDept> roots = new ArrayList<>();
        for (SysDept d : list) {
            SysDept parent = map.get(d.getParentId() == null ? 0L : d.getParentId());
            if (parent == null || parent.getId().equals(d.getId())) {
                roots.add(d);
            } else {
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(d);
            }
        }
        return roots;
    }

    public void createDept(SysDept dept) {
        baseMapper.insert(dept);
    }

    public void updateDept(SysDept dept) {
        baseMapper.updateById(dept);
    }

    public void deleteDept(Long id) {
        long children = baseMapper.selectCount(new LambdaQueryWrapper<SysDept>().eq(SysDept::getParentId, id));
        if (children > 0) {
            throw BizException.badRequest("存在下级部门, 不允许删除");
        }
        baseMapper.deleteById(id);
    }
}
