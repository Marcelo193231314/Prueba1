package com.casino.backend.controllers;

import com.casino.backend.models.User;
import com.casino.backend.models.Wallet;
import com.casino.backend.repositories.UserRepository;
import com.casino.backend.repositories.WalletRepository;
import com.casino.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository; // Agregamos la herramienta de la billetera

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return "Error: El usuario ya existe";
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER"); 
        
        // 1. Guardamos al nuevo usuario en la base de datos
        User savedUser = userRepository.save(user);

        // 2. Le creamos su billetera con $1000.0 de regalo inicial
        Wallet newWallet = new Wallet(1000.0, savedUser);
        walletRepository.save(newWallet);
        
        return "Usuario registrado exitosamente con bono de bienvenida de $1000";
    }

    @PostMapping("/login")
    public String login(@RequestBody User loginRequest) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return jwtUtil.generateToken(user.getUsername(), user.getRole());
            }
        }
        return "Error: Credenciales inválidas";
    }
}