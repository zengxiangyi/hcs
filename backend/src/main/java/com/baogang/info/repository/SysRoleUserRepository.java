package com.baogang.info.repository;

import com.baogang.info.entity.SysRoleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysRoleUserRepository extends JpaRepository<SysRoleUser, Long> {

    List<SysRoleUser> findByRoleCode(String roleCode);

    List<SysRoleUser> findByUserCode(String userCode);

    void deleteByRoleCode(String roleCode);

    void deleteByUserCode(String userCode);

    SysRoleUser findByRoleCodeAndUserCode(String roleCode, String userCode);

    @Query("select distinct r.roleCode from SysRoleUser r where r.userCode = :userCode")
    List<String> findRolesByUserCode(String userCode);
}
