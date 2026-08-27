package com.erp.platform.modules.auth;

import com.erp.platform.common.audit.AuditAwareImpl;
import com.erp.platform.modules.auth.entity.Role;
import com.erp.platform.modules.auth.entity.User;
import com.erp.platform.modules.auth.repository.RoleRepository;
import com.erp.platform.modules.auth.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A user's roles must survive being saved twice.
 *
 * <p>Creating an employee saves the new login, then saves it again to attach groups and reporting
 * manager. The second save is a merge, and merging copies collections by clearing the target first.
 * Hibernate wraps whichever collection instance it was handed, so a login whose roles arrived as
 * {@code Set.of(role)} had an immutable set as the backing store of its persistent collection, and
 * that clear threw UnsupportedOperationException — surfacing as a bare "Internal server error" on
 * the employee screen, after the employee row and its ledger had already been written.
 */
@DataJpaTest
@Import(UserRolesMergeTest.AuditorConfig.class)
class UserRolesMergeTest {

    /** The JPA slice leaves the auditor out, and auditing looks it up by name. */
    @TestConfiguration
    static class AuditorConfig {
        @Bean(name = "auditAwareImpl")
        AuditAwareImpl auditAwareImpl() {
            return new AuditAwareImpl();
        }
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("A login saved with one role can be saved again, as creating an employee does")
    void savingTwiceDoesNotFailOnRoles() {
        Role role = new Role();
        role.setName("EMPLOYEE_TEST");
        role = roleRepository.save(role);

        User user = new User();
        user.setTenantId(UUID.randomUUID());
        user.setEmail("merge.test@example.com");
        user.setFullName("Merge Test");
        user.setPassword("irrelevant-hash");
        // Exactly what provisionLogin does: a mutable copy, not Set.of(role).
        user.setRoles(new HashSet<>(Set.of(role)));
        User saved = userRepository.save(user);

        // The second save is the one that used to fail: EmployeeService writes the login, then
        // saves it again to attach groups and the reporting manager.
        saved.setReportingManagerName("Someone");
        assertThatCode(() -> userRepository.saveAndFlush(saved)).doesNotThrowAnyException();

        assertThat(userRepository.findById(saved.getId()))
                .get()
                .satisfies(u -> assertThat(u.getRoles()).hasSize(1));
    }

    @Test
    @DisplayName("An immutable role set is what broke it — the collection must stay mutable")
    void rolesCollectionIsMutable() {
        Role role = new Role();
        role.setName("EMPLOYEE_TEST_2");
        role = roleRepository.save(role);

        User user = new User();
        user.setTenantId(UUID.randomUUID());
        user.setEmail("mutable.test@example.com");
        user.setFullName("Mutable Test");
        user.setPassword("irrelevant-hash");
        user.setRoles(new HashSet<>(Set.of(role)));
        User saved = userRepository.saveAndFlush(user);

        // clear() is precisely what merge does to the target collection.
        assertThatCode(() -> saved.getRoles().clear()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("An immutable role set is what broke it: Hibernate wraps the instance it is given")
    void immutableRoleSetBreaksTheSecondSave() {
        Role role = new Role();
        role.setName("EMPLOYEE_TEST_3");
        role = roleRepository.save(role);

        User user = new User();
        user.setTenantId(UUID.randomUUID());
        user.setEmail("immutable.test@example.com");
        user.setFullName("Immutable Test");
        user.setPassword("irrelevant-hash");
        user.setRoles(Set.of(role));   // the old code, kept here to show what it costs
        User saved = userRepository.saveAndFlush(user);

        // clear() is what merge performs on the target collection before copying into it.
        assertThatThrownBy(() -> saved.getRoles().clear())
                .as("this is the failure the employee screen reported as Internal server error")
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
