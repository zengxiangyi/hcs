package com.baogang.info.repository;

import com.baogang.info.entity.ConstValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConstValueRepository extends JpaRepository<ConstValue, Long> {

    Optional<ConstValue> findByCode(String code);

    boolean existsByCode(String code);

    List<ConstValue> findByCategory(String category);
}
