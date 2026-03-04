package com.empresaccutsb.apirest.security;

import com.empresaccutsb.apirest.model.AppUser;
import com.empresaccutsb.apirest.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public CustomUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user =
                appUserRepository
                        .findByUsername(username)
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "No existe un usuario con username " + username));

        return User.withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(user.getRoles().stream().map(Enum::name).toArray(String[]::new))
                .disabled(!user.isEnabled())
                .build();
    }
}
