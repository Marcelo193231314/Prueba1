package com.casino.backend.controllers;

import com.casino.backend.models.Transaction;
import com.casino.backend.models.User;
import com.casino.backend.models.Wallet;
import com.casino.backend.repositories.TransactionRepository;
import com.casino.backend.repositories.UserRepository;
import com.casino.backend.repositories.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    // Ruta exclusiva para Administradores: ve la bitácora de todo el casino
    @GetMapping("/transactions/all")
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    // LEER: Detalles de un solo jugador (historial, balance, rol, estatus de bloqueo)
    @GetMapping("/player/{username}")
    public Object getPlayerDetails(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return "Error: Usuario no encontrado.";
        }
        
        User user = userOpt.get();
        Wallet wallet = walletRepository.findByUserId(user.getId());
        List<Transaction> transactions = transactionRepository.findByUserId(user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRole());
        response.put("balance", wallet != null ? wallet.getBalance() : 0.0);
        response.put("blocked", user.isBlocked());
        response.put("transactions", transactions);

        return response;
    }

    // LEER (Read ALL): Ver a todos los jugadores registrados
    @GetMapping("/players")
    public Object getAllPlayers() {
        return userRepository.findAll();
    }

    // ACTUALIZAR (Update): Bloquear o desbloquear a un jugador
    @PutMapping("/player/{username}/block")
    public Object toggleBlockPlayer(@PathVariable String username, @RequestParam boolean status) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return "Error: Usuario no encontrado.";
        }
        
        User user = userOpt.get();
        
        // Evitamos que un admin se bloquee a sí mismo o a otro admin
        if ("ADMIN".equals(user.getRole())) {
            return "Error: No puedes bloquear a un Administrador.";
        }
        
        user.setBlocked(status);
        userRepository.save(user);
        return status ? "El jugador " + username + " ha sido BLOQUEADO." : "El jugador " + username + " ha sido DESBLOQUEADO.";
    }

    // ELIMINAR (Delete): El administrador borra la cuenta de un jugador
    @DeleteMapping("/player/{username}")
    public Object deletePlayerAsAdmin(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            return "Error: Usuario no encontrado.";
        }
        
        User user = userOpt.get();
        
        if ("ADMIN".equals(user.getRole())) {
            return "Error: No puedes eliminar a otro Administrador.";
        }
        
        userRepository.delete(user);
        return "El jugador " + username + " ha sido eliminado de la base de datos por el administrador.";
    }
}