package com.erp.platform.modules.dispatch.service;

import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.modules.dispatch.dto.CreateDispatchChallanRequest;
import com.erp.platform.modules.dispatch.dto.DispatchChallanDto;
import com.erp.platform.modules.dispatch.entity.DispatchChallan;
import com.erp.platform.modules.dispatch.entity.DispatchChallanLine;
import com.erp.platform.modules.dispatch.repository.DispatchChallanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DispatchChallanService {

    private final DispatchChallanRepository dispatchChallanRepository;
    private final TenantContext tenantContext;

    public PageResponse<DispatchChallanDto> list(Pageable pageable) {
        return PageResponse.of(dispatchChallanRepository.findByTenantIdAndDeletedAtIsNull(tenantContext.current(), pageable).map(this::toDto));
    }

    /**
     * Challans dispatched to one customer, with their lines.
     *
     * <p>Feeds the Dealer-to-Dealer transfer screen: once the "from" dealer is chosen, only stock
     * that was actually dispatched to that dealer may be transferred on, so the crop, variety and
     * lot options are taken from the challan rather than from the whole master.
     */
    public List<DispatchChallanDto> listByCustomer(String customerName) {
        if (customerName == null || customerName.isBlank()) return List.of();
        return dispatchChallanRepository
                .findByTenantIdAndCustomerNameIgnoreCaseAndDeletedAtIsNullOrderByChallanDateDesc(
                        tenantContext.current(), customerName.trim())
                .stream().map(this::toDto).toList();
    }

    public DispatchChallanDto getById(UUID id) {
        UUID tenantId = tenantContext.current();
        DispatchChallan entity = dispatchChallanRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("DispatchChallan not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public DispatchChallanDto create(CreateDispatchChallanRequest request) {
        UUID tenantId = tenantContext.current();
        DispatchChallan entity = new DispatchChallan();
        entity.setTenantId(tenantId);
        apply(entity, request);
        // Challan number is auto-generated when the caller doesn't supply one.
        if (entity.getChallanNumber() == null || entity.getChallanNumber().isBlank())
            entity.setChallanNumber(generateChallanNumber(tenantId));
        if (entity.getStatus() == null || entity.getStatus().isBlank())
            entity.setStatus("DRAFT");
        return toDto(dispatchChallanRepository.save(entity));
    }

    @Transactional
    public DispatchChallanDto update(UUID id, CreateDispatchChallanRequest request) {
        UUID tenantId = tenantContext.current();
        DispatchChallan entity = dispatchChallanRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("DispatchChallan not found: " + id));
        apply(entity, request);
        return toDto(dispatchChallanRepository.save(entity));
    }

    /** Copy every editable field from the request onto the entity. */
    private void apply(DispatchChallan e, CreateDispatchChallanRequest r) {
        if (r.getChallanNumber() != null && !r.getChallanNumber().isBlank())
            e.setChallanNumber(r.getChallanNumber());
        e.setChallanDate(r.getChallanDate());
        e.setCustomerId(r.getCustomerId());
        e.setCustomerName(r.getCustomerName());
        e.setDeliveryAddress(r.getDeliveryAddress());
        e.setProductName(r.getProductName());
        e.setLotNumber(r.getLotNumber());
        e.setQuantityKgs(r.getQuantityKgs());
        e.setTransporterName(r.getTransporterName());
        e.setVehicleNumber(r.getVehicleNumber());
        if (r.getStatus() != null) e.setStatus(r.getStatus());
        e.setRemarks(r.getRemarks());
        e.setValue(r.getValue());
        // delivery order link
        e.setSalesOrderId(r.getSalesOrderId());
        e.setSalesOrderNumber(r.getSalesOrderNumber());
        e.setDeliveryOrderId(r.getDeliveryOrderId());
        e.setDeliveryOrderNumber(r.getDeliveryOrderNumber());
        // dispatch header
        e.setSalesArea(r.getSalesArea());
        e.setDispatchLocationId(r.getDispatchLocationId());
        e.setDispatchLocation(r.getDispatchLocation());
        e.setWayBillNo(r.getWayBillNo());
        e.setRrRlNo(r.getRrRlNo());
        // freight
        e.setFreightCarrierId(r.getFreightCarrierId());
        e.setFreightCarrier(r.getFreightCarrier());
        e.setOtherCarrier(r.getOtherCarrier());
        e.setCarrier(r.getCarrier());
        e.setLorryNo(r.getLorryNo());
        e.setFreightAmount(r.getFreightAmount());
        e.setFreightPaidAdvance(r.getFreightPaidAdvance());
        e.setFreightToPay(r.getFreightToPay());
        // billing address
        e.setBillingAddress(r.getBillingAddress());
        e.setBillingState(r.getBillingState());
        e.setBillingDistrict(r.getBillingDistrict());
        e.setBillingCity(r.getBillingCity());
        e.setBillingZip(r.getBillingZip());
        e.setBillingPhone(r.getBillingPhone());
        // supplier (delivery) address
        e.setSupplierAddress(r.getSupplierAddress());
        e.setSupplierState(r.getSupplierState());
        e.setSupplierDistrict(r.getSupplierDistrict());
        e.setSupplierCity(r.getSupplierCity());
        e.setSupplierZip(r.getSupplierZip());
        e.setSupplierPhone(r.getSupplierPhone());
        // line items — replaced wholesale on each save
        if (e.getItems() == null) e.setItems(new java.util.ArrayList<>());
        e.getItems().clear();
        if (r.getItems() != null) {
            for (CreateDispatchChallanRequest.Item it : r.getItems()) {
                DispatchChallanLine line = new DispatchChallanLine();
                line.setProductId(it.getProductId());
                line.setCropGroup(it.getCropGroup());
                line.setCrop(it.getCrop());
                line.setVariety(it.getVariety());
                line.setCropVariety(it.getCropVariety());
                line.setProductName(it.getProductName());
                line.setPacking(it.getPacking());
                line.setLotNumber(it.getLotNumber());
                line.setQuantity(it.getQuantity());
                line.setRate(it.getRate());
                line.setValue(it.getValue());
                e.getItems().add(line);
            }
        }
    }

    private String generateChallanNumber(UUID tenantId) {
        long n = dispatchChallanRepository.findByTenantIdAndDeletedAtIsNull(tenantId, PageRequest.of(0, 1)).getTotalElements();
        return String.format("DC-%d-%05d", LocalDate.now().getYear(), n + 1);
    }

    @Transactional
    public void delete(UUID id) {
        UUID tenantId = tenantContext.current();
        DispatchChallan entity = dispatchChallanRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new RuntimeException("DispatchChallan not found: " + id));
        entity.setDeletedAt(LocalDateTime.now());
        dispatchChallanRepository.save(entity);
    }

    private DispatchChallanDto toDto(DispatchChallan entity) {
        DispatchChallanDto dto = new DispatchChallanDto();
        dto.setId(entity.getId());
        dto.setChallanNumber(entity.getChallanNumber());
        dto.setChallanDate(entity.getChallanDate());
        dto.setCustomerId(entity.getCustomerId());
        dto.setCustomerName(entity.getCustomerName());
        dto.setDeliveryAddress(entity.getDeliveryAddress());
        dto.setProductName(entity.getProductName());
        dto.setLotNumber(entity.getLotNumber());
        dto.setQuantityKgs(entity.getQuantityKgs());
        dto.setTransporterName(entity.getTransporterName());
        dto.setVehicleNumber(entity.getVehicleNumber());
        dto.setStatus(entity.getStatus());
        dto.setRemarks(entity.getRemarks());
        dto.setValue(entity.getValue());
        dto.setInvoiceNumber(entity.getInvoiceNumber());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setSalesOrderId(entity.getSalesOrderId());
        dto.setSalesOrderNumber(entity.getSalesOrderNumber());
        dto.setDeliveryOrderId(entity.getDeliveryOrderId());
        dto.setDeliveryOrderNumber(entity.getDeliveryOrderNumber());
        dto.setSalesArea(entity.getSalesArea());
        dto.setDispatchLocationId(entity.getDispatchLocationId());
        dto.setDispatchLocation(entity.getDispatchLocation());
        dto.setWayBillNo(entity.getWayBillNo());
        dto.setRrRlNo(entity.getRrRlNo());
        dto.setFreightCarrierId(entity.getFreightCarrierId());
        dto.setFreightCarrier(entity.getFreightCarrier());
        dto.setOtherCarrier(entity.getOtherCarrier());
        dto.setCarrier(entity.getCarrier());
        dto.setLorryNo(entity.getLorryNo());
        dto.setFreightAmount(entity.getFreightAmount());
        dto.setFreightPaidAdvance(entity.getFreightPaidAdvance());
        dto.setFreightToPay(entity.getFreightToPay());
        dto.setBillingAddress(entity.getBillingAddress());
        dto.setBillingState(entity.getBillingState());
        dto.setBillingDistrict(entity.getBillingDistrict());
        dto.setBillingCity(entity.getBillingCity());
        dto.setBillingZip(entity.getBillingZip());
        dto.setBillingPhone(entity.getBillingPhone());
        dto.setSupplierAddress(entity.getSupplierAddress());
        dto.setSupplierState(entity.getSupplierState());
        dto.setSupplierDistrict(entity.getSupplierDistrict());
        dto.setSupplierCity(entity.getSupplierCity());
        dto.setSupplierZip(entity.getSupplierZip());
        dto.setSupplierPhone(entity.getSupplierPhone());
        java.util.List<DispatchChallanDto.Item> items = new java.util.ArrayList<>();
        if (entity.getItems() != null) {
            for (DispatchChallanLine line : entity.getItems()) {
                DispatchChallanDto.Item i = new DispatchChallanDto.Item();
                i.setProductId(line.getProductId());
                i.setCropGroup(line.getCropGroup());
                i.setCrop(line.getCrop());
                i.setVariety(line.getVariety());
                i.setCropVariety(line.getCropVariety());
                i.setProductName(line.getProductName());
                i.setPacking(line.getPacking());
                i.setLotNumber(line.getLotNumber());
                i.setQuantity(line.getQuantity());
                i.setRate(line.getRate());
                i.setValue(line.getValue());
                items.add(i);
            }
        }
        dto.setItems(items);
        return dto;
    }
}

