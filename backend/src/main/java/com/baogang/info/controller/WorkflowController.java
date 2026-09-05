package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.WorkflowQuery;
import com.baogang.info.entity.Workflow;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.service.SysRoleUserService;
import com.baogang.info.service.WorkflowService;
import com.baogang.info.tool.StringTool;
import com.baogang.info.tool.UserInfo;
import jakarta.validation.Valid;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        return ApiResponse.success(workflowService.listPaged(p.offset(), p.size()));
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
    public ApiResponse<PageResult<Workflow>> sender(){
        // 查询员工工号
        String user= UserInfo.currentUsername();
        if(StringTool.isNotBlank(user)){
            return ApiResponse.success(workflowService.findBySender(user,0,30));
        }
        return ApiResponse.success(null);
    }
    // 我的代办
    @PostMapping("/todo")
    public ApiResponse<PageResult<Workflow>> todo(@Valid @RequestBody WorkflowQuery query){
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
                query.setPage(p.page());
                query.setPageSize(p.size());
                return ApiResponse.success(workflowService.todo(query, p.page(), p.size()));
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
            query.setPage(p.page());
            query.setPageSize(p.size());
            return ApiResponse.success(workflowService.done(query, p.page(), p.size()));
        }
        return ApiResponse.success(null);
    }

}
