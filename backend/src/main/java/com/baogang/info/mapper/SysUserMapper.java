package com.baogang.info.mapper;

import com.baogang.info.dto.SysUserQuery;
import com.baogang.info.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysUserMapper {

    // 可变条件分页查询（空条件即查全部）
    List<SysUser> query(@Param("q") SysUserQuery q,
                        @Param("offset") long offset,
                        @Param("limit") int limit);

    long countByQuery(@Param("q") SysUserQuery q);
}
