package lab04.security;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfiguration
{
    @Bean
    UserDetailsService userDetailsService(PasswordEncoder passwordEncoder)
    {
        UserDetails user = User
                .withUsername("bm46478")
                .password(passwordEncoder.encode("123456"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder) throws Exception
    {
        return http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth ->
                        auth
                                .requestMatchers("/api/users").authenticated()
                                .requestMatchers("/api/signature/generate-hmac").authenticated()
                                .requestMatchers("/api/signature/generate-jwts").authenticated()
                                .requestMatchers("/api/aes/encData").authenticated()
                                .requestMatchers("/api/aes/decData").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/users/{id}").authenticated()
                )
                .httpBasic(withDefaults())

                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/users/{id}").hasAuthority("SCOPE_pba_resource"))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)))
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("classpath:key.pem") Resource key) throws CertificateException, IOException
    {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(key.getInputStream());
        RSAPublicKey publicKey = (RSAPublicKey) cert.getPublicKey();

        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }
}
