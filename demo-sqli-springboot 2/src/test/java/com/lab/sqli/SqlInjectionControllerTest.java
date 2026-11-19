package com.lab.sqli;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SqlInjectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginVulnerablePermiteBypassConSqlInjection() throws Exception {
        String payload = """
                {
                    "username": "admin' -- ",
                    "password": "cualquier_password"
                }
                """;

        mockMvc.perform(post("/api/vulnerable/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LOGIN_EXITOSO"))
                .andExpect(jsonPath("$.usuario.ROL").value("ADMIN"));
    }

    @Test
    void buscarVulnerablePermiteExfiltrarPasswordsConUnionSelect() throws Exception {
        String payload = "' UNION SELECT id, username, password, rol FROM usuarios -- ";

        mockMvc.perform(get("/api/vulnerable/buscar")
                        .param("username", payload))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Sup3rS3cr3tAdmin!")));
    }

    @Test
    void loginSeguroBloqueaBypassConSqlInjection() throws Exception {
        String payload = """
                {
                    "username": "admin' -- ",
                    "password": "cualquier_password"
                }
                """;

        mockMvc.perform(post("/api/seguro/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void buscarSeguroBloqueaUnionSelect() throws Exception {
        String payload = "' UNION SELECT id, username, password, rol FROM usuarios -- ";

        mockMvc.perform(get("/api/seguro/buscar")
                        .param("username", payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRegistros").value(0))
                .andExpect(jsonPath("$.resultados", hasSize(0)));
    }

    @Test
    void buscarSeguroFuncionaParaUsuarioLegitimo() throws Exception {
        mockMvc.perform(get("/api/seguro/buscar")
                        .param("username", "ana.gomez"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRegistros").value(1))
                .andExpect(jsonPath("$.resultados[0].USERNAME").value("ana.gomez"));
    }
}
