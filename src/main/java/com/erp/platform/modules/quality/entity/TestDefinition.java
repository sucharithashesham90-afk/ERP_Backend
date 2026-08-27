package com.erp.platform.modules.quality.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_definitions",
       indexes = {
           @Index(name = "idx_test_def_tenant", columnList = "tenant_id"),
           @Index(name = "idx_test_def_code", columnList = "tenant_id, code")
       })
@Getter
@Setter
public class TestDefinition extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "test_type", length = 20)
    @Enumerated(EnumType.STRING)
    private TestType testType;

    @Column(name = "applicable_to", length = 100)
    private String applicableTo;

    @Column(length = 1000)
    private String description;

    @Column(name = "standard_reference", length = 100)
    private String standardReference;

    @Column(name = "short_name", length = 50)
    private String shortName;

    @Column(name = "no_of_replications")
    private int noOfReplications = 0;

    @Column(name = "test_location_ids", length = 1000)
    private String testLocationIds;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "definition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestParameter> parameters = new ArrayList<>();

    public enum TestType {
        INCOMING, IN_PROCESS, OUTGOING, PERIODIC
    }
}
