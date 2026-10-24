package tech.oliver.safenotes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tech.oliver.safenotes.entity.RefreshToken;
import tech.oliver.safenotes.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByOpaqueHash(String opaqueHash);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query("update RefreshToken r set r.revokedAt = :revokedAt where r.familyId = :familyId")
    void updatedRevokedAtByFamilyId(Instant revokedAt, UUID familyId);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query("update RefreshToken r set r.revokedAt = :revokedAt where r.opaqueHash = :opaqueHash and r.revokedAt is null")
    int revokeIfNotRevoked(Instant revokedAt, String opaqueHash);

    List<RefreshToken> user(User user);

    boolean existsByOpaqueHashAndUserId(String opaqueHash, Long userId);

    @Modifying
    @Query("update RefreshToken r set r.revokedAt = :revokedAt where r.user.id = :userId and r.revokedAt is null")
    void revokeAllFromUserId(Instant revokedAt, Long userId);
}

