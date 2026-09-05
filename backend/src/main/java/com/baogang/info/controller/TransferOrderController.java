package com.baogang.info.controller;

import com.baogang.info.common.ApiResponse;
import com.baogang.info.common.PageParam;
import com.baogang.info.common.PageResult;
import com.baogang.info.dto.TransferOrderQuery;
import com.baogang.info.entity.TransferOrder;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.service.TransferOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return ApiResponse.success(transferOrderService.save(transferOrder));
    }

    @PutMapping("/update")
    public ApiResponse<TransferOrder> update(@Valid @RequestBody TransferOrder transferOrder) {
        if (transferOrder.getId() == null) {
            throw new IllegalArgumentException("修改操作必须传入 id");
        }
        return ApiResponse.success(transferOrderService.update(transferOrder));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransferOrder> getById(@PathVariable Long id) {
        TransferOrder transferOrder = transferOrderService.getById(id);
        if (transferOrder == null) {
            throw new ResourceNotFoundException("调拨单不存在：id=" + id);
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
