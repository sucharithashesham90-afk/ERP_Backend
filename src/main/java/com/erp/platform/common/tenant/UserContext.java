package com.erp.platform.common.tenant;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequestScope
public class UserContext {

    private UUID userId;
    private List<String> allowedLocations = Collections.emptyList();

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public List<String> getAllowedLocations() { return allowedLocations; }
    public void setAllowedLocations(List<String> allowedLocations) {
        this.allowedLocations = allowedLocations != null ? allowedLocations : Collections.emptyList();
    }

    public boolean hasLocationRestriction() {
        return allowedLocations != null && !allowedLocations.isEmpty();
    }
}
