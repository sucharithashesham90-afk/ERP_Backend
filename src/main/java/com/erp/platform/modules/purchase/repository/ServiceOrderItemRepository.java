package com.erp.platform.modules.purchase.repository;

import com.erp.platform.modules.purchase.entity.ServiceOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceOrderItemRepository extends JpaRepository<ServiceOrderItem, UUID> {

    List<ServiceOrderItem> findByServiceOrderId(UUID serviceOrderId);
}
