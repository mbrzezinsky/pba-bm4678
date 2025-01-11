package lab04.users.api;

import lab04.security.EncryptionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;

@RestController
@RequestMapping("/api/aes")
public class EncryptionServiceController {
    private final EncryptionService encryptionService;

    public EncryptionServiceController(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @PostMapping("/encData")
    public EncryptionResponse encryptPayload(@RequestBody String payload) throws Exception
    {
        return new EncryptionResponse(encryptionService.encryptDataWithAES(payload), encryptionService.encryptRSA(encryptionService.getEncodedKey()));
    }   

    @PostMapping("/decData")
    public String generatePayload(@RequestBody EncryptionResponse payload) throws Exception
    {
        SecretKey aesKey = encryptionService.retriveKey(encryptionService.decryptRSA(payload.key()));

        return encryptionService.decryptDataWithAES(payload.payload(), aesKey);
    }
}
