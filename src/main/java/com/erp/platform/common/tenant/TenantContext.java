package com.erp.platform.common.tenant;

import com.erp.platform.common.exception.AppException;
import com.erp.platform.common.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.UUID;

@Component
@RequestScope
public class TenantContext {

    private UUID currentTenantId;

    public UUID current() {
        if (currentTenantId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "No tenant context available");
        }
        return currentTenantId;
    }

    public void set(UUID tenantId) {
        this.currentTenantId = tenantId;
    }

    public boolean isSet() {
        return currentTenantId != null;
    }
}
