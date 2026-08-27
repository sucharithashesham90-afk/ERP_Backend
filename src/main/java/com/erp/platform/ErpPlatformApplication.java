package com.erp.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

// ERP Platform Service - Modules Audited & Synchronized: Sales, Accounting, Purchase, Lot Traceability, Production, Processing, Admin, Inventory, Quality
@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
public class ErpPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(ErpPlatformApplication.class, args);
    }
}
