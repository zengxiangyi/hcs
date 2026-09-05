package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.dto.TransferOrderQuery;
import com.baogang.info.entity.TransferOrder;
import com.baogang.info.mapper.TransferOrderMapper;
import com.baogang.info.repository.TransferOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransferOrderService {

    private final TransferOrderRepository transferOrderRepository;
    private final TransferOrderMapper transferOrderMapper;

    public TransferOrderService(TransferOrderRepository transferOrderRepository,
                                TransferOrderMapper transferOrderMapper) {
        this.transferOrderRepository = transferOrderRepository;
        this.transferOrderMapper = transferOrderMapper;
    }

    public PageResult<TransferOrder> listPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransferOrder> result = transferOrderRepository.findAll(pageable);
        return PageResult.of(result.getContent(), result.getTotalElements(), page + 1, size);
    }

    @Transactional
    public TransferOrder save(TransferOrder transferOrder) {
        transferOrder.setId(null);  // 新增时忽略客户端传入的 id
        return transferOrderRepository.save(transferOrder);
    }

    @Transactional
    public TransferOrder update(TransferOrder transferOrder) {
        if (transferOrder.getId() == null) {
            throw new IllegalArgumentException("修改操作必须传入 id");
        }
        TransferOrder existing = transferOrderRepository.findById(transferOrder.getId())
                .orElseThrow(() -> new IllegalArgumentException("调拨单不存在：id=" + transferOrder.getId()));
        existing.setCode(transferOrder.getCode());
        existing.setName(transferOrder.getName());
        existing.setCategory(transferOrder.getCategory());
        existing.setTransferDate(transferOrder.getTransferDate());
        existing.setMaterialCode(transferOrder.getMaterialCode());
        existing.setNum(transferOrder.getNum());
        existing.setWeight(transferOrder.getWeight());
        existing.setMaterial(transferOrder.getMaterial());
        existing.setRollNum(transferOrder.getRollNum());
        existing.setOutProcess(transferOrder.getOutProcess());
        existing.setInProcess(transferOrder.getInProcess());
        existing.setOutRoom(transferOrder.getOutRoom());
        existing.setInRoom(transferOrder.getInRoom());
        existing.setRemark(transferOrder.getRemark());
        existing.setPrompt(transferOrder.getPrompt());
        existing.setQuenching(transferOrder.getQuenching());
        existing.setSupplier(transferOrder.getSupplier());
        existing.setCreateUser(transferOrder.getCreateUser());
        existing.setCreateTime(transferOrder.getCreateTime());
        existing.setReceiveUser(transferOrder.getReceiveUser());
        existing.setReceiveTime(transferOrder.getReceiveTime());
        existing.setState(transferOrder.getState());
        return transferOrderRepository.save(existing);
    }

    public TransferOrder getById(Long id) {
        return transferOrderRepository.findById(id).orElse(null);
    }

    public List<TransferOrder> getByCode(String code) {
        return transferOrderRepository.findByCode(code);
    }

    @Transactional
    public void deleteById(Long id) {
        transferOrderRepository.deleteById(id);
    }

    // 可变条件查询：接收 TransferOrderQuery，按非空字段动态拼接 WHERE（空条件即查全部）
    public PageResult<TransferOrder> search(TransferOrderQuery q, int pageOffset, int size) {
        long total = transferOrderMapper.countByQuery(q);
        List<TransferOrder> content = transferOrderMapper.query(q, (long) pageOffset * size, size);
        return PageResult.of(content, total, pageOffset + 1, size);
    }
}
