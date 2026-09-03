package com.baogang.info.repository;

import com.baogang.info.entity.TaskProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskProcessRepository extends JpaRepository<TaskProcess, Long> {

    List<TaskProcess> findByState(String state);

    Page<TaskProcess> findByState(String state, Pageable pageable);

    List<TaskProcess> findByStep(String step);
}
