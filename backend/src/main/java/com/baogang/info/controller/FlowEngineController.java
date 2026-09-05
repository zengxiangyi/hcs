package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.service.FlowEngine;
import com.baogang.info.service.SysRoleUserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/flowEngine")
public class FlowEngineController {

    private final FlowEngine flowEngine;
    private final SysRoleUserService sysRoleUserService;

    public FlowEngineController(FlowEngine flowEngine,SysRoleUserService sysRoleUserService) {
        this.flowEngine = flowEngine;
        this.sysRoleUserService=sysRoleUserService;
    }

    @PostMapping("/start")
    public ApiResponse<String> start(@RequestBody Map<String, String> params){
        if (params.get("flowType") == null || params.get("flowType").isBlank()) {
            throw new IllegalArgumentException("flowType 不能为空");
        }
        return ApiResponse.success(flowEngine.start(params.get("flowType")));
    }

    @PostMapping("/deal")
    public ApiResponse<String> dealNode(@RequestBody Map<String, String> params){

        flowEngine.dealNode(params.get("workflow"),
                            params.get("flowGraph"),
                            params.get("edge"));
        return ApiResponse.success("success");
    }

    @GetMapping("/flowGraph/{flowGraph}")
    public ApiResponse<Map<String,Object>> current(@PathVariable String flowGraph){
        Map<String,Object> data=flowEngine.getFlowGraph(flowGraph);
        return ApiResponse.success(data);
    }

}
