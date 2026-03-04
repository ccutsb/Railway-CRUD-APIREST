package com.empresaccutsb.apirest.repository;

import com.empresaccutsb.apirest.model.AppUser;
import com.empresaccutsb.apirest.model.RefreshToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    List<RefreshToken> findAllByUserAndRevokedFalse(AppUser user);
}
