package com.srs.modules.ai.controller;

import com.srs.common.anno.OperLog;
import com.srs.common.core.PageQuery;
import com.srs.common.core.PageResult;
import com.srs.common.core.R;
import com.srs.modules.ai.entity.AiSceneConfig;
import com.srs.modules.ai.service.AiSceneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AI场景配置")
@RestController
@RequestMapping("/api/v1/ai/scene")
@RequiredArgsConstructor
public class AiSceneController {

    private final AiSceneService sceneService;

    @Operation(summary = "AI场景列表")
    @PreAuthorize("@ss.has('ai:scene:list')")
    @GetMapping("/list")
    public R<PageResult<AiSceneConfig>> list(PageQuery query,
                                             @RequestParam(required = false) Long communityId,
                                             @RequestParam(required = false) String sceneName) {
        return R.data(PageResult.of(sceneService.page(query, communityId, sceneName)));
    }

    @Operation(summary = "新增AI场景")
    @PreAuthorize("@ss.has('ai:scene:add')")
    @OperLog(module = "AI场景配置", businessType = 1)
    @PostMapping
    public R<Void> save(@RequestBody AiSceneConfig scene) {
        scene.setId(null);
        sceneService.saveScene(scene);
        return R.ok("保存成功");
    }

    @Operation(summary = "修改AI场景")
    @PreAuthorize("@ss.has('ai:scene:edit')")
    @OperLog(module = "AI场景配置", businessType = 2)
    @PutMapping
    public R<Void> update(@RequestBody AiSceneConfig scene) {
        if (scene.getId() == null) {
            return R.fail(400, "场景ID不能为空");
        }
        sceneService.saveScene(scene);
        return R.ok("保存成功");
    }

    @Operation(summary = "删除AI场景")
    @PreAuthorize("@ss.has('ai:scene:remove')")
    @OperLog(module = "AI场景配置", businessType = 3)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable List<Long> ids) {
        sceneService.removeScenes(ids);
        return R.ok("删除成功");
    }
}
