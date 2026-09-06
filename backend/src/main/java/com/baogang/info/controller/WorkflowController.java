package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.common.Todo;
import com.baogang.info.dto.WorkflowQuery;
import com.baogang.info.entity.Workflow;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.service.SysRoleUserService;
import com.baogang.info.service.WorkflowService;
import com.baogang.info.tool.StringTool;
import com.baogang.info.tool.UserInfo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    private final WorkflowService workflowService;
    private final SysRoleUserService sysRoleUserService;

    public WorkflowController(WorkflowService workflowService,SysRoleUserService sysRoleUserService) {
        this.workflowService = workflowService;
        this.sysRoleUserService=sysRoleUserService;
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<Workflow>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageParam p = PageParam.of(page, size);
        return ApiResponse.success(workflowService.listPaged(p.page0(), p.size()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Workflow> getById(@PathVariable Long id) {
        Workflow workflow = workflowService.getById(id);
        if (workflow == null) {
            throw new ResourceNotFoundException("workflow not found: " + id);
        }
        return ApiResponse.success(workflow);
    }

    @GetMapping("/code/{code}")
    public ApiResponse<Workflow> getByCode(@PathVariable String code) {
        Workflow workflow = workflowService.getByCode(code);
        if (workflow == null) {
            throw new ResourceNotFoundException("workflow not found: " + code);
        }
        return ApiResponse.success(workflow);
    }

    @PostMapping("/save")
    public ApiResponse<Workflow> save(@Valid @RequestBody Workflow workflow) {
        return ApiResponse.success(workflowService.save(workflow));
    }

    @PutMapping("/update")
    public ApiResponse<Workflow> update(@Valid @RequestBody Workflow workflow) {
        if (workflow.getId() == null) {
            throw new IllegalArgumentException("修改操作必须传入 id");
        }
        return ApiResponse.success(workflowService.update(workflow.getId(), workflow));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        workflowService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/state/{state}")
    public ApiResponse<List<Workflow>> findByState(@PathVariable String state) {
        return ApiResponse.success(workflowService.findByState(state));
    }

    @GetMapping("/flowGraph/{flowGraph}")
    public ApiResponse<List<Workflow>> findByFlowGraph(@PathVariable String flowGraph) {
        return ApiResponse.success(workflowService.findByFlowGraph(flowGraph));
    }


    // 我发起的
    @GetMapping("/sender")
    public ApiResponse<PageResult<Workflow>> sender(@Valid @RequestBody WorkflowQuery query){
        // 查询员工工号
        String user= UserInfo.currentUsername();
        if(StringTool.isNotBlank(user)){
            PageParam p = PageParam.of(query.getPage(), query.getPageSize());
            return ApiResponse.success(workflowService.findBySender(user, p.page0(), p.size()));
        }
        return ApiResponse.success(null);
    }
    // 我的代办
    @PostMapping("/todo")
    public ApiResponse<PageResult<Todo>> todo(@Valid @RequestBody WorkflowQuery query){
        // 查询员工工号
        String user= UserInfo.currentUsername();
        if(StringTool.isNotBlank(user)){
            //查询角色
            List<String> roles=sysRoleUserService.findRolesByuserCode(user);
            if(roles!=null&&roles.size()>0){
                // 按角色查询代办
                query.setDealUser(user);
                query.setRoleCode(roles.get(0));
                PageParam p = PageParam.of(query.getPage(), query.getPageSize());
                return ApiResponse.success(workflowService.todo(query, p.page0(), p.size()));
            }
        }
        return ApiResponse.success(null);
    }

    // 我的已办
    @PostMapping("/done")
    public ApiResponse<PageResult<Workflow>> done(@Valid @RequestBody WorkflowQuery query){
        // 查询员工工号
        String user= UserInfo.currentUsername();
        if(StringTool.isNotBlank(user)){
            query.setDealUser(user);
            PageParam p = PageParam.of(query.getPage(), query.getPageSize());
            return ApiResponse.success(workflowService.done(query, p.page0(), p.size()));
        }
        return ApiResponse.success(null);
    }

    @PostMapping("/changeState")
    public ApiResponse<String> done(@Valid @RequestBody Map<String,String> param){
        // 查询参数
        String code=param.get("code");
        String state=param.get("state");
        if(StringTool.isNotBlank(code)&&StringTool.isNotBlank(state)){
            String result=workflowService.changeState(code,state);
            return ApiResponse.success(result);
        }
        return ApiResponse.error(400,"参数错误");
    }

}
