package com.baogang.info.mapper;

import com.baogang.info.dto.SysRightQuery;
import com.baogang.info.entity.SysRight;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRightMapper {

    // 可变条件分页查询（空条件即查全部）
    List<SysRight> query(@Param("q") SysRightQuery q,
                         @Param("offset") long offset,
                         @Param("limit") int limit);

    long countByQuery(@Param("q") SysRightQuery q);
}
