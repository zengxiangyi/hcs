package com.baogang.info.repository;

import com.baogang.info.entity.SysRoleRight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysRoleRightRepository extends JpaRepository<SysRoleRight, Long> {

    List<SysRoleRight> findByRoleCode(String roleCode);

    List<SysRoleRight> findByRightCode(String rightCode);

    void deleteByRoleCode(String roleCode);

    void deleteByRightCode(String rightCode);

    SysRoleRight findByRoleCodeAndRightCode(String roleCode, String rightCode);
}
