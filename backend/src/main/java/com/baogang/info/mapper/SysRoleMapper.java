package com.baogang.info.mapper;

import com.baogang.info.dto.SysRoleQuery;
import com.baogang.info.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysRoleMapper {

    // 可变条件分页查询（空条件即查全部）
    List<SysRole> query(@Param("q") SysRoleQuery q,
                        @Param("offset") long offset,
                        @Param("limit") int limit);

    long countByQuery(@Param("q") SysRoleQuery q);
}
