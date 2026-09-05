package com.baogang.info.service;

import com.baogang.info.common.PageResult;
import com.baogang.info.dto.BluePrintQuery;
import com.baogang.info.entity.BluePrint;
import com.baogang.info.exception.ResourceNotFoundException;
import com.baogang.info.mapper.BluePrintMapper;
import com.baogang.info.repository.BluePrintRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BluePrintService {

    private final BluePrintRepository bluePrintRepository;
    private final BluePrintMapper bluePrintMapper;

    public BluePrintService(BluePrintRepository bluePrintRepository, BluePrintMapper bluePrintMapper) {
        this.bluePrintRepository = bluePrintRepository;
        this.bluePrintMapper = bluePrintMapper;
    }

    public PageResult<BluePrint> listPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BluePrint> result = bluePrintRepository.findAll(pageable);
        return PageResult.of(result.getContent(), result.getTotalElements(), page + 1, size);
    }

    @Transactional
    public BluePrint save(BluePrint bluePrint) {
        bluePrint.setId(null);  // 新增时忽略客户端传入的 id（createTime 由 controller 服务端填充）
        // (code, edition) 业务键防重：重复插入会让 findByCodeAndEdition（Optional 单行语义）直接 500
        if (bluePrintRepository.existsByCodeAndEdition(bluePrint.getCode(), bluePrint.getEdition())) {
            throw new IllegalArgumentException(
                    "蓝本已存在：code=" + bluePrint.getCode() + ", edition=" + bluePrint.getEdition());
        }
        return bluePrintRepository.save(bluePrint);
    }

    // 修改：按 id 更新已有记录，保留原始 createTime/createUser，不覆盖创建信息
    @Transactional
    public BluePrint update(BluePrint bluePrint) {
        if (bluePrint.getId() == null) {
            throw new IllegalArgumentException("修改操作必须传入 id");
        }
        BluePrint existing = bluePrintRepository.findById(bluePrint.getId())
                .orElseThrow(() -> new ResourceNotFoundException("蓝本不存在：id=" + bluePrint.getId()));
        // 在托管实体上同步业务字段；createTime/createUser/edition/state 不改动即自动保留
        existing.setCode(bluePrint.getCode());
        existing.setName(bluePrint.getName());
        existing.setGraph(bluePrint.getGraph());
        existing.setFirstLevel(bluePrint.getFirstLevel());
        existing.setSecondLevel(bluePrint.getSecondLevel());
        existing.setMaterialName(bluePrint.getMaterialName());
        existing.setWeight(bluePrint.getWeight());
        existing.setMaterialCode(bluePrint.getMaterialCode());
        existing.setIsFirstCheck(bluePrint.getIsFirstCheck());
        existing.setBusbarNum(bluePrint.getBusbarNum());
        existing.setTestNum(bluePrint.getTestNum());
        existing.setCoolTime(bluePrint.getCoolTime());
        existing.setHardnessDepth(bluePrint.getHardnessDepth());
        existing.setChamfer(bluePrint.getChamfer());
        existing.setFallHead(bluePrint.getFallHead());
        existing.setQuenching(bluePrint.getQuenching());
        existing.setAttention(bluePrint.getAttention());
        existing.setModel(bluePrint.getModel());
        existing.setFirstHardness(bluePrint.getFirstHardness());
        existing.setLastHardness(bluePrint.getLastHardness());
        existing.setSpecs(bluePrint.getSpecs());
        existing.setCustomer(bluePrint.getCustomer());
        existing.setRemark(bluePrint.getRemark());
        // 配合 @DynamicUpdate：仅生成被改动列的 UPDATE
        return bluePrintRepository.save(existing);
    }

    public BluePrint getById(Long id) {
        return bluePrintRepository.findById(id).orElse(null);
    }

    // 可变条件查询：接收 BluePrintQuery，按非空字段动态拼接 WHERE（空条件即查全部）
    public PageResult<BluePrint> search(BluePrintQuery q, int pageOffset, int size) {
        long total = bluePrintMapper.countByQuery(q);
        List<BluePrint> content = bluePrintMapper.query(q, (long) pageOffset * size, size);
        return PageResult.of(content, total, pageOffset + 1, size);
    }

    public List<BluePrint> getByCode(String code) {
        return bluePrintRepository.findByCode(code);
    }

    public BluePrint getByCodeAndEdition(String code, String edition) {
        return bluePrintRepository.findByCodeAndEdition(code, edition).orElse(null);
    }

    @Transactional
    public int deleteByCodeAndEdition(String code, String edition) {
        return bluePrintRepository.deleteByCodeAndEdition(code, edition);
    }
}
