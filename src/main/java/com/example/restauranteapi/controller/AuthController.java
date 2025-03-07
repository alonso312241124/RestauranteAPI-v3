package com.example.restauranteapi.controller;

import com.example.restauranteapi.DTO.LoginRequestDTO;
import com.example.restauranteapi.DTO.LoginResponseDTO;
import com.example.restauranteapi.DTO.UserRegisterDTO;
import com.example.restauranteapi.config.JwtTokenProvider;
import com.example.restauranteapi.entity.Usuario;
import com.example.restauranteapi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    // Registro de usuario
    @PostMapping("/auth/register")
    public ResponseEntity<Map<String, String>> save(@RequestBody UserRegisterDTO userDTO) {
        try {
            // Verificar si el email ya está en uso
            if (usuarioRepository.findByEmail(userDTO.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Email ya en uso"));
            }

            // Crear y guardar el nuevo usuario
            Usuario usuario = Usuario.builder()
                    .email(userDTO.getEmail())
                    .password(passwordEncoder.encode(userDTO.getPassword()))
                    .nombre(userDTO.getNombre())
                    .telefono(userDTO.getTelefono())
                    .authorities(List.of("ROLE_USER")) // Asignar roles
                    .build();

            usuarioRepository.save(usuario);

            // Devolver respuesta exitosa
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of("email", usuario.getEmail(),
                            "nombre", usuario.getNombre())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Error al registrar el usuario"));
        }
    }

    // Login de usuario
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginDTO) {
        try {
            // Validar al usuario en Spring Security
            UsernamePasswordAuthenticationToken userPassAuthToken = new UsernamePasswordAuthenticationToken(
                    loginDTO.getEmail(), loginDTO.getPassword());
            Authentication auth = authenticationManager.authenticate(userPassAuthToken);

            // Obtener el usuario autenticado
            Usuario usuario = (Usuario) auth.getPrincipal();

            // Generar el token JWT
            String token = tokenProvider.generateToken(auth);

            // Devolver respuesta exitosa con el token
            return ResponseEntity.ok(new LoginResponseDTO(usuario.getEmail(), token));
        } catch (Exception e) {
            // Manejar errores de autenticación
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of(
                            "path", "/auth/login",
                            "message", "Credenciales erróneas",
                            "timestamp", new Date()
                    )
            );
        }
    }
}