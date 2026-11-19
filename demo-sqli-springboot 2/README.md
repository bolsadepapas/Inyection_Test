# Laboratorio de SQL Injection (OWASP A05:2025) en Spring Boot

## 1. Descripción del Laboratorio
Este proyecto es un laboratorio práctico, autocontenido y reproducible desarrollado con **Spring Boot 3.3.5** y una base de datos **H2 en memoria**. Su objetivo es demostrar de manera controlada la vulnerabilidad **OWASP Top 10:2025 – A05:2025 Injection (SQL Injection / CWE-89)** ocasionada por la concatenación directa de cadenas en consultas SQL, así como su mitigación definitiva mediante **Consultas Parametrizadas (`PreparedStatement`)**.

## 2. Advertencia Ética
> **AVISO IMPORTANTE:** Este proyecto ha sido diseñado exclusivamente con fines educativos y académicos. Se ejecuta en un entorno local aislado con datos 100% ficticios en memoria. Las técnicas aquí expuestas no deben emplearse jamás contra sistemas sin autorización explícita.

## 3. Requisitos Previos
- **Java JDK:** 17 o superior
- **Apache Maven:** 3.8+
- **Bash y `curl`** (opcional, para ejecutar el script de pruebas desde terminal)

## 4. Ejecución del Servidor e Interfaz Web
Para iniciar la aplicación localmente:

```bash
mvn spring-boot:run
```

Una vez iniciado el servidor en el puerto `8080`, puedes acceder a:
- **Interfaz Web Interactiva (Demo en vivo):** `http://localhost:8080`
- **Consola H2 (Inspección de BD):** `http://localhost:8080/h2-console`
  - **JDBC URL:** `jdbc:h2:mem:labdb`
  - **User Name:** `sa`
  - **Password:** *(dejar en blanco)*

## 5. Ejecución de Pruebas Automatizadas y Script `curl`

### Pruebas de Integración (JUnit 5 + MockMvc)
Ejecuta la suite automatizada que verifica tanto la explotación en los endpoints vulnerables como la protección en los endpoints seguros:

```bash
mvn test
```

### Script Automatizado con `curl`
Con el servidor corriendo (`mvn spring-boot:run`), ejecuta en otra terminal:

```bash
./scripts/pruebas-sqli.sh
```

## 6. Resumen de los 6 Pasos de Explotación y Mitigación

| Paso | Escenario | Endpoint | Payload (`username`) | Resultado Esperado |
| :--- | :--- | :--- | :--- | :--- |
| **1** | Búsqueda legítima (modo vulnerable) | `GET /api/vulnerable/buscar` | `ana.gomez` | `200 OK` — Devuelve 1 registro con los datos públicos de `ana.gomez`. |
| **2** | **Ataque A:** Bypass de Autenticación | `POST /api/vulnerable/login` | `admin' -- ` | `200 OK` (`LOGIN_EXITOSO`) — El comentario `--` anula la validación de contraseña y autentica como `ADMIN`. |
| **3** | **Ataque B:** Exfiltración con `UNION SELECT` | `GET /api/vulnerable/buscar` | `' UNION SELECT id, username, password, rol FROM usuarios -- ` | `200 OK` — Inyecta una segunda consulta que expone la columna `password` de todos los usuarios (`Sup3rS3cr3tAdmin!`, etc.). |
| **4** | **Verificación 1:** Mitigación de Bypass de Login | `POST /api/seguro/login` | `admin' -- ` | `401 UNAUTHORIZED` (`CREDENCIALES_INVALIDAS`) — El `PreparedStatement` trata el payload como un literal de texto inofensivo. |
| **5** | **Verificación 2:** Mitigación de `UNION SELECT` | `GET /api/seguro/buscar` | `' UNION SELECT id, username, password, rol FROM usuarios -- ` | `200 OK` (`totalRegistros: 0`) — Busca literalmente un usuario con ese nombre exacto y devuelve lista vacía. |
| **6** | Búsqueda legítima (modo seguro) | `GET /api/seguro/buscar` | `ana.gomez` | `200 OK` (`totalRegistros: 1`) — Confirma que la funcionalidad legítima opera sin inconvenientes. |
