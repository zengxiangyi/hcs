package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.TaskProcessQuery;
import com.baogang.info.entity.TaskProcess;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.service.TaskProcessService;
import com.baogang.info.tool.DateTimeTool;
import com.baogang.info.tool.UserInfo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/taskprocess")
public class TaskProcessController {

    private final TaskProcessService taskProcessService;

    public TaskProcessController(TaskProcessService taskProcessService) {
        this.taskProcessService = taskProcessService;
    }

    // 复杂/可变条件查询：POST 请求体承载 TaskProcessQuery，支持任意字段组合过滤
    @PostMapping("/search")
    public ApiResponse<PageResult<TaskProcess>> searchByQuery(@RequestBody TaskProcessQuery query) {
        PageParam p = PageParam.of(query.getPage(), query.getPageSize());
        return ApiResponse.success(taskProcessService.search(query, p.page0(), p.size()));
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<TaskProcess>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageParam p = PageParam.of(page, size);
        return ApiResponse.success(taskProcessService.listPaged(p.page0(), p.size()));
    }

    @PostMapping("/save")
    public ApiResponse<TaskProcess> save(@Valid @RequestBody TaskProcess taskProcess) {
        return ApiResponse.success(taskProcessService.save(taskProcess));
    }

    @PostMapping("/bind")
    public ApiResponse<String> bind(@Valid @RequestBody Map<String,String> bindMap) {
        TaskProcess one=new TaskProcess();
        one.setTransfer(bindMap.get("transfer"));
        one.setBlueprint(bindMap.get("blueprint"));
        one.setCreateTime(DateTimeTool.currentTime());
        one.setCreateUser(UserInfo.currentUsername());
        one.setState("A");
        taskProcessService.save(one);
        return ApiResponse.success("处理完毕");
    }

    @PutMapping("/update")
    public ApiResponse<TaskProcess> update(@Valid @RequestBody TaskProcess taskProcess) {
        if (taskProcess.getId() == null) {
            throw new IllegalArgumentException("修改操作必须传入 id");
        }
        return ApiResponse.success(taskProcessService.update(taskProcess));
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskProcess> getById(@PathVariable Long id) {
        TaskProcess taskProcess = taskProcessService.getById(id);
        if (taskProcess == null) {
            throw new ResourceNotFoundException("任务流程不存在：id=" + id);
        }
        return ApiResponse.success(taskProcess);
    }

    @GetMapping("/state/{state}")
    public ApiResponse<List<TaskProcess>> getByState(@PathVariable String state) {
        return ApiResponse.success(taskProcessService.getByState(state));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteById(@PathVariable Long id) {
        taskProcessService.deleteById(id);
        return ApiResponse.success("删除成功");
    }
}
