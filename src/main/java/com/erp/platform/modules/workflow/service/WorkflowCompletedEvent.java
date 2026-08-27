package com.erp.platform.modules.workflow.service;

import com.erp.platform.modules.workflow.entity.WorkflowInstance;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class WorkflowCompletedEvent extends ApplicationEvent {

    private final UUID instanceId;
    private final String module;
    private final UUID referenceId;
    private final String referenceNumber;
    private final String outcome; // "APPROVED" or "REJECTED"
    private final UUID tenantId;

    public WorkflowCompletedEvent(Object source, WorkflowInstance instance) {
        super(source);
        this.instanceId = instance.getId();
        this.module = instance.getModule();
        this.referenceId = instance.getReferenceId();
        this.referenceNumber = instance.getReferenceNumber();
        this.outcome = instance.getStatus().name();
        this.tenantId = instance.getTenantId();
    }
}
