package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.TransferOrderQuery;
import com.baogang.info.entity.TransferOrder;
import com.baogang.info.service.TransferOrderService;
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
@RequestMapping("/transfer")
public class TransferOrderController {

    private final TransferOrderService transferOrderService;

    public TransferOrderController(TransferOrderService transferOrderService) {
        this.transferOrderService = transferOrderService;
    }

    // 复杂/可变条件查询：POST 请求体承载 TransferOrderQuery，支持任意字段组合过滤
    @PostMapping("/search")
    public ApiResponse<PageResult<TransferOrder>> searchByQuery(@RequestBody TransferOrderQuery query) {
        PageParam p = PageParam.of(query.getPage(), query.getPageSize());
        return ApiResponse.success(transferOrderService.search(query, p.offset(), p.size()));
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<TransferOrder>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageParam p = PageParam.of(page, size);
        return ApiResponse.success(transferOrderService.listPaged(p.offset(), p.size()));
    }

    @PostMapping("/save")
    public ApiResponse<TransferOrder> save(@Valid @RequestBody TransferOrder transferOrder) {
        if (transferOrder == null) {
            return ApiResponse.error(400, "请求体不能为空");
        }
        return ApiResponse.success(transferOrderService.save(transferOrder));
    }

    @PutMapping("/update")
    public ApiResponse<TransferOrder> update(@Valid @RequestBody TransferOrder transferOrder) {
        if (transferOrder == null) {
            return ApiResponse.error(400, "请求体不能为空");
        }
        if (transferOrder.getId() == null) {
            return ApiResponse.error(400, "修改操作必须传入 id");
        }
        try {
            return ApiResponse.success(transferOrderService.update(transferOrder));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<TransferOrder> getById(@PathVariable Long id) {
        TransferOrder transferOrder = transferOrderService.getById(id);
        if (transferOrder == null) {
            return ApiResponse.error(400, "调拨单不存在");
        }
        return ApiResponse.success(transferOrder);
    }

    @GetMapping("/code/{code}")
    public ApiResponse<List<TransferOrder>> getByCode(@PathVariable String code) {
        return ApiResponse.success(transferOrderService.getByCode(code));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteById(@PathVariable Long id) {
        transferOrderService.deleteById(id);
        return ApiResponse.success("删除成功");
    }
}
