package com.erp.platform.modules.organization.service;

import com.erp.platform.common.dto.PageResponse;
import com.erp.platform.common.exception.AppException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.erp.platform.modules.hr.entity.Employee;
import com.erp.platform.modules.hr.repository.EmployeeRepository;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.modules.organization.dto.BranchDto;
import com.erp.platform.modules.organization.dto.CreateBranchRequest;
import com.erp.platform.modules.organization.entity.Branch;
import com.erp.platform.modules.organization.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BranchService {

    private final BranchRepository branchRepository;
    private final EmployeeRepository employeeRepository;
    private final TenantContext tenantContext;

    public PageResponse<BranchDto> list(UUID companyId, Pageable pageable) {
        UUID tenantId = tenantContext.current();
        var page = companyId != null
                ? branchRepository.findByTenantIdAndCompanyIdAndDeletedAtIsNull(tenantId, companyId, pageable)
                : branchRepository.findByTenantIdAndDeletedAtIsNull(tenantId, pageable);
        return PageResponse.of(page.map(this::toDto));
    }

    public BranchDto getById(UUID id) {
        return toDto(findOrThrow(id));
    }

    @Transactional
    public BranchDto create(CreateBranchRequest request) {
        UUID tenantId = tenantContext.current();
        Branch branch = new Branch();
        branch.setTenantId(tenantId);
        branch.setCompanyId(request.getCompanyId());
        rejectDuplicate(tenantId, request, null);
        applyRequest(branch, request);
        branch = branchRepository.save(branch);
        log.info("Branch created: id={}, name={}", branch.getId(), branch.getName());
        return toDto(branch);
    }

    @Transactional
    public BranchDto update(UUID id, CreateBranchRequest request) {
        rejectDuplicate(tenantContext.current(), request, id);
        Branch branch = findOrThrow(id);
        applyRequest(branch, request);
        return toDto(branchRepository.save(branch));
    }

    @Transactional
    public void delete(UUID id) {
        Branch branch = findOrThrow(id);
        branch.setDeletedAt(LocalDateTime.now());
        branchRepository.save(branch);
        log.info("Branch soft-deleted: id={}", id);
    }

    private void applyRequest(Branch branch, CreateBranchRequest r) {
        branch.setName(r.getName());
        branch.setCode(r.getCode());
        branch.setAddress(r.getAddress());
        branch.setCity(r.getCity());
        branch.setState(r.getState());
        branch.setPhone(r.getPhone());
        branch.setEmail(r.getEmail());
        branch.setHeadOffice(r.isHeadOffice());
    }

    private BranchDto toDto(Branch b) {
        BranchDto dto = new BranchDto();
        dto.setId(b.getId());
        dto.setTenantId(b.getTenantId());
        dto.setCompanyId(b.getCompanyId());
        dto.setName(b.getName());
        dto.setCode(b.getCode());
        dto.setAddress(b.getAddress());
        dto.setCity(b.getCity());
        dto.setState(b.getState());
        dto.setPhone(b.getPhone());
        dto.setEmail(b.getEmail());
        dto.setHeadOffice(b.isHeadOffice());
        dto.setActive(b.isActive());
        dto.setCreatedAt(b.getCreatedAt());
        return dto;
    }

    private Branch findOrThrow(UUID id) {
        return branchRepository.findByTenantIdAndIdAndDeletedAtIsNull(tenantContext.current(), id)
                .orElseThrow(() -> AppException.notFound("Branch not found: " + id));
    }

    /**
     * Refuse a branch that already exists rather than quietly making a second one.
     *
     * <p>There was no check here and none in the database, so the same branch could be created over
     * and over — by a repeated request, a double-submitted form, or a demo seeder re-running. Names
     * and codes are compared without case, because "HO" and "ho" are the same branch to everyone
     * except a string comparison.
     */
    private void rejectDuplicate(UUID tenantId, CreateBranchRequest request, UUID selfId) {
        String code = request.getCode() == null ? "" : request.getCode().trim();
        String name = request.getName() == null ? "" : request.getName().trim();
        boolean codeTaken = !code.isEmpty() && (selfId == null
                ? branchRepository.existsByTenantIdAndCodeIgnoreCaseAndDeletedAtIsNull(tenantId, code)
                : branchRepository.existsByTenantIdAndCodeIgnoreCaseAndIdNotAndDeletedAtIsNull(tenantId, code, selfId));
        if (codeTaken) throw AppException.conflict("There is already a branch with the code " + code + ".");
        boolean nameTaken = !name.isEmpty() && (selfId == null
                ? branchRepository.existsByTenantIdAndNameIgnoreCaseAndDeletedAtIsNull(tenantId, name)
                : branchRepository.existsByTenantIdAndNameIgnoreCaseAndIdNotAndDeletedAtIsNull(tenantId, name, selfId));
        if (nameTaken) throw AppException.conflict("There is already a branch called " + name + ".");
    }

    /**
     * Find branches that are the same branch entered more than once, and optionally fold them together.
     *
     * <p>Duplicates are grouped by code where there is one and by name otherwise, both without case.
     * The oldest of each group is kept, on the grounds that it is the one everything else was
     * attached to first.
     *
     * <p>Nothing is removed unless apply is set. Called without it this reports what it would do, so
     * the list can be read before any record is touched — these are records other things point at,
     * and a cleanup that surprises somebody is worse than the duplicates.
     *
     * <p>Employees sitting on a duplicate are moved onto the branch that is kept rather than being
     * left pointing at a deleted one. That is safe precisely because these are the same branch twice.
     */
    @Transactional
    public Map<String, Object> deduplicate(boolean apply) {
        UUID tenantId = tenantContext.current();
        List<Branch> all = branchRepository.findByTenantIdAndDeletedAtIsNull(tenantId, Pageable.unpaged()).getContent();

        Map<String, List<Branch>> groups = new LinkedHashMap<>();
        for (Branch b : all) {
            String key = (b.getCode() != null && !b.getCode().isBlank() ? "c:" + b.getCode() : "n:" + b.getName());
            groups.computeIfAbsent(key.toLowerCase(), k -> new ArrayList<>()).add(b);
        }

        List<Map<String, Object>> plan = new ArrayList<>();
        int removed = 0, moved = 0;
        for (var e : groups.entrySet()) {
            List<Branch> dupes = e.getValue();
            if (dupes.size() < 2) continue;
            dupes.sort(Comparator.comparing(Branch::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())));
            Branch keep = dupes.get(0);
            List<Branch> drop = dupes.subList(1, dupes.size());

            List<Map<String, Object>> dropped = new ArrayList<>();
            for (Branch d : drop) {
                List<Employee> staff = employeeRepository.findByTenantIdAndBranchIdAndDeletedAtIsNull(tenantId, d.getId());
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", d.getId());
                row.put("name", d.getName());
                row.put("code", d.getCode());
                row.put("employeesToMove", staff.size());
                dropped.add(row);
                if (apply) {
                    for (Employee emp : staff) { emp.setBranchId(keep.getId()); employeeRepository.save(emp); moved++; }
                    d.setDeletedAt(LocalDateTime.now());
                    branchRepository.save(d);
                    removed++;
                } else {
                    moved += staff.size();
                    removed++;
                }
            }
            Map<String, Object> g = new LinkedHashMap<>();
            g.put("keeping", Map.of("id", keep.getId(), "name", keep.getName() == null ? "" : keep.getName(),
                                    "code", keep.getCode() == null ? "" : keep.getCode()));
            g.put("removing", dropped);
            plan.add(g);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("applied", apply);
        out.put("duplicateGroups", plan.size());
        out.put("branchesRemoved", removed);
        out.put("employeesMoved", moved);
        out.put("groups", plan);
        if (!apply) out.put("note", "Nothing was changed. Repeat with apply=true to carry this out.");
        log.info("Branch de-duplication {}: {} groups, {} branches, {} employees",
                apply ? "applied" : "previewed", plan.size(), removed, moved);
        return out;
    }
}
