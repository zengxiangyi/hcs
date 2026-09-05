package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.GraphQuery;
import com.baogang.info.entity.FlowGraph;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.service.FlowGraphService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
        FlowGraph flowGraph = flowGraphService.getById(id);
        if (flowGraph == null) {
            throw new ResourceNotFoundException("flowGraph not found: " + id);
        }
        return ApiResponse.success(flowGraph);
    }

    @PostMapping("/save")
    public ApiResponse<FlowGraph> save(@Valid @RequestBody FlowGraph flowGraph) {
        return ApiResponse.success(flowGraphService.save(flowGraph));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        flowGraphService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/flowGraph/{flowGraph}")
    public ApiResponse<FlowGraph> findByWorkflow(@PathVariable String flowGraph) {
        List<FlowGraph> graphs = flowGraphService.findByFlowGraph(flowGraph);
        if (graphs == null || graphs.isEmpty()) {
            throw new ResourceNotFoundException("流程图不存在：" + flowGraph);
        }
        return ApiResponse.success(graphs.get(0));
    }

    // 修改：必须传入 id，createTime/createUser 等创建信息由 service 保留原值
    @PutMapping("/update")
    public ApiResponse<FlowGraph> update(@Valid @RequestBody FlowGraph flowGraph) {
        if (flowGraph.getId() == null) {
            throw new IllegalArgumentException("修改操作必须传入 id");
        }
        return ApiResponse.success(flowGraphService.update(flowGraph.getId(), flowGraph));
    }
}
