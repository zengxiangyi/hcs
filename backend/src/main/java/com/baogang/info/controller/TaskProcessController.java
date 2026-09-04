package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.TaskProcessQuery;
import com.baogang.info.entity.TaskProcess;
import com.baogang.info.service.TaskProcessService;
import com.baogang.info.tool.DateTimeTool;
import com.baogang.info.tool.JsonTool;
import com.baogang.info.tool.UserInfo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        int page = query.getPage();
        int size = query.getPageSize();
        return ApiResponse.success(taskProcessService.search(query, page - 1, size));
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<TaskProcess>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(taskProcessService.listPaged(page - 1, size));
    }

    @PostMapping("/save")
    public ApiResponse<TaskProcess> save(@Valid @RequestBody TaskProcess taskProcess) {
        if (taskProcess == null) {
            return ApiResponse.error(400, "请求体不能为空");
        }
        return ApiResponse.success(taskProcessService.save(taskProcess));
    }

    @PostMapping("/bind")
    public ApiResponse<String> bind(@Valid @RequestBody Map<String,String> bindMap) {
        if (bindMap == null) {
            return ApiResponse.error(400, "请求体不能为空");
        }
        JsonTool.print(bindMap);
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
        if (taskProcess == null) {
            return ApiResponse.error(400, "请求体不能为空");
        }
        if (taskProcess.getId() == null) {
            return ApiResponse.error(400, "修改操作必须传入 id");
        }
        try {
            return ApiResponse.success(taskProcessService.update(taskProcess));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskProcess> getById(@PathVariable Long id) {
        TaskProcess taskProcess = taskProcessService.getById(id);
        if (taskProcess == null) {
            return ApiResponse.error(400, "任务流程不存在");
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
