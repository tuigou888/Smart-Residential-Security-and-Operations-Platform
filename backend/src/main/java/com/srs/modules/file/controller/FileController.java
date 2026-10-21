package com.srs.modules.file.controller;

import com.srs.common.core.R;
import com.srs.modules.file.entity.FileUpload;
import com.srs.modules.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Tag(name = "文件管理")
@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "文件上传")
    @PostMapping("/upload")
    public R<FileUpload> upload(@RequestParam("file") MultipartFile file,
                                @RequestParam(required = false) String bizType,
                                @RequestParam(required = false) Long bizId) {
        return R.data(fileService.upload(file, bizType, bizId));
    }

    @Operation(summary = "文件下载")
    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id) throws IOException {
        FileUpload info = fileService.info(id);
        Path path = fileService.resolve(id);
        InputStreamResource resource = new InputStreamResource(Files.newInputStream(path));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + info.getFileName() + "\"")
                .body(resource);
    }

    @Operation(summary = "文件删除")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        fileService.remove(id);
        return R.ok("删除成功");
    }

    @Operation(summary = "业务附件列表")
    @GetMapping("/list")
    public R<List<FileUpload>> list(@RequestParam(required = false) String bizType,
                                    @RequestParam(required = false) Long bizId) {
        return R.data(fileService.listByBiz(bizType, bizId));
    }
}
