package com.example.restauranteapi.controller;

import com.example.restauranteapi.entity.Usuario;
import com.example.restauranteapi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ClienteAPIController {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/clientes")
    public ResponseEntity<List<Usuario>> getListClientes(){
        var clientes = usuarioRepository.findAll();
        return ResponseEntity.ok(clientes);
    }

    @PostMapping("/clientes")
    public ResponseEntity<Usuario> saveCliente(@RequestBody Usuario cliente){
        var clienteGuardado = usuarioRepository.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteGuardado);
    }

    @PutMapping("/clientes/{id}")
    public ResponseEntity<Usuario> updateCliente(@PathVariable Long id, @RequestBody Usuario clienteEditado){
        return usuarioRepository.findById(id)
                .map(cliente -> {
                    if(clienteEditado.getNombre() != null)
                        cliente.setNombre(clienteEditado.getNombre());
                    if(clienteEditado.getEmail() != null)
                        cliente.setEmail(clienteEditado.getEmail());
                    if(clienteEditado.getTelefono() != null)
                        cliente.setTelefono(clienteEditado.getTelefono());
                    if(clienteEditado.getPassword() != null)
                        cliente.setPassword(clienteEditado.getPassword());
                    if(clienteEditado.getAuthorities() != null)
                        cliente.setAuthorities(clienteEditado.getAuthorities().stream()
                                .map(Object::toString)
                                .collect(Collectors.toList()));
                    return ResponseEntity.ok(usuarioRepository.save(cliente));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<?> deleteCliente(@PathVariable Long id){
        return usuarioRepository.findById(id)
                .map(cliente ->{
                    usuarioRepository.delete(cliente);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}