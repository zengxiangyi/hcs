package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.entity.SysRoleUser;
import com.baogang.info.service.SysRoleUserService;
import com.baogang.info.tool.StringTool;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
@RequestMapping("/sysRoleUser")
public class SysRoleUserController {

    private final SysRoleUserService sysRoleUserService;

    public SysRoleUserController(SysRoleUserService sysRoleUserService) {
        this.sysRoleUserService = sysRoleUserService;
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<SysRoleUser>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageParam p = PageParam.of(page, size);
        return ApiResponse.success(sysRoleUserService.listPaged(p.offset(), p.size()));
    }

    @GetMapping("/{id}")
    public ApiResponse<SysRoleUser> getById(@PathVariable Long id) {
        return ApiResponse.success(sysRoleUserService.getById(id));
    }

    @PostMapping("/save")
    public ApiResponse<String> save(@Valid @RequestBody Map<String,Object> body) {
        String roleCode= body.get("roleCode")!=null?body.get("roleCode").toString():null;
        List<String> userCodes=(List<String>)body.get("userCodes");
        if(StringTool.isBlank(roleCode)||userCodes==null||userCodes.size()==0){
            return ApiResponse.error(400,"参数错误");
        }
        for(String userCode:userCodes) {
            SysRoleUser sysRoleUser=new SysRoleUser();
            sysRoleUser.setRoleCode(roleCode);
            sysRoleUser.setUserCode(userCode);
            // 避免重复增加
            if(sysRoleUserService.findByRoleCodeAndUserCode(roleCode,userCode)!=null){
                continue;
            }else {
                sysRoleUserService.save(sysRoleUser);
            }
        }
        return ApiResponse.success("保存成功");
    }

    @PutMapping("/{id}")
    public ApiResponse<SysRoleUser> update(@PathVariable Long id, @Valid @RequestBody SysRoleUser sysRoleUser) {
        return ApiResponse.success(sysRoleUserService.update(id, sysRoleUser));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sysRoleUserService.deleteById(id);
        return ApiResponse.success();
    }

    @GetMapping("/role")
    public ApiResponse<Map<String,Object>> findByRoleCode(@RequestParam String roleCode) {
        Map<String, Object> map=new HashMap<>();
        map.put("roleCode",roleCode);
        List<SysRoleUser> data=sysRoleUserService.findByRoleCode(roleCode);
        if(data!=null&&data.size()>0){
           List<String> users=data.stream().map(SysRoleUser::getUserCode).collect(Collectors.toList());
            map.put("userCodes",users);
        }
        return ApiResponse.success(map);
    }

    @GetMapping("/user")
    public ApiResponse<Map<String, Object>> findByUserCode(@RequestParam String userCode) {
        Map<String, Object> map=new HashMap<>();
        map.put("userCode",userCode);
        List<SysRoleUser> data=sysRoleUserService.findByUserCode(userCode);
        if(data!=null&&data.size()>0){
            List<String> roles=data.stream().map(SysRoleUser::getRoleCode).collect(Collectors.toList());
            map.put("roleCodes",roles);
        }
        return ApiResponse.success(map);
    }

    //
}
