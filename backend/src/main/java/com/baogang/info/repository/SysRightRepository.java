package com.baogang.info.repository;

import com.baogang.info.entity.SysRight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysRightRepository extends JpaRepository<SysRight, Long> {

    Optional<SysRight> findByCode(String code);

    boolean existsByCode(String code);

    List<SysRight> findByCategory(String category);

    int deleteByCode(String code);
}
