package com.baogang.info.mapper;

import com.baogang.info.dto.TaskProcessQuery;
import com.baogang.info.entity.TaskProcess;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskProcessMapper {

    // 可变条件分页查询（空条件即查全部）
    List<TaskProcess> query(@Param("q") TaskProcessQuery q,
                            @Param("offset") long offset,
                            @Param("limit") int limit);

    long countByQuery(@Param("q") TaskProcessQuery q);
}
