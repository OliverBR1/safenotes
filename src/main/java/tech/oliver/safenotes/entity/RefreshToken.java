package tech.oliver.safenotes.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "jti", nullable = false)
    private UUID jti;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "opaque_hash", nullable = false)
    private String opaqueHash;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public RefreshToken() {
    }

    public UUID getJti() {
        return jti;
    }

    public void setJti(UUID jti) {
        this.jti = jti;
    }

    public UUID getFamilyId() {
        return familyId;
    }

    public void setFamilyId(UUID familyId) {
        this.familyId = familyId;
    }

    public String getOpaqueHash() {
        return opaqueHash;
    }

    public void setOpaqueHash(String opaqueHash) {
        this.opaqueHash = opaqueHash;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}
