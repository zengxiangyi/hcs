package com.baogang.info.repository;

import com.baogang.info.entity.TransferOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferOrderRepository extends JpaRepository<TransferOrder, Long> {

    List<TransferOrder> findByState(String state);

    Page<TransferOrder> findByState(String state, Pageable pageable);

    List<TransferOrder> findByCode(String code);
}
