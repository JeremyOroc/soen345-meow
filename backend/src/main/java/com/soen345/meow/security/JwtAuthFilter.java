package com.soen345.meow.security;

import com.soen345.meow.entity.User;
import com.soen345.meow.repository.UserRepository;
import com.soen345.meow.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authorizationHeader.substring(7).trim();
            if (!StringUtils.hasText(token) || !jwtUtil.isTokenValid(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String subject = jwtUtil.getEmailFromToken(token);
                Optional<User> userOpt = userRepository.findByEmail(subject);
                if (userOpt.isEmpty()) {
                    userOpt = userRepository.findByPhone(subject);
                }
                userOpt.ifPresent(this::setAuthentication);
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(User user) {
        List<SimpleGrantedAuthority> authorities = getAuthorities(user);
        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getEmail(), authorities);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            principal,
                null,
                authorities
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private List<SimpleGrantedAuthority> getAuthorities(User user) {
        if (!StringUtils.hasText(user.getRole())) {
            return List.of();
        }

        String role = user.getRole().startsWith("ROLE_") ? user.getRole() : "ROLE_" + user.getRole();
        return List.of(new SimpleGrantedAuthority(role));
    }
}