package com.baogang.info.mapper;

import com.baogang.info.dto.ConstValueQuery;
import com.baogang.info.entity.ConstValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConstValueMapper {

    // 可变条件分页查询（空条件即查全部）
    List<ConstValue> query(@Param("q") ConstValueQuery q,
                           @Param("offset") long offset,
                           @Param("limit") int limit);

    long countByQuery(@Param("q") ConstValueQuery q);
}
