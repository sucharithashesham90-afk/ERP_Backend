package com.erp.platform.security;

import com.erp.platform.common.dto.ApiResponse;
import com.erp.platform.common.exception.ErrorCode;
import com.erp.platform.common.tenant.TenantContext;
import com.erp.platform.common.tenant.UserContext;
import com.erp.platform.modules.auth.entity.User;
import com.erp.platform.modules.auth.repository.UserRepository;
import com.erp.platform.modules.auth.service.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    /** Reachable even while a login is gated on changing its temp password. */
    private static final List<String> ALLOWED_WHILE_MUST_CHANGE_PASSWORD = List.of(
            "/api/v1/auth/me/change-password", "/api/v1/auth/me", "/api/v1/auth/logout");

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final TenantContext tenantContext;
    private final UserContext userContext;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = extractToken(request);
        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            String email = tokenProvider.getEmailFromToken(token);
            UUID tenantId = tokenProvider.getTenantIdFromToken(token);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

            tenantContext.set(tenantId);
            userContext.setUserId(tokenProvider.getUserIdFromToken(token));
            userContext.setAllowedLocations(tokenProvider.getLocationsFromToken(token));

            // Enforced live off the User row (not a JWT claim), so the moment someone changes their
            // temp password the very next request already gets through on the same token — no
            // reissue needed. UserDetailsServiceImpl returns a plain Spring Security UserDetails
            // that doesn't carry these custom fields, so this is a second, deliberate lookup.
            User user = userRepository.findById(userContext.getUserId()).orElse(null);
            if (user != null && user.isChangePasswordOnLogin()) {
                if (user.getPasswordExpiresAt() != null && LocalDateTime.now().isAfter(user.getPasswordExpiresAt())) {
                    writeError(response, ErrorCode.PASSWORD_EXPIRED,
                            "Your temporary password has expired. Contact HR or your manager to reset it.");
                    return;
                }
                String path = request.getRequestURI();
                boolean allowed = ALLOWED_WHILE_MUST_CHANGE_PASSWORD.stream().anyMatch(path::startsWith);
                if (!allowed) {
                    writeError(response, ErrorCode.MUST_CHANGE_PASSWORD,
                            "You must change your password before continuing.");
                    return;
                }
            }
        }
        chain.doFilter(request, response);
    }

    /** Short-circuits the chain with the same {@link ApiResponse#error} shape GlobalExceptionHandler
     *  would produce — this runs before Spring MVC, so it has to write that JSON itself. */
    private void writeError(HttpServletResponse response, ErrorCode code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(code.getCode(), message)));
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
