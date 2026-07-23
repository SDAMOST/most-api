package pl.salezjanie.most.identity.application;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.salezjanie.most.identity.domain.CommunityMember;
import pl.salezjanie.most.identity.domain.CommunityMemberRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Service responsible for issuing authentication tokens.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final CommunityMemberRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;

    public AuthService(CommunityMemberRepository repository, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
    }

    public record LoginCommand(String email, String password) {}
    public record TokenResponse(String token) {}

    public TokenResponse login(LoginCommand command) {
        CommunityMember member = repository.findByEmail(command.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(command.password(), member.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("most-api")
                .issuedAt(now)
                .expiresAt(now.plus(7, ChronoUnit.DAYS))
                .subject(member.getId().toString())
                .claim("email", member.getEmail())
                .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

        return new TokenResponse(token);
    }
}
