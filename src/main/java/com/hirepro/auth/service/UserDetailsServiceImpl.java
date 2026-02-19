package com.hirepro.auth.service;

import com.hirepro.users.entity.AuthUser;
import com.hirepro.users.repository.AuthUserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AuthUserRepository authUserRepository;

    public UserDetailsServiceImpl(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUser user = authUserRepository.findByEmailAndNotDeleted(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                isAccountActive(user),
                true,
                true,
                isAccountNotLocked(user),
                getAuthorities(user)
        );
    }

    private boolean isAccountActive(AuthUser user) {
        return "ACTIVE".equals(user.getStatus());
    }

    private boolean isAccountNotLocked(AuthUser user) {
        return !"INACTIVE".equals(user.getStatus());
    }

    private Collection<? extends GrantedAuthority> getAuthorities(AuthUser user) {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole())
        );
    }
}