package org.example.turism_app.security;

import org.example.turism_app.config.JsonAccessDeniedHandler;
import org.example.turism_app.config.JsonAuthenticationEntryPoint;
import org.example.turism_app.config.SecurityConfig;
import org.example.turism_app.controller.TripController;
import org.example.turism_app.dto.TripRequest;
import org.example.turism_app.dto.TripResponse;
import org.example.turism_app.model.TripStatus;
import org.example.turism_app.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Лабораторна №7 — Access testing.
 *
 * <p>10 JUnit-тестів, які перевіряють обмеження доступу до REST API подорожей:
 * <table>
 *   <tr><th>Роль</th><th>GET</th><th>POST / PUT</th><th>DELETE</th></tr>
 *   <tr><td>без авторизації</td><td>401</td><td>401 / 403</td><td>401 / 403</td></tr>
 *   <tr><td>USER</td><td>200</td><td>403</td><td>403</td></tr>
 *   <tr><td>ADMIN</td><td>200</td><td>201 / 200</td><td>403</td></tr>
 *   <tr><td>SUPERADMIN</td><td>200</td><td>201 / 200</td><td>204</td></tr>
 * </table></p>
 *
 * <p>{@code @WebMvcTest} піднімає лише веб-шар: контролер + справжню конфігурацію Spring Security
 * ({@link SecurityConfig}). Запити надсилаються з Basic-авторизацією справжніх користувачів з лаби №5
 * (user, admin, superadmin). Сервіс підмінено заглушкою ({@code @MockitoBean}), тому база даних не потрібна —
 * тести перевіряють саме доступ, а не роботу з БД.</p>
 */
@WebMvcTest(TripController.class)
@Import({SecurityConfig.class, JsonAuthenticationEntryPoint.class, JsonAccessDeniedHandler.class})
@DisplayName("Лаба №7: тести доступу до /api/trips")
class TripAccessTest {

    private static final String TRIP_JSON = """
            {
              "title": "Київ — вихідні",
              "description": "Музеї, лавра і Поділ",
              "startDate": "2026-12-18",
              "endDate": "2026-12-21",
              "budget": 6000,
              "status": "PLANNED"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TripService tripService;

    @BeforeEach
    void setUp() {
        TripResponse trip = new TripResponse(
                4L, "Київ — вихідні", "Музеї, лавра і Поділ",
                LocalDate.of(2026, 12, 18), LocalDate.of(2026, 12, 21),
                new BigDecimal("6000"), TripStatus.PLANNED,
                "admin", LocalDateTime.now(), "admin", LocalDateTime.now());

        when(tripService.getAll(any())).thenReturn(List.of(trip));
        when(tripService.getById(anyLong())).thenReturn(trip);
        when(tripService.create(any(TripRequest.class))).thenReturn(trip);
        when(tripService.update(anyLong(), any(TripRequest.class))).thenReturn(trip);
    }

    // ---------------------------------------------------------------- без авторизації

    @Test
    @DisplayName("1. Без авторизації: GET /api/trips → 401 Unauthorized")
    void anonymousCannotReadTrips() throws Exception {
        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", containsString("Basic")));

        verify(tripService, never()).getAll(any());
    }

    @Test
    @DisplayName("2. Неправильний пароль: GET /api/trips → 401 Unauthorized")
    void wrongPasswordIsRejected() throws Exception {
        mockMvc.perform(get("/api/trips").with(httpBasic("admin", "wrong-password")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // ---------------------------------------------------------------- USER

    @Test
    @DisplayName("3. user: GET /api/trips → 200 OK")
    void userCanReadAllTrips() throws Exception {
        mockMvc.perform(get("/api/trips").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4));
    }

    @Test
    @DisplayName("4. user: GET /api/trips/4 → 200 OK")
    void userCanReadOneTrip() throws Exception {
        mockMvc.perform(get("/api/trips/4").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    @DisplayName("5. user: POST /api/trips → 403 Forbidden (немає прав на створення)")
    void userCannotCreateTrip() throws Exception {
        mockMvc.perform(post("/api/trips").with(httpBasic("user", "user123")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(TRIP_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("не має права")));

        verify(tripService, never()).create(any());
    }

    // ---------------------------------------------------------------- ADMIN

    @Test
    @DisplayName("6. admin: POST /api/trips → 201 Created")
    void adminCanCreateTrip() throws Exception {
        mockMvc.perform(post("/api/trips").with(httpBasic("admin", "admin123")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(TRIP_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/trips/4"));

        verify(tripService).create(any(TripRequest.class));
    }

    @Test
    @DisplayName("7. admin: PUT /api/trips/4 → 200 OK")
    void adminCanUpdateTrip() throws Exception {
        mockMvc.perform(put("/api/trips/4").with(httpBasic("admin", "admin123")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(TRIP_JSON))
                .andExpect(status().isOk());

        verify(tripService).update(eq(4L), any(TripRequest.class));
    }

    @Test
    @DisplayName("8. admin: DELETE /api/trips/4 → 403 Forbidden (видаляє лише SUPERADMIN)")
    void adminCannotDeleteTrip() throws Exception {
        mockMvc.perform(delete("/api/trips/4").with(httpBasic("admin", "admin123")).with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("SUPERADMIN")));

        verify(tripService, never()).delete(anyLong());
    }

    // ---------------------------------------------------------------- SUPERADMIN

    @Test
    @DisplayName("9. superadmin: DELETE /api/trips/4 → 204 No Content")
    void superadminCanDeleteTrip() throws Exception {
        mockMvc.perform(delete("/api/trips/4").with(httpBasic("superadmin", "superadmin123")).with(csrf()))
                .andExpect(status().isNoContent());

        verify(tripService).delete(4L);
    }

    // ---------------------------------------------------------------- CSRF (лаба №4)

    @Test
    @DisplayName("10. superadmin без CSRF-токена: POST /api/trips → 403 Forbidden")
    void requestWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/trips").with(httpBasic("superadmin", "superadmin123"))
                        .contentType(MediaType.APPLICATION_JSON).content(TRIP_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(containsString("CSRF")));

        verify(tripService, never()).create(any());
    }
}
