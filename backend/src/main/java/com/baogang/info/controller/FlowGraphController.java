package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.GraphQuery;
import com.baogang.info.dto.SysRoleQuery;
import com.baogang.info.entity.BluePrint;
import com.baogang.info.entity.FlowGraph;
import com.baogang.info.service.FlowGraphService;
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

@RestController
@RequestMapping("/flowGraph")
public class FlowGraphController {

    private final FlowGraphService flowGraphService;

    public FlowGraphController(FlowGraphService flowGraphService) {
        this.flowGraphService = flowGraphService;
    }

    @PostMapping("/search")
    public ApiResponse<PageResult<FlowGraph>> list(@RequestBody GraphQuery query) {
        PageParam p = PageParam.of(query.getPage(), query.getPageSize());
        return ApiResponse.success(flowGraphService.listPaged(p.offset(), p.size()));
    }

    @GetMapping("/{id}")
    public ApiResponse<FlowGraph> getById(@PathVariable Long id) {
        return ApiResponse.success(flowGraphService.getById(id));
    }

    @PostMapping("/save")
    public ApiResponse<FlowGraph> save(@Valid @RequestBody FlowGraph flowGraph) {
        return ApiResponse.success(flowGraphService.save(flowGraph));
    }

    @PutMapping("/{id}")
    public ApiResponse<FlowGraph> update(@PathVariable Long id, @Valid @RequestBody FlowGraph flowGraph) {
        return ApiResponse.success(flowGraphService.update(id, flowGraph));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        flowGraphService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/flowGraph/{flowGraph}")
    public ApiResponse<FlowGraph> findByWorkflow(@PathVariable String flowGraph) {
        return ApiResponse.success(flowGraphService.findByFlowGraph(flowGraph).get(0));
    }

    // 修改：必须传入 id，createTime/createUser 等创建信息由 service 保留原值
    @PutMapping("/update")
    public ApiResponse<FlowGraph> update(@Valid @RequestBody FlowGraph flowGraph) {
        if (flowGraph == null) {
            return ApiResponse.error(400, "请求体不能为空");
        }
        if (flowGraph.getId() == null) {
            return ApiResponse.error(400, "修改操作必须传入 id");
        }
        try {
            return ApiResponse.success(flowGraphService.update(flowGraph.getId(),flowGraph));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}
