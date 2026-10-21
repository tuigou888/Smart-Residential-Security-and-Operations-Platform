package com.srs.modules.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srs.common.exception.BizException;
import com.srs.modules.file.entity.FileUpload;
import com.srs.modules.file.mapper.FileUploadMapper;
import com.srs.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 本地文件服务(私有化部署场景; 云部署可切换为 MinIO/OSS, 接口保持不变)
 */
@Slf4j
@Service
public class FileService extends ServiceImpl<FileUploadMapper, FileUpload> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final long MAX_SIZE = 20 * 1024 * 1024L;

    @Value("${srs.file.upload-path:./data/upload}")
    private String uploadPath;

    @Value("${srs.file.access-prefix:/api/v1/file/download/}")
    private String accessPrefix;

    public FileUpload upload(MultipartFile file, String bizType, Long bizId) {
        if (file == null || file.isEmpty()) {
            throw BizException.badRequest("文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw BizException.badRequest("文件大小不能超过 20MB");
        }
        try {
            String dateDir = LocalDate.now().format(FMT);
            Path dir = Paths.get(uploadPath, dateDir);
            Files.createDirectories(dir);
            String original = StringUtils.cleanPath(
                    file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename());
            String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
            String name = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = dir.resolve(name);
            try (var in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            FileUpload entity = new FileUpload();
            entity.setFileName(original);
            entity.setFilePath(target.toString());
            entity.setFileUrl(accessPrefix + dateDir + "/" + name);
            entity.setFileSize(file.getSize());
            entity.setFileType(file.getContentType());
            entity.setBizType(bizType);
            entity.setBizId(bizId);
            entity.setUploadUserId(SecurityUtils.getUserId());
            entity.setUploadUserName(SecurityUtils.getUsername());
            entity.setCreateTime(java.time.LocalDateTime.now());
            baseMapper.insert(entity);
            return entity;
        } catch (IOException e) {
            throw new BizException(500, "文件上传失败: " + e.getMessage());
        }
    }

    public FileUpload info(Long id) {
        FileUpload entity = baseMapper.selectById(id);
        if (entity == null) {
            throw BizException.notFound("文件不存在: " + id);
        }
        return entity;
    }

    public Path resolve(Long id) {
        FileUpload entity = info(id);
        Path path = Paths.get(entity.getFilePath());
        if (!Files.exists(path)) {
            throw BizException.notFound("文件已丢失: " + entity.getFileName());
        }
        return path;
    }

    public void remove(Long id) {
        FileUpload entity = info(id);
        try {
            Files.deleteIfExists(Paths.get(entity.getFilePath()));
        } catch (IOException ignored) {
            // 物理文件缺失时仍删除记录
        }
        baseMapper.deleteById(id);
    }

    public List<FileUpload> listByBiz(String bizType, Long bizId) {
        return baseMapper.selectList(new LambdaQueryWrapper<FileUpload>()
                .eq(StringUtils.hasText(bizType), FileUpload::getBizType, bizType)
                .eq(bizId != null, FileUpload::getBizId, bizId)
                .orderByDesc(FileUpload::getCreateTime));
    }
}
