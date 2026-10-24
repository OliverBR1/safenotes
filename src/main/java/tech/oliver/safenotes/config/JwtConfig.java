package tech.oliver.safenotes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfig {

    @Value("${jwt.public.key}")
    private RSAPublicKey publicKey;

    @Value("${jwt.private.key}")
    private RSAPrivateKey privateKey;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.expires-in}")
    private Integer expiresIn;

    @Value("${jwt.refresh-expires-in}")
    private Integer refreshExpiresIn;

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getIssuer() {
        return issuer;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public Integer getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

}
