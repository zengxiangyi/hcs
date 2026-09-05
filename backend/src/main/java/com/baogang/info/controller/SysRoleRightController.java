package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.entity.SysRoleRight;
import com.baogang.info.service.SysRoleRightService;
import com.baogang.info.tool.StringTool;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sysRoleRight")
public class SysRoleRightController {

    private final SysRoleRightService sysRoleRightService;

    public SysRoleRightController(SysRoleRightService sysRoleRightService) {
        this.sysRoleRightService = sysRoleRightService;
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<SysRoleRight>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageParam p = PageParam.of(page, size);
        return ApiResponse.success(sysRoleRightService.listPaged(p.page0(), p.size()));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysRoleRight> getById(@PathVariable Long id) {
        return ApiResponse.success(sysRoleRightService.getById(id));
    }

    @PostMapping("/save")
    public ApiResponse<String> save(@Valid @RequestBody Map<String,Object> body) {
        String roleCode= body.get("roleCode")!=null?body.get("roleCode").toString():null;
        List<String> rightCodes=(List<String>)body.get("rightCodes");
        if(StringTool.isBlank(roleCode)||rightCodes==null||rightCodes.size()==0){
            return ApiResponse.error(400,"参数错误");
        }
        for(String rightCode:rightCodes) {
            SysRoleRight sysRoleRight = new SysRoleRight();
            sysRoleRight.setRoleCode(roleCode);
            sysRoleRight.setRightCode(rightCode);
            // 避免重复增加
            if(sysRoleRightService.findByRoleCodeAndRightCode(roleCode,rightCode)!=null){
                continue;
            }else {
                sysRoleRightService.save(sysRoleRight);
            }
        }
        return ApiResponse.success("处理完毕");
    }

    @PutMapping("/update")
    public ApiResponse<SysRoleRight> update(@Valid @RequestBody SysRoleRight sysRoleRight) {
        if (sysRoleRight.getId() == null) {
            throw new IllegalArgumentException("修改操作必须传入 id");
        }
        return ApiResponse.success(sysRoleRightService.update(sysRoleRight.getId(), sysRoleRight));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sysRoleRightService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/role")
    public ApiResponse<Map<String,Object>> findByRoleCode(@RequestParam String roleCode) {
        Map<String,Object>  map = new HashMap<>();
        map.put("roleCode",roleCode);
        List<SysRoleRight> rightList = sysRoleRightService.findByRoleCode(roleCode);
        if(rightList!=null&&rightList.size()>0){
            List<String> data=rightList.stream().map(SysRoleRight::getRightCode).collect(Collectors.toList());
            map.put("rightCodes",data);
        }
        return ApiResponse.success(map);
    }

    @GetMapping("/right/{rightCode}")
    public ApiResponse<List<SysRoleRight>> findByRightCode(@PathVariable String rightCode) {
        return ApiResponse.success(sysRoleRightService.findByRightCode(rightCode));
    }

}
