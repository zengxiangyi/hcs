package com.baogang.info.mapper;

import com.baogang.info.dto.TransferOrderQuery;
import com.baogang.info.entity.TransferOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TransferOrderMapper {

    // 可变条件分页查询（空条件即查全部）
    List<TransferOrder> query(@Param("q") TransferOrderQuery q,
                              @Param("offset") long offset,
                              @Param("limit") int limit);

    long countByQuery(@Param("q") TransferOrderQuery q);
}
