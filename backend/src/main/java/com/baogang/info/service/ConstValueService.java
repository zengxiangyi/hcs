package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.dto.ConstValueQuery;
import com.baogang.info.entity.ConstValue;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.mapper.ConstValueMapper;
import com.baogang.info.repository.ConstValueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConstValueService {

    private final ConstValueRepository constValueRepository;
    private final ConstValueMapper constValueMapper;

    public ConstValueService(ConstValueRepository constValueRepository, ConstValueMapper constValueMapper) {
        this.constValueRepository = constValueRepository;
        this.constValueMapper = constValueMapper;
    }

    public ConstValue getById(Long id) {
        return constValueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("constValue not found: " + id));
    }

    public ConstValue getByCode(String code) {
        return constValueRepository.findByCode(code).orElse(null);
    }

    @Transactional
    public ConstValue save(ConstValue constValue) {
        constValue.setId(null); // 新增时忽略客户端传入的 id
        // (code, category) 业务键防重：同一 category 下 code 重复会让按组合定位出现歧义
        if (constValueRepository.existsByCodeAndCategory(constValue.getCode(), constValue.getCategory())) {
            throw new IllegalArgumentException(
                    "常量值已存在：code=" + constValue.getCode() + ", category=" + constValue.getCategory());
        }
        return constValueRepository.save(constValue);
    }

    @Transactional
    public ConstValue update(Long id, ConstValue constValue) {
        ConstValue existing = getById(id);
        // 显式字段拷贝：新增字段时必须在此同步补充
        existing.setCode(constValue.getCode());
        existing.setName(constValue.getName());
        existing.setCategory(constValue.getCategory());
        existing.setMark(constValue.getMark());
        existing.setRemark(constValue.getRemark());
        return constValueRepository.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!constValueRepository.existsById(id)) {
            throw new ResourceNotFoundException("constValue not found: " + id);
        }
        constValueRepository.deleteById(id);
    }

    public List<ConstValue> findByCategory(String category) {
        return constValueRepository.findByCategory(category);
    }

    // 可变条件查询：接收 ConstValueQuery，按非空字段动态拼接 WHERE（空条件即查全部）
    public PageResult<ConstValue> search(ConstValueQuery query, int pageOffset, int size) {
        long total = constValueMapper.countByQuery(query);
        List<ConstValue> content = constValueMapper.query(query, (long) pageOffset * size, size);
        return PageResult.of(content, total, pageOffset + 1, size);
    }
}
