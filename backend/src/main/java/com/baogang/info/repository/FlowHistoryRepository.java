package com.baogang.info.repository;

import com.baogang.info.entity.FlowHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlowHistoryRepository extends JpaRepository<FlowHistory, Long> {

    List<FlowHistory> findByWorkflowOrderByDealTimeDesc(String workflow);

    List<FlowHistory> findByDealUser(String dealUser);
}
