package com.lab.sqli.controller;

import com.lab.sqli.dto.LoginRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SqlInjectionController {

    private final JdbcTemplate jdbcTemplate;

    public SqlInjectionController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/vulnerable/login")
    public ResponseEntity<Map<String, Object>> loginVulnerable(@RequestBody LoginRequest req) {
        String sql = "SELECT id, username, rol, email FROM usuarios " +
                     "WHERE username = '" + req.username() + "' " +
                     "AND password = '" + req.password() + "'";

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("modo", "VULNERABLE");
        response.put("consultaEjecutada", sql);

        try {
            List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sql);
            if (!resultados.isEmpty()) {
                response.put("status", "LOGIN_EXITOSO");
                response.put("usuario", resultados.get(0));
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "CREDENCIALES_INVALIDAS");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (DataAccessException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/vulnerable/buscar")
    public ResponseEntity<Map<String, Object>> buscarVulnerable(@RequestParam String username) {
        String sql = "SELECT id, username, rol, email FROM usuarios WHERE username = '" + username + "'";

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("modo", "VULNERABLE");
        response.put("consultaEjecutada", sql);

        try {
            List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sql);
            response.put("totalRegistros", resultados.size());
            response.put("resultados", resultados);
            return ResponseEntity.ok(response);
        } catch (DataAccessException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/seguro/login")
    public ResponseEntity<Map<String, Object>> loginSeguro(@RequestBody LoginRequest req) {
        String sql = "SELECT id, username, rol, email FROM usuarios WHERE username = ? AND password = ?";

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("modo", "SEGURO (Prepared Statement)");
        response.put("consultaParametrizada", sql);

        List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sql, req.username(), req.password());
        if (!resultados.isEmpty()) {
            response.put("status", "LOGIN_EXITOSO");
            response.put("usuario", resultados.get(0));
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "CREDENCIALES_INVALIDAS");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/seguro/buscar")
    public ResponseEntity<Map<String, Object>> buscarSeguro(@RequestParam String username) {
        String sql = "SELECT id, username, rol, email FROM usuarios WHERE username = ?";

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("modo", "SEGURO (Prepared Statement)");
        response.put("consultaParametrizada", sql);

        List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sql, username);
        response.put("totalRegistros", resultados.size());
        response.put("resultados", resultados);
        return ResponseEntity.ok(response);
    }
}
