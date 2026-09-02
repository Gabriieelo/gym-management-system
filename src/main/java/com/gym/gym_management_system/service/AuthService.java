package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.LoginRequest;
import com.gym.gym_management_system.dto.LoginResponse;
import com.gym.gym_management_system.entity.Usuario;
import com.gym.gym_management_system.repository.UsuarioRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UsuarioRepository usuarioRepository;
    private final Clock reloj;
    private final Duration duracionToken;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            UsuarioRepository usuarioRepository,
            Clock reloj,
            @Value("${app.security.token-hours:8}") long horasToken) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.usuarioRepository = usuarioRepository;
        this.reloj = reloj;
        this.duracionToken = Duration.ofHours(horasToken);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.nombreUsuario().trim(), request.password())
        );
        Usuario usuario = usuarioRepository.findByNombreUsuario(authentication.getName())
                .orElseThrow();

        Instant emitido = reloj.instant();
        Instant vencimiento = emitido.plus(duracionToken);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gym-management-system")
                .issuedAt(emitido)
                .expiresAt(vencimiento)
                .subject(usuario.getNombreUsuario())
                .claim("uid", usuario.getId())
                .claim("roles", List.of(usuario.getRol().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new LoginResponse(
                token,
                "Bearer",
                vencimiento,
                usuario.getId(),
                usuario.getNombreUsuario(),
                usuario.getNombreCompleto(),
                usuario.getRol()
        );
    }
}
