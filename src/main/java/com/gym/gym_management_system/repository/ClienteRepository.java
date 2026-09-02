package com.gym.gym_management_system.repository;

import com.gym.gym_management_system.entity.Cliente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    Optional<Cliente> findByDni(String dni);

    boolean existsByDni(String dni);

    List<Cliente> findByActivo(boolean activo);

    long countByActivo(boolean activo);
}
