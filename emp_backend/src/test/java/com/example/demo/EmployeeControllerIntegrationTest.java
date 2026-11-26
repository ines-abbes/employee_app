package com.example.demo;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests d'intégration : requêtes HTTP réelles via MockMvc,
 * contrôleur Spring, repository JPA et base H2 en mémoire.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        employeeRepository.deleteAll();
    }

    private Employee newEmployee() {
        return new Employee(
                "Sarra", "Mansour", "sarra@example.com",
                2800L, "IT", "QA Engineer", LocalDate.of(2023, 6, 10));
    }

    @Test
    void shouldCreateEmployeeAndReturnIt() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEmployee())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.fname", is("Sarra")))
                .andExpect(jsonPath("$.email", is("sarra@example.com")));

        org.junit.jupiter.api.Assertions.assertEquals(1,
                employeeRepository.count());
    }

    @Test
    void shouldGetAllEmployees() throws Exception {
        employeeRepository.save(newEmployee());

        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].fname", is("Sarra")));
    }

    @Test
    void shouldGetEmployeeById() throws Exception {
        Employee saved = employeeRepository.save(newEmployee());

        mockMvc.perform(get("/api/v1/employees/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) saved.getId())))
                .andExpect(jsonPath("$.lname", is("Mansour")));
    }

    @Test
    void shouldReturn404WhenEmployeeDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/employees/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateEmployee() throws Exception {
        Employee saved = employeeRepository.save(newEmployee());
        Employee changes = new Employee(
                "Sarra", "Ben Salem", "sarra.updated@example.com",
                3200L, "Engineering", "Senior QA",
                LocalDate.of(2023, 6, 10));

        mockMvc.perform(put("/api/v1/employees/{id}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lname", is("Ben Salem")))
                .andExpect(jsonPath("$.salary", is(3200)));

        Employee updated = employeeRepository.findById(saved.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("Ben Salem", updated.getLname());
    }

    @Test
    void shouldDeleteEmployee() throws Exception {
        Employee saved = employeeRepository.save(newEmployee());

        mockMvc.perform(delete("/api/v1/employees/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Deleted", is(true)));

        org.junit.jupiter.api.Assertions.assertFalse(
                employeeRepository.existsById(saved.getId()));
    }

    @Test
    void shouldReturn404WhenDeletingUnknownEmployee() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/99999"))
                .andExpect(status().isNotFound());
    }
}
