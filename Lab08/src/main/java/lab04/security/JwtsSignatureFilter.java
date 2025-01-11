package lab04.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtsSignatureFilter extends OncePerRequestFilter
{

    private final SignatureService signatureService;

    public static final String HEADER = "X-JWS-SIGNATURE";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException
    {
        boolean isNotPost = !"PUT".equalsIgnoreCase(request.getMethod());
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

        if (!signatureService.verifyJWSSignature(payload, signature))
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
}
