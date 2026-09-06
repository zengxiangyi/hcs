package com.baogang.info.mapper;

import com.baogang.info.common.Todo;
import com.baogang.info.dto.WorkflowQuery;
import com.baogang.info.entity.Workflow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkflowMapper {

    // 可变条件分页查询（空条件即查全部）
    List<Workflow> queryDone(@Param("q") WorkflowQuery q,
                         @Param("offset") long offset,
                         @Param("limit") int limit);

    long countDone(@Param("q") WorkflowQuery q);

    // 可变条件分页查询（空条件即查全部）
    List<Todo> queryTodo(@Param("q") WorkflowQuery q,
                         @Param("offset") long offset,
                         @Param("limit") int limit);

    long countTodo(@Param("q") WorkflowQuery q);
}
