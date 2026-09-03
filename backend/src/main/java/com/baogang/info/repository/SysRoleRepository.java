package com.baogang.info.repository;

import com.baogang.info.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    Optional<SysRole> findByCode(String code);

    boolean existsByCode(String code);

    List<SysRole> findByCategory(String category);

    int deleteByCode(String code);
}
