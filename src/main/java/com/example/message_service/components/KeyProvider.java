package com.example.message_service.components;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@Getter
@Slf4j
public class KeyProvider {

    @Value("${jwt.private-key}")
    private String privateKeyPem;

    @Value("${jwt.public-key}")
    private String publicKeyPem;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            this.privateKey = loadPrivateKey(privateKeyPem);
            this.publicKey = loadPublicKey(publicKeyPem);
            log.info("RSA keys loaded successfully from environment variables.");
        } catch (Exception e) {
            log.error("Failed to load RSA keys from environment variables", e);
            throw new IllegalStateException("Unable to initialize RSA keys", e);
        }
    }

    private PrivateKey loadPrivateKey(String pem) throws Exception {
        pem = normalizePem(pem);
        byte[] keyBytes = decodePem(pem, "PRIVATE KEY");
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private PublicKey loadPublicKey(String pem) throws Exception {
        pem = normalizePem(pem);
        byte[] keyBytes = decodePem(pem, "PUBLIC KEY");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    private String normalizePem(String pem) {
        // Nếu là chuỗi "-----BEGIN...\\n..." thì chuyển \\n thành newline thực
        if (pem.contains("\\n")) {
            pem = pem.replace("\\n", "\n");
        }
        return pem;
    }

    private byte[] decodePem(String pemContent, String keyType) {
        return Base64.getDecoder().decode(
                pemContent
                        .replace("-----BEGIN " + keyType + "-----", "")
                        .replace("-----END " + keyType + "-----", "")
                        .replaceAll("\\s+", "")
        );
    }
}
