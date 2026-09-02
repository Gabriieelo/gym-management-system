package com.gym.gym_management_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.gym.gym_management_system.dto.LoginRequest;
import com.gym.gym_management_system.dto.LoginResponse;
import com.gym.gym_management_system.entity.RolUsuario;
import com.gym.gym_management_system.entity.Usuario;
import com.gym.gym_management_system.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioRepository usuarioRepository;

    private AuthService authService;
    private NimbusJwtDecoder decoder;
    private Instant ahora;

    @BeforeEach
    void configurar() {
        SecretKey clave = new SecretKeySpec(
                "clave-de-prueba-con-mas-de-32-caracteres-seguros"
                        .getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        ahora = Instant.parse("2026-09-02T12:00:00Z");
        authService = new AuthService(
                authenticationManager,
                NimbusJwtEncoder.withSecretKey(clave).build(),
                usuarioRepository,
                Clock.fixed(ahora, ZoneOffset.UTC),
                8
        );
        decoder = NimbusJwtDecoder.withSecretKey(clave)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Test
    void generaUnTokenFirmadoConUsuarioRolYVencimiento() {
        Authentication autenticado = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(autenticado);
        when(usuarioRepository.findByNombreUsuario("admin")).thenReturn(Optional.of(usuarioAdmin()));

        LoginResponse response = authService.login(new LoginRequest("admin", "Admin123!"));
        Jwt jwt = decoder.decode(response.token());

        assertEquals("Bearer", response.tipo());
        assertEquals("admin", jwt.getSubject());
        assertEquals(List.of("ADMIN"), jwt.getClaimAsStringList("roles"));
        assertEquals(ahora.plus(Duration.ofHours(8)), jwt.getExpiresAt());
    }

    private Usuario usuarioAdmin() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombreUsuario("admin");
        usuario.setNombreCompleto("Administrador");
        usuario.setRol(RolUsuario.ADMIN);
        usuario.setActivo(true);
        return usuario;
    }
}
