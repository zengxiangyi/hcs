package com.baogang.info.repository;

import com.baogang.info.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findByCode(String code);

    boolean existsByCode(String code);

    int deleteByCode(String code);

}
