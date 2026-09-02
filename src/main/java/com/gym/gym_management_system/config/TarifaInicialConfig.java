package com.gym.gym_management_system.config;

import com.gym.gym_management_system.service.TarifaService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TarifaInicialConfig {
    @Bean
    public ApplicationRunner inicializarTarifas(TarifaService servicio) {
        return args -> servicio.inicializar();
    }
}
