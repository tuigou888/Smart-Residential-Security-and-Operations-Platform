package com.srs.modules.base.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.common.util.AesCrypto;
import com.srs.modules.base.entity.BaseOwner;
import com.srs.modules.base.entity.BaseRoomResident;
import com.srs.modules.base.mapper.BaseOwnerMapper;
import com.srs.modules.base.mapper.BaseRoomMapper;
import com.srs.modules.base.mapper.BaseRoomResidentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 业主/住户管理: 手机号与身份证 AES 加密存储 + 脱敏展示
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BaseOwnerService extends ServiceImpl<BaseOwnerMapper, BaseOwner> {

    private final BaseRoomResidentMapper residentMapper;
    private final BaseRoomMapper roomMapper;
    private final AesCrypto aesCrypto;

    public IPage<BaseOwner> pageOwners(PageQuery q, String ownerName, String phone, Integer userType,
                                       Integer authStatus, Long communityId) {
        Page<BaseOwner> page = new Page<>(q.getPageNum(), q.getPageSize());
        String phoneCipher = StringUtils.hasText(phone) ? aesCrypto.encrypt(phone) : null;
        IPage<BaseOwner> result = baseMapper.selectPage(page, new LambdaQueryWrapper<BaseOwner>()
                .like(StringUtils.hasText(ownerName), BaseOwner::getOwnerName, ownerName)
                .and(phoneCipher != null, w -> w.eq(BaseOwner::getPhone, phoneCipher)
                        .or().eq(BaseOwner::getPhoneMask, phone))
                .eq(userType != null, BaseOwner::getUserType, userType)
                .eq(authStatus != null, BaseOwner::getAuthStatus, authStatus)
                .orderByDesc(BaseOwner::getId));
        for (BaseOwner owner : result.getRecords()) {
            decrypt(owner);
            owner.setRoomIds(residentMapper.selectList(new LambdaQueryWrapper<BaseRoomResident>()
                            .eq(BaseRoomResident::getOwnerId, owner.getId())
                            .eq(BaseRoomResident::getStatus, 1))
                    .stream().map(BaseRoomResident::getRoomId).toList());
        }
        return result;
    }

    public BaseOwner detail(Long id) {
        BaseOwner owner = baseMapper.selectById(id);
        if (owner == null) {
            throw BizException.notFound("业主不存在: " + id);
        }
        decrypt(owner);
        owner.setRoomIds(residentMapper.selectList(new LambdaQueryWrapper<BaseRoomResident>()
                        .eq(BaseRoomResident::getOwnerId, id))
                .stream().map(BaseRoomResident::getRoomId).toList());
        return owner;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOwner(BaseOwner owner) {
        if (StringUtils.hasText(owner.getPhone())) {
            owner.setPhoneMask(AesCrypto.maskPhone(owner.getPhone()));
            owner.setPhone(aesCrypto.encrypt(owner.getPhone()));
        } else {
            owner.setPhone(null);
        }
        if (StringUtils.hasText(owner.getIdCard())) {
            owner.setIdCardMask(AesCrypto.maskIdCard(owner.getIdCard()));
            owner.setIdCard(aesCrypto.encrypt(owner.getIdCard()));
        } else {
            owner.setIdCard(null);
        }
        if (owner.getId() == null) {
            baseMapper.insert(owner);
        } else {
            baseMapper.updateById(owner);
        }
    }

    public void remove(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (Long id : ids) {
            residentMapper.delete(new LambdaQueryWrapper<BaseRoomResident>()
                    .eq(BaseRoomResident::getOwnerId, id));
        }
        baseMapper.deleteByIds(ids);
    }

    /** 绑定房屋 */
    @Transactional(rollbackFor = Exception.class)
    public void bindRoom(Long ownerId, Long roomId, Long communityId, Integer relation) {
        BaseOwner owner = baseMapper.selectById(ownerId);
        if (owner == null) {
            throw BizException.notFound("业主不存在: " + ownerId);
        }
        if (roomMapper.selectById(roomId) == null) {
            throw BizException.notFound("房屋不存在: " + roomId);
        }
        long exists = residentMapper.selectCount(new LambdaQueryWrapper<BaseRoomResident>()
                .eq(BaseRoomResident::getOwnerId, ownerId)
                .eq(BaseRoomResident::getRoomId, roomId)
                .eq(BaseRoomResident::getStatus, 1));
        if (exists > 0) {
            throw BizException.conflict("该房屋已绑定");
        }
        BaseRoomResident resident = new BaseRoomResident();
        resident.setCommunityId(communityId);
        resident.setRoomId(roomId);
        resident.setOwnerId(ownerId);
        resident.setRelation(relation == null ? 1 : relation);
        resident.setMoveInDate(java.time.LocalDate.now());
        resident.setStatus(1);
        residentMapper.insert(resident);

        if (roomMapper.selectById(roomId) != null) {
            com.srs.modules.base.entity.BaseRoom room = new com.srs.modules.base.entity.BaseRoom();
            room.setId(roomId);
            room.setOwnerStatus(1);
            roomMapper.updateById(room);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void unbindRoom(Long ownerId, Long roomId) {
        residentMapper.delete(new LambdaQueryWrapper<BaseRoomResident>()
                .eq(BaseRoomResident::getOwnerId, ownerId)
                .eq(BaseRoomResident::getRoomId, roomId));
    }

    /** 人口/房屋统计用: 已认证业主数 */
    public long countOwners(Long communityId) {
        return baseMapper.selectCount(new LambdaQueryWrapper<BaseOwner>());
    }

    private void decrypt(BaseOwner owner) {
        if (owner.getPhone() != null) {
            String plain = aesCrypto.decrypt(owner.getPhone());
            owner.setPhone(plain);
            if (owner.getPhoneMask() == null) {
                owner.setPhoneMask(AesCrypto.maskPhone(plain));
            }
        }
        if (owner.getIdCard() != null) {
            String plain = aesCrypto.decrypt(owner.getIdCard());
            owner.setIdCard(plain);
            if (owner.getIdCardMask() == null) {
                owner.setIdCardMask(AesCrypto.maskIdCard(plain));
            }
        }
    }

    public List<BaseOwner> listByRoom(Long roomId) {
        List<Long> ownerIds = residentMapper.selectList(new LambdaQueryWrapper<BaseRoomResident>()
                        .eq(BaseRoomResident::getRoomId, roomId)
                        .eq(BaseRoomResident::getStatus, 1))
                .stream().map(BaseRoomResident::getOwnerId).toList();
        List<BaseOwner> list = new ArrayList<>();
        for (Long id : ownerIds) {
            BaseOwner o = baseMapper.selectById(id);
            if (o != null) {
                decrypt(o);
                list.add(o);
            }
        }
        return list;
    }
}
