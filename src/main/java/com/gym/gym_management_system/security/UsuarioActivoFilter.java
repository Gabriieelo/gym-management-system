package com.gym.gym_management_system.security;

import com.gym.gym_management_system.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class UsuarioActivoFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;

    public UsuarioActivoFilter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            boolean usuarioActivo = usuarioRepository.findByNombreUsuario(authentication.getName())
                    .map(usuario -> usuario.isActivo())
                    .orElse(false);
            if (!usuarioActivo) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Usuario inexistente o inactivo");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
