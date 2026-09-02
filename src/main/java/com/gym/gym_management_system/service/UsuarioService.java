package com.gym.gym_management_system.service;

import com.gym.gym_management_system.dto.CambiarPasswordRequest;
import com.gym.gym_management_system.dto.EstadoUsuarioRequest;
import com.gym.gym_management_system.dto.RestablecerPasswordRequest;
import com.gym.gym_management_system.dto.UsuarioActualizarRequest;
import com.gym.gym_management_system.dto.UsuarioRequest;
import com.gym.gym_management_system.dto.UsuarioResponse;
import com.gym.gym_management_system.entity.Usuario;
import com.gym.gym_management_system.exception.PasswordActualIncorrectaException;
import com.gym.gym_management_system.exception.UsuarioDuplicadoException;
import com.gym.gym_management_system.exception.UsuarioNoEncontradoException;
import com.gym.gym_management_system.repository.UsuarioRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse crear(UsuarioRequest request) {
        String nombreUsuario = normalizarUsuario(request.nombreUsuario());
        validarNombreDisponible(nombreUsuario, null);

        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(request.rol());
        usuario.setActivo(true);
        return convertirAResponse(usuarioRepository.save(usuario));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirAResponse)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return convertirAResponse(buscar(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse actualizar(Long id, UsuarioActualizarRequest request) {
        Usuario usuario = buscar(id);
        String nombreUsuario = normalizarUsuario(request.nombreUsuario());
        validarNombreDisponible(nombreUsuario, id);

        usuario.setNombreCompleto(request.nombreCompleto().trim());
        usuario.setNombreUsuario(nombreUsuario);
        usuario.setRol(request.rol());
        return convertirAResponse(usuarioRepository.save(usuario));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse cambiarEstado(Long id, EstadoUsuarioRequest request) {
        Usuario usuario = buscar(id);
        usuario.setActivo(request.activo());
        return convertirAResponse(usuarioRepository.save(usuario));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void restablecerPassword(Long id, RestablecerPasswordRequest request) {
        Usuario usuario = buscar(id);
        usuario.setPassword(passwordEncoder.encode(request.passwordNueva()));
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfil(String nombreUsuario) {
        return convertirAResponse(buscarPorNombre(nombreUsuario));
    }

    public void cambiarMiPassword(String nombreUsuario, CambiarPasswordRequest request) {
        Usuario usuario = buscarPorNombre(nombreUsuario);
        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPassword())) {
            throw new PasswordActualIncorrectaException();
        }
        usuario.setPassword(passwordEncoder.encode(request.passwordNueva()));
        usuarioRepository.save(usuario);
    }

    private void validarNombreDisponible(String nombreUsuario, Long usuarioActualId) {
        usuarioRepository.findByNombreUsuario(nombreUsuario)
                .filter(usuario -> !usuario.getId().equals(usuarioActualId))
                .ifPresent(usuario -> {
                    throw new UsuarioDuplicadoException(nombreUsuario);
                });
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    private Usuario buscarPorNombre(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(normalizarUsuario(nombreUsuario))
                .orElseThrow(() -> new UsuarioNoEncontradoException(nombreUsuario));
    }

    private String normalizarUsuario(String nombreUsuario) {
        return nombreUsuario.trim().toLowerCase(Locale.ROOT);
    }

    private UsuarioResponse convertirAResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getNombreUsuario(),
                usuario.getRol(),
                usuario.isActivo(),
                usuario.getFechaCreacion(),
                usuario.getFechaActualizacion()
        );
    }
}
