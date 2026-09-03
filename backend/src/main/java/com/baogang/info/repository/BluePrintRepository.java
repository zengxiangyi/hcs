package com.baogang.info.repository;

import com.baogang.info.entity.BluePrint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BluePrintRepository extends JpaRepository<BluePrint, Long> {

    List<BluePrint> findByState(String state);

    Page<BluePrint> findByState(String state, Pageable pageable);

    List<BluePrint> findByCode(String code);

    Optional<BluePrint> findByCodeAndEdition(String code, String edition);

    int deleteByCodeAndEdition(String code, String edition);
}
