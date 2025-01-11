package lab04.security;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class SignatureService {

    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    private static final String SECRET_KEY = "123456";

    @Getter
    private final PublicKey publicKey;
    @Getter
    private final PrivateKey privateKey;
    private final ObjectMapper objectMapper;

    public SignatureService(
            @Value("classpath:private_key.pem") Resource privateKey,
            @Value("classpath:public_key.pem") Resource publicKey,
            ObjectMapper objectMapper
    ) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        this.objectMapper = objectMapper;

        byte[] publicKeyBytes = publicKey.getContentAsByteArray();
        String publicKeyContent = new String(publicKeyBytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
        KeyFactory publicKeyFactory = KeyFactory.getInstance("RSA");
        this.publicKey = publicKeyFactory.generatePublic(publicKeySpec);

        // Load Private Key
        byte[] privateKeyBytes = privateKey.getContentAsByteArray();
        String privateKeyContent = new String(privateKeyBytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent));
        KeyFactory privateKeyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = privateKeyFactory.generatePrivate(privateKeySpec);

    }

    public String generateHMACSignature(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), HMAC_SHA256_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC signature", e);
        }
    }

    public String generateJWSSignature(String data) {
        return Jwts.builder()
                .content(data)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public boolean verifyJWSSignature(String data, String jws) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(jws);

            String signedPayload = objectMapper.writeValueAsString(claims.getPayload());
            String payload = objectMapper.readTree(data).toString();

            return payload.equals(signedPayload);
        } catch (Exception e) {
            return false;
        }
    }
}

