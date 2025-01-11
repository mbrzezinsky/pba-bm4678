package lab04.users.api;

import lab04.security.SignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/signature")
public class SignatureController {

    @Autowired
    private SignatureService signatureService;

    @PostMapping("/generate-hmac")
    public String generateHMACSignature(@RequestBody String data) {
        return signatureService.generateHMACSignature(data);
    }

    @PostMapping("/generate-jwts")
    public ResponseEntity<SignatureResponse> generateJWTS(@RequestBody String data) {
        return ResponseEntity.ok(new SignatureResponse(signatureService.generateJWSSignature(data)));
    }
}
