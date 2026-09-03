package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.JwtUtil;
import com.baogang.info.common.LoginOut;
import com.baogang.info.entity.SysRoleRight;
import com.baogang.info.entity.SysRoleUser;
import com.baogang.info.entity.SysUser;
import com.baogang.info.service.SysRoleRightService;
import com.baogang.info.service.SysRoleUserService;
import com.baogang.info.service.SysUserService;
import com.baogang.info.tool.CollectionTool;
import com.baogang.info.tool.StringTool;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录和注册账号相关
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final SysUserService sysUserService;
    private final SysRoleUserService sysRoleUserService;
    private final SysRoleRightService sysRoleRightService;
    private final JwtUtil jwtUtil;

    public AuthController(SysUserService sysUserService, SysRoleUserService sysRoleUserService, SysRoleRightService sysRoleRightService, JwtUtil jwtUtil) {
        this.sysUserService = sysUserService;
        this.sysRoleUserService = sysRoleUserService;
        this.sysRoleRightService = sysRoleRightService;
        this.jwtUtil = jwtUtil;
    }

    // 登录
    @PostMapping("/login")
    public ApiResponse<LoginOut> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (StringTool.isBlank(username)) {
            return ApiResponse.error(400, "用户名为空");
        }
        if(StringTool.isBlank(password)){
            return ApiResponse.error(400, "密码为空");
        }
        SysUser ac=sysUserService.getByCode(username);
        if(ac==null){
            return ApiResponse.error(400, "账号不存在");
        }
        if(!password.equals(ac.getPassword())){
            return ApiResponse.error(400, "密码错误");
        }
        String token = jwtUtil.generateToken(username);
        Map<String,Object> map=new HashMap<>();
        map.put("name",ac.getName());
        map.put("username", username);
        map.put("id", ac.getId());

        LoginOut loginOut=new LoginOut(token, map);
        // 缓存角色
        List<SysRoleUser> roleList=sysRoleUserService.findByUserCode(ac.getCode());
        if(CollectionTool.isNotEmptyList(roleList)){
            List<String> roles=roleList.stream().map(SysRoleUser::getRoleCode).toList();
            loginOut.setRoles(roles);
            // 按角色查询权限
            String roleCode=roles.get(0);
            List<SysRoleRight> roleRightList=sysRoleRightService.findByRoleCode(roleCode);
            List<String> rights=roleRightList.stream().map(SysRoleRight::getRightCode).toList();
            loginOut.setRights(rights);
        }
        // 不打印 LoginOut：含 JWT token 与权限列表，明文输出到 stdout 属凭据泄露
        return ApiResponse.success(loginOut);
    }

    // 重置密码
    @PostMapping("/verify")
    public ApiResponse<String> verify(@RequestBody Map<String, String> body) {
        String username=body.get("username");
        String email=body.get("email");
        String cellphone=body.get("cellphone");
        // 参数非空校验
        if(StringTool.isAnyBlank(username,email,cellphone)){
            return ApiResponse.error(400, "用户名,邮箱,手机号都不能为空");
        }
        // 设置新密码
        SysUser sysUser=sysUserService.getByCode(username);
        if(sysUser==null){
            return ApiResponse.error(400, "账号不存在");
        }
        if(!sysUser.getEmail().equals(email)){
            return ApiResponse.error(400, "邮箱不匹配");
        }
        if(!sysUser.getCellphone().equals(cellphone)){
            return ApiResponse.error(400, "手机号不匹配");
        }
        return ApiResponse.success("身份验证成功");
    }

    // 重置密码
    @PostMapping("/resetPassword")
    public ApiResponse<String> resetPassword(@RequestBody Map<String, String> body) {
        String username=body.get("username");
        String password=body.get("password");
        // 参数非空校验
        if(StringTool.isBlank(username)){
            return ApiResponse.error(400, "重置密码用户名不能为空");
        }
        if(StringTool.isBlank(password)){
            return ApiResponse.error(400, "重置新密码不能为空");
        }
        // 设置新密码
        SysUser sysUser=sysUserService.getByCode(username);
        if(sysUser==null){
            return ApiResponse.error(400, "账号不存在");
        }
        sysUser.setPassword(password);
        sysUserService.update(sysUser.getId(), sysUser);
        return ApiResponse.success("密码重置成功");
    }

    // 注册新用户
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody Map<String, String> body) {
        String username=body.get("username");
        String password=body.get("password");
        String email=body.get("email");
        String cellphone=body.get("cellphone");
        // 数据非空校验
        if(StringTool.isBlank(username)){
            return ApiResponse.error(400, "用户名为空");
        }
        if(StringTool.isBlank(password)){
            return ApiResponse.error(400, "密码为空");
        }
        if(StringTool.isBlank(email)){
            return ApiResponse.error(400, "邮箱为空");
        }
        if(StringTool.isBlank(cellphone)){
            return ApiResponse.error(400, "手机号为空");
        }
        SysUser ac=sysUserService.getByCode(username);
        if(ac!=null){
            return ApiResponse.error(400, "账号已存在");
        }
        // 新建用户
        SysUser sysUser=new SysUser();
        sysUser.setCode(username);
        sysUser.setPassword(password);
        sysUser.setEmail(email);
        sysUser.setCellphone(cellphone);
        sysUser.setState("A");
        sysUserService.save(sysUser);
        return ApiResponse.success("注册成功");
    }

}
