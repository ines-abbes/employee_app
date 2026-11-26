package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.demo.controller.EmployeeController;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

/**
 * Tests unitaires du contrôleur : le repository est simulé avec Mockito.
 * Aucune base de données et aucun contexte Spring ne sont démarrés.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeControllerUnitTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeController employeeController;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee(
                "Amine", "Ben Ali", "amine@example.com",
                2500L, "IT", "Developer", LocalDate.of(2024, 1, 15));
        employee.setId(1L);
    }

    @Test
    void shouldReturnAllEmployees() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        List<Employee> result = employeeController.getAllEmployees();

        assertEquals(1, result.size());
        assertEquals("Amine", result.get(0).getFname());
        verify(employeeRepository).findAll();
    }

    @Test
    void shouldCreateEmployee() {
        when(employeeRepository.save(employee)).thenReturn(employee);

        Employee result = employeeController.createEmployee(employee);

        assertEquals(1L, result.getId());
        assertEquals("amine@example.com", result.getEmail());
        verify(employeeRepository).save(employee);
    }

    @Test
    void shouldReturnEmployeeWhenIdExists() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        ResponseEntity<Employee> response = employeeController.getByID(1L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Amine", response.getBody().getFname());
        verify(employeeRepository).findById(1L);
    }

    @Test
    void shouldThrowNotFoundWhenGettingUnknownEmployee() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeController.getByID(99L));
    }

    @Test
    void shouldUpdateExistingEmployee() {
        Employee changes = new Employee(
                "Amine", "Trabelsi", "amine.new@example.com",
                3000L, "Engineering", "Senior Developer",
                LocalDate.of(2024, 2, 1));

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Employee> response =
                employeeController.updateEmployeeByID(1L, changes);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Trabelsi", response.getBody().getLname());
        assertEquals("amine.new@example.com", response.getBody().getEmail());
        assertEquals(3000L, response.getBody().getSalary());
        verify(employeeRepository).save(employee);
    }

    @Test
    void shouldDeleteExistingEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        ResponseEntity<?> response = employeeController.deleteEmployee(1L);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("Deleted"));
        verify(employeeRepository).delete(employee);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingUnknownEmployee() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> employeeController.deleteEmployee(99L));

        verify(employeeRepository, never()).delete(any(Employee.class));
    }
}
