package lab04.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

@Component
public class HmacSignatureFilter extends OncePerRequestFilter
{

    private static final String KEY = "123456";
    public static final String HEADER = "X-HMAC-SIGNATURE";
    private final Mac mac;

    HmacSignatureFilter()
    {
        try
        {
            mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(), "HmacSHA256");

            mac.init(secretKey);
        }
        catch (NoSuchAlgorithmException | InvalidKeyException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException
    {
        boolean isNotPost = !"POST".equalsIgnoreCase(request.getMethod());
        boolean signatureHeaderIsNotPresent = request.getHeader(HEADER) == null;

        return isNotPost || signatureHeaderIsNotPresent;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {
        RequestWrapper requestToUse = new RequestWrapper(request);

        byte[] buff = new byte[requestToUse.getContentLength()];
        requestToUse.getInputStream().read(buff);

        String payload = new String(buff, StandardCharsets.UTF_8);
        String signature = request.getHeader(HEADER);

        if (!isValidHmac(payload, signature))
        {
            response.getWriter().write("""
                    {
                      "code": "401",
                      "message": "Not valid '%s'."
                    }
                    """.formatted(HEADER));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");

            return;
        }

        filterChain.doFilter(requestToUse, response);
    }

    private boolean isValidHmac(String payload, String signature)
    {
        byte[] decoded = Base64.getDecoder().decode(signature);
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        return Arrays.equals(hash, decoded);
    }
}
