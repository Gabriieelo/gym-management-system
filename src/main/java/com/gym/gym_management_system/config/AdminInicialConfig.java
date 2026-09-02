package com.gym.gym_management_system.config;

import com.gym.gym_management_system.entity.RolUsuario;
import com.gym.gym_management_system.entity.Usuario;
import com.gym.gym_management_system.repository.UsuarioRepository;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInicialConfig {

    @Bean
    public ApplicationRunner crearAdministradorInicial(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.username}") String nombreUsuario,
            @Value("${app.admin.password}") String password,
            @Value("${app.admin.name}") String nombreCompleto) {
        return argumentos -> {
            String usuarioNormalizado = nombreUsuario.trim().toLowerCase(Locale.ROOT);
            if (usuarioRepository.findByNombreUsuario(usuarioNormalizado).isEmpty()) {
                Usuario administrador = new Usuario();
                administrador.setNombreCompleto(nombreCompleto.trim());
                administrador.setNombreUsuario(usuarioNormalizado);
                administrador.setPassword(passwordEncoder.encode(password));
                administrador.setRol(RolUsuario.ADMIN);
                administrador.setActivo(true);
                usuarioRepository.save(administrador);
            }
        };
    }
}
