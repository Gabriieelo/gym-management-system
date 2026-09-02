package com.gym.gym_management_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gym.gym_management_system.dto.CambiarPasswordRequest;
import com.gym.gym_management_system.dto.UsuarioRequest;
import com.gym.gym_management_system.dto.UsuarioResponse;
import com.gym.gym_management_system.entity.RolUsuario;
import com.gym.gym_management_system.entity.Usuario;
import com.gym.gym_management_system.exception.PasswordActualIncorrectaException;
import com.gym.gym_management_system.exception.UsuarioDuplicadoException;
import com.gym.gym_management_system.repository.UsuarioRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private PasswordEncoder passwordEncoder;
    private UsuarioService usuarioService;

    @BeforeEach
    void configurar() {
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    @Test
    void creaUnUsuarioConPasswordCifradaYNombreNormalizado() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> {
            Usuario usuario = invocacion.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        UsuarioResponse response = usuarioService.crear(new UsuarioRequest(
                " Juan Pérez ", " Juan.Perez ", "Password123", RolUsuario.USER));

        assertEquals("juan.perez", response.nombreUsuario());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void noGuardaLaPasswordComoTextoPlano() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> {
            Usuario usuario = invocacion.getArgument(0);
            assertNotEquals("Password123", usuario.getPassword());
            assertTrue(passwordEncoder.matches("Password123", usuario.getPassword()));
            return usuario;
        });

        usuarioService.crear(new UsuarioRequest(
                "Juan Pérez", "juan", "Password123", RolUsuario.USER));
    }

    @Test
    void rechazaUnNombreDeUsuarioDuplicado() {
        Usuario existente = usuarioExistente();
        when(usuarioRepository.findByNombreUsuario("juan")).thenReturn(Optional.of(existente));

        assertThrows(
                UsuarioDuplicadoException.class,
                () -> usuarioService.crear(new UsuarioRequest(
                        "Otro Juan", "JUAN", "Password123", RolUsuario.USER))
        );
    }

    @Test
    void cambiaLaPasswordCuandoLaActualEsCorrecta() {
        Usuario usuario = usuarioExistente();
        when(usuarioRepository.findByNombreUsuario("juan")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        usuarioService.cambiarMiPassword(
                "juan",
                new CambiarPasswordRequest("Password123", "Password456")
        );

        assertTrue(passwordEncoder.matches("Password456", usuario.getPassword()));
    }

    @Test
    void rechazaElCambioCuandoLaPasswordActualEsIncorrecta() {
        Usuario usuario = usuarioExistente();
        when(usuarioRepository.findByNombreUsuario("juan")).thenReturn(Optional.of(usuario));

        assertThrows(
                PasswordActualIncorrectaException.class,
                () -> usuarioService.cambiarMiPassword(
                        "juan",
                        new CambiarPasswordRequest("Incorrecta", "Password456")
                )
        );
    }

    private Usuario usuarioExistente() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombreCompleto("Juan Pérez");
        usuario.setNombreUsuario("juan");
        usuario.setPassword(passwordEncoder.encode("Password123"));
        usuario.setRol(RolUsuario.USER);
        usuario.setActivo(true);
        return usuario;
    }
}
