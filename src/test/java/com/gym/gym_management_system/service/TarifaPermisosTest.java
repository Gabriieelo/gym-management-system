package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.TarifaRequest;
import com.gym.gym_management_system.entity.Tarifa;
import com.gym.gym_management_system.repository.TarifaRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TarifaPermisosTest {
    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean
        TarifaRepository repository() {
            return mock(TarifaRepository.class);
        }

        @Bean
        TarifaService servicio(TarifaRepository repository) {
            return new TarifaService(repository);
        }
    }

    @AfterEach
    void limpiarSesion() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void userPuedeConsultarPeroNoActualizar() {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            autenticar("USER");
            var repository = context.getBean(TarifaRepository.class);
            Tarifa tarifa = new Tarifa();
            tarifa.setPaseDiario(new BigDecimal("4000.00"));
            when(repository.findById(1L)).thenReturn(Optional.of(tarifa));
            var servicio = context.getBean(TarifaService.class);
            assertEquals(new BigDecimal("4000.00"), servicio.consultar().paseDiario());
            assertThrows(AccessDeniedException.class, () -> servicio.actualizar(request()));
            verify(repository, never()).save(any());
        }
    }

    @Test
    void adminPuedeActualizar() {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            autenticar("ADMIN");
            var repository = context.getBean(TarifaRepository.class);
            Tarifa tarifa = new Tarifa();
            when(repository.findById(1L)).thenReturn(Optional.of(tarifa));
            when(repository.save(tarifa)).thenReturn(tarifa);
            var response = context.getBean(TarifaService.class).actualizar(request());
            assertEquals(new BigDecimal("45000.00"), response.cuotaEnTermino());
            verify(repository).save(tarifa);
        }
    }

    private void autenticar(String rol) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("prueba", null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + rol))));
    }

    private TarifaRequest request() {
        return new TarifaRequest(new BigDecimal("45000.00"),
                new BigDecimal("50000.00"), new BigDecimal("5000.00"));
    }
}
