package com.casino.backend.controllers;

import com.casino.backend.models.User;
import com.casino.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // ACTUALIZAR (Update): El usuario cambia su propio nombre
    @PutMapping("/update-username")
    public Object updateUsername(@RequestParam String newUsername) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        
        if (userRepository.findByUsername(newUsername).isPresent()) {
            return "Error: El nombre de usuario '" + newUsername + "' ya está ocupado.";
        }
        
        User user = userRepository.findByUsername(currentUsername).get();
        user.setUsername(newUsername);
        userRepository.save(user);
        
        return "Nombre actualizado con éxito a: " + newUsername + ". (Nota: Deberás hacer Login de nuevo con tu nuevo nombre).";
    }

    // ELIMINAR (Delete): El usuario borra su propia cuenta
    @DeleteMapping("/delete-account")
    public Object deleteMyAccount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).get();
        
        userRepository.delete(user); // Como tienes ddl-auto=update y relaciones, esto borrará al usuario.
        return "Tu cuenta ha sido eliminada permanentemente. ¡Hasta pronto!";
    }
}