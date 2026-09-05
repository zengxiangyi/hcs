package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.BluePrintQuery;
import com.baogang.info.entity.BluePrint;
import com.baogang.info.service.BluePrintService;
import com.baogang.info.tool.DateTimeTool;
import com.baogang.info.tool.StringTool;
import com.baogang.info.tool.UserInfo;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blueprint")
public class BluePrintController {

    private final BluePrintService bluePrintService;

    public BluePrintController(BluePrintService bluePrintService) {
        this.bluePrintService = bluePrintService;
    }

    // 复杂/可变条件查询：POST 请求体承载 BluePrintQuery，支持任意字段组合过滤
    @PostMapping("/search")
    public ApiResponse<PageResult<BluePrint>> searchByQuery(@RequestBody BluePrintQuery query) {
        PageParam p = PageParam.of(query.getPage(), query.getPageSize());
        return ApiResponse.success(bluePrintService.search(query, p.offset(), p.size()));
    }

    @PostMapping("/save")
    public ApiResponse<BluePrint> save(@Valid @RequestBody BluePrint bluePrint) {
        if (bluePrint == null) {
            return ApiResponse.error(400, "请求体不能为空");
        }
        bluePrint.setCreateTime(DateTimeTool.currentTime());
        bluePrint.setEdition("V1");
        bluePrint.setState("A");
        bluePrint.setCreateUser(UserInfo.currentUsername());
        return ApiResponse.success(bluePrintService.save(bluePrint));
    }

    // 修改：必须传入 id，createTime/createUser 等创建信息由 service 保留原值
    @PutMapping("/update")
    public ApiResponse<BluePrint> update(@Valid @RequestBody BluePrint bluePrint) {
        if (bluePrint == null) {
            return ApiResponse.error(400, "请求体不能为空");
        }
        if (bluePrint.getId() == null) {
            return ApiResponse.error(400, "修改操作必须传入 id");
        }
        try {
            return ApiResponse.success(bluePrintService.update(bluePrint));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/code/{code}")
    public ApiResponse<List<BluePrint>> getByCode(@PathVariable String code) {
        if(StringTool.isBlank(code)){
            return ApiResponse.error(400, "参数错误");
        }
        return ApiResponse.success(bluePrintService.getByCode(code));
    }

    @GetMapping("/code/{code}/{edition}")
    public ApiResponse<BluePrint> getByCodeAndEdition(@PathVariable String code, @PathVariable String edition) {
        if(StringTool.isBlank(code)||StringTool.isBlank(edition)){
            return ApiResponse.error(400, "参数错误");
        }
        BluePrint bluePrint = bluePrintService.getByCodeAndEdition(code, edition);
        if (bluePrint == null) {
            return ApiResponse.error(400, "蓝本不存在");
        }
        return ApiResponse.success(bluePrint);
    }

    // 删除
    @DeleteMapping("/code/{code}/{edition}")
    public ApiResponse<String> deleteByCodeAndEdition(@PathVariable String code, @PathVariable String edition) {
        if (StringTool.isBlank(code) || StringTool.isBlank(edition)) {
            return ApiResponse.error(400, "参数错误");
        }
        int len = bluePrintService.deleteByCodeAndEdition(code, edition);
        if (len > 0) {
            return ApiResponse.success("删除成功");
        } else {
            return ApiResponse.error(400, "删除失败");
        }
    }
}
