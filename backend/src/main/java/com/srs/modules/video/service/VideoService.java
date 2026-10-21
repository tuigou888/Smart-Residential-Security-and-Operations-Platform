package com.srs.modules.video.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.core.PageQuery;
import com.srs.common.exception.BizException;
import com.srs.modules.iot.entity.IotDevice;
import com.srs.modules.iot.mapper.IotDeviceMapper;
import com.srs.modules.video.entity.VsChannel;
import com.srs.modules.video.entity.VsRecordPlan;
import com.srs.modules.video.mapper.VsChannelMapper;
import com.srs.modules.video.mapper.VsRecordPlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 视频监控: 通道管理 + 取流地址生成
 * <p>
 * 一期未接入 ZLMediaKit 时(srs.media.enabled=false)返回演示地址, 前端展示占位画面;
 * 接入后将 srs.media.enabled 置为 true 并配置 base-url, 自动切换为真实 FLV/HLS/WebRTC 地址。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService extends ServiceImpl<VsChannelMapper, VsChannel> {

    private final IotDeviceMapper deviceMapper;
    private final VsRecordPlanMapper recordPlanMapper;

    @Value("${srs.media.enabled:false}")
    private boolean mediaEnabled;

    @Value("${srs.media.base-url:http://127.0.0.1:8080}")
    private String mediaBaseUrl;

    public IPage<VsChannel> pageChannels(PageQuery q, Long communityId, String channelName, Integer status) {
        Page<VsChannel> page = new Page<>(q.getPageNum(), q.getPageSize());
        IPage<VsChannel> result = baseMapper.selectPage(page, new LambdaQueryWrapper<VsChannel>()
                .eq(communityId != null, VsChannel::getCommunityId, communityId)
                .like(StringUtils.hasText(channelName), VsChannel::getChannelName, channelName)
                .eq(status != null, VsChannel::getStatus, status)
                .orderByAsc(VsChannel::getChannelNo));
        for (VsChannel c : result.getRecords()) {
            IotDevice d = deviceMapper.selectById(c.getDeviceId());
            if (d != null) {
                c.setDeviceName(d.getDeviceName());
                c.setOnlineStatus(d.getOnlineStatus());
            }
        }
        return result;
    }

    /** 通道树: 小区 -> 设备 -> 通道 */
    public List<Map<String, Object>> channelTree(Long communityId) {
        List<VsChannel> channels = baseMapper.selectList(new LambdaQueryWrapper<VsChannel>()
                .eq(communityId != null, VsChannel::getCommunityId, communityId)
                .orderByAsc(VsChannel::getChannelNo));
        Map<Long, List<VsChannel>> byDevice = new LinkedHashMap<>();
        for (VsChannel c : channels) {
            byDevice.computeIfAbsent(c.getDeviceId(), k -> new ArrayList<>()).add(c);
        }
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Map.Entry<Long, List<VsChannel>> entry : byDevice.entrySet()) {
            IotDevice device = deviceMapper.selectById(entry.getKey());
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("deviceId", entry.getKey());
            node.put("deviceName", device == null ? "未知设备" : device.getDeviceName());
            node.put("online", device != null && device.getOnlineStatus() == 1);
            List<Map<String, Object>> kids = new ArrayList<>();
            for (VsChannel c : entry.getValue()) {
                Map<String, Object> ch = new LinkedHashMap<>();
                ch.put("channelId", c.getId());
                ch.put("channelName", c.getChannelName());
                ch.put("gbCode", c.getGbCode());
                ch.put("hasPtz", c.getHasPtz());
                kids.add(ch);
            }
            node.put("channels", kids);
            tree.add(node);
        }
        return tree;
    }

    public void saveChannel(VsChannel entity) {
        if (entity.getId() == null) {
            baseMapper.insert(entity);
        } else {
            baseMapper.updateById(entity);
        }
    }

    public void removeChannels(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            baseMapper.deleteByIds(ids);
        }
    }

    /** 实时流地址: 优先通道自定义 stream_url, 否则按流媒体服务拼装 */
    public Map<String, Object> liveUrl(Long channelId) {
        VsChannel channel = baseMapper.selectById(channelId);
        if (channel == null) {
            throw BizException.notFound("通道不存在: " + channelId);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("channelId", channelId);
        data.put("channelName", channel.getChannelName());
        data.put("gbCode", channel.getGbCode());
        data.put("hasPtz", channel.getHasPtz());
        if (StringUtils.hasText(channel.getStreamUrl())) {
            data.put("flv", channel.getStreamUrl());
        } else if (mediaEnabled) {
            String app = "rtp";
            String stream = channel.getGbCode() == null ? String.valueOf(channelId) : channel.getGbCode();
            data.put("flv", mediaBaseUrl + "/" + app + "/" + stream + ".live.flv");
            data.put("hls", mediaBaseUrl + "/" + app + "/" + stream + ".live.m3u8");
            data.put("webrtc", mediaBaseUrl.replace("http", "ws") + "/" + app + "/" + stream + ".live.flv");
        } else {
            data.put("flv", null);
            data.put("demo", true);
        }
        data.put("mediaEnabled", mediaEnabled);
        return data;
    }

    /** 回放地址: 一期返回时间范围内回放地址(需流媒体支持录像检索) */
    public Map<String, Object> playbackUrl(Long channelId, String start, String end) {
        VsChannel channel = baseMapper.selectById(channelId);
        if (channel == null) {
            throw BizException.notFound("通道不存在: " + channelId);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("channelId", channelId);
        data.put("start", start);
        data.put("end", end);
        data.put("mediaEnabled", mediaEnabled);
        if (mediaEnabled) {
            String stream = channel.getGbCode() == null ? String.valueOf(channelId) : channel.getGbCode();
            data.put("hls", mediaBaseUrl + "/record/" + stream + "/" + start + "_" + end + ".m3u8");
        }
        return data;
    }

    /** 云台控制: 一期记录指令(实际下发交由设备接入层) */
    public void ptzControl(Long channelId, String direction, Integer speed) {
        log.info("[ptz] channelId={} direction={} speed={}", channelId, direction, speed);
    }

    /** 截图: 一期返回抓拍占位地址 */
    public Map<String, Object> snapshot(Long channelId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("channelId", channelId);
        data.put("mediaEnabled", mediaEnabled);
        data.put("url", null);
        return data;
    }

    public List<VsRecordPlan> recordPlans() {
        return recordPlanMapper.selectList(new LambdaQueryWrapper<VsRecordPlan>()
                .orderByAsc(VsRecordPlan::getId));
    }

    public void saveRecordPlan(VsRecordPlan plan) {
        if (plan.getId() == null) {
            recordPlanMapper.insert(plan);
        } else {
            recordPlanMapper.updateById(plan);
        }
    }

    public void removeRecordPlan(Long id) {
        recordPlanMapper.deleteById(id);
    }
}
