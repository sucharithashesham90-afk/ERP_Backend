package com.erp.platform.modules.auth.service;

import com.erp.platform.modules.auth.entity.Permission;
import com.erp.platform.modules.auth.entity.Role;
import com.erp.platform.modules.auth.entity.User;
import com.erp.platform.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // "email" here is the login identifier — accept either the e-mail or the username.
        User user = userRepository.findByEmail(email)
                .or(() -> userRepository.findByUsername(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            for (Permission perm : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(perm.getName()));
            }
        }

        boolean blocked = user.getStatus() == User.UserStatus.PENDING_APPROVAL
                || user.getStatus() == User.UserStatus.REJECTED
                || user.getStatus() == User.UserStatus.INACTIVE;

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(blocked)
                .accountLocked(user.getStatus() == User.UserStatus.LOCKED)
                .build();
    }
}
