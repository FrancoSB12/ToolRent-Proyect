package com.proyect.toolrent.Services;


import com.proyect.toolrent.Entities.EmployeeEntity;
import com.proyect.toolrent.Repositories.EmployeeRepository;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class EmployeeServiceTest {

    ValidationService validationService = mock(ValidationService.class);
    EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    KeycloakUserService keycloakUserService = mock(KeycloakUserService.class);

    EmployeeService employeeService = new EmployeeService(employeeRepository, keycloakUserService, validationService);

    //getAllEmployees() tests
    //Normal flow case (success)
    @Test
    void whenGetAllEmployees_thenReturnsList() {
        // Given
        EmployeeEntity employee1 = new EmployeeEntity(
                "21.365.459-4",
                "Vania",
                "Aristoza",
                "vanitas112@gmail.com",
                "+56978203641",
                false);

        EmployeeEntity employee2 = new EmployeeEntity(
                "19.453.751-7",
                "Nicolas",
                "Reslen",
                "reslenMax@hotmail.com",
                "+56978124565",
                true);

        ArrayList<EmployeeEntity> employees = new ArrayList<>(Arrays.asList(employee1, employee2));

        when(employeeRepository.findAll()).thenReturn(employees);

        // When
        List<EmployeeEntity> result = employeeService.getAllEmployees();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Vania");
        assertThat(result.get(1).getName()).isEqualTo("Nicolas");
    }

    //Exception (empty list)
    @Test
    void whenGetAllEmployeesEmpty_thenReturnsEmptyList() {
        // Given
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<EmployeeEntity> result = employeeService.getAllEmployees();

        // Then
        assertThat(result).isEmpty();
    }

    //getEmployeeByRun() tests
    //Normal flow case (success)
    @Test
    void whenGetEmployeeByRun_thenReturnsEmployee() {
        // Given
        EmployeeEntity employee = new EmployeeEntity(
                "21.365.459-4",
                "Vania",
                "Aristoza",
                "vanitas112@gmail.com",
                "+56978203641",
                false);

        when(employeeRepository.findById(employee.getRun())).thenReturn(Optional.of(employee));

        // When
        Optional<EmployeeEntity> result = employeeService.getEmployeeByRun(employee.getRun());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getRun()).isEqualTo(employee.getRun());
    }

    //Exception (Employee not found)
    @Test
    void whenGetEmployeeByRunNotFound_thenReturnsEmpty() {
        // Given
        String run = "99.999.999-K";
        when(employeeRepository.findById(run)).thenReturn(Optional.empty());

        // When
        Optional<EmployeeEntity> result = employeeService.getEmployeeByRun(run);

        // Then
        assertThat(result).isEmpty();
    }

    //getEmployeeIfItIsAdmin() tests
    //Normal flow case (success)
    @Test
    void whenGetEmployeeIfItIsAdmin_thenReturnsAdminList() {
        // Given
        EmployeeEntity adminEmployee = new EmployeeEntity(
                "21.365.459-4",
                "Vania",
                "Aristoza",
                "vanitas112@gmail.com",
                "+56978203641",
                true);

        EmployeeEntity adminEmployee2 = new EmployeeEntity(
                "19.453.751-7",
                "Nicolas",
                "Reslen",
                "reslenMax@hotmail.com",
                "+56978124565",
                true);

        List<EmployeeEntity> list = List.of(adminEmployee, adminEmployee2);

        when(employeeRepository.findByIsAdmin(true)).thenReturn(list);

        // When
        List<EmployeeEntity> result = employeeService.getEmployeeIfItIsAdmin();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).isAdmin()).isTrue();
        assertThat(result.get(1).isAdmin()).isTrue();
    }

    //exists() tests
    //Normal flow case (success)
    @Test
    void whenExists_thenReturnsTrue() {
        // Given
        EmployeeEntity employee = new EmployeeEntity(
                "21.365.459-4",
                "Vania",
                "Aristoza",
                "vanitas112@gmail.com",
                "+56978203641",
                false);

        when(employeeRepository.existsById(employee.getRun())).thenReturn(true);

        // When
        boolean exists = employeeService.exists(employee.getRun());

        // Then
        assertThat(exists).isTrue();
    }

    //Exception (employee doesn't exist)
    @Test
    void whenNotExists_thenReturnsFalse() {
        // Given
        String run = "99.999.999-9";
        when(employeeRepository.existsById(run)).thenReturn(false);

        // When
        boolean exists = employeeService.exists(run);

        // Then
        assertThat(exists).isFalse();
    }

    //registerEmployee() tests
    //Normal flow case (success)
    @Test
    void whenRegisterEmployeeAdmin_thenSavesAndCreatesKeycloakAdmin() {
        // Given
        EmployeeEntity newAdminEmployee = new EmployeeEntity(
                "21.365.459-4",
                "Vania",
                "Aristoza",
                "vanitas112@gmail.com",
                "+56978203641",
                true);

        String password = "pass";
        String userId = "user-uuid-123";

        when(employeeRepository.save(newAdminEmployee)).thenReturn(newAdminEmployee);
        when(keycloakUserService.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(userId);

        // When
        EmployeeEntity result = employeeService.registerEmployee(newAdminEmployee, password);

        // Then

        assertThat(result).isNotNull();
        //Verify that it saved in the DB
        verify(employeeRepository).save(newAdminEmployee);

        //Verify that it created an user in keycloak
        verify(keycloakUserService).createUser("21.365.459-4", "vanitas112@gmail.com", "Vania", "Aristoza", "pass");

        //Verify that it assigned the Admin role
        verify(keycloakUserService).assignRole(userId, "Admin");
    }

    @Test
    void whenRegisterEmployeeRegular_thenSavesAndCreatesKeycloakEmployee() {
        // Given
        EmployeeEntity newEmployee = new EmployeeEntity(
                "21.365.459-4",
                "Vania",
                "Aristoza",
                "vanitas112@gmail.com",
                "+56978203641",
                false);

        String password = "pass";
        String userId = "user-uuid-456";

        when(employeeRepository.save(newEmployee)).thenReturn(newEmployee);
        when(keycloakUserService.createUser(any(), any(), any(), any(), any())).thenReturn(userId);

        // When
        employeeService.registerEmployee(newEmployee, password);

        // Then
        //Verify that it assigned the Employee role
        verify(keycloakUserService).assignRole(userId, "Employee");
    }

    //updateEmployee() tests
    //Normal flow case (success)
    @Test
    void whenUpdateEmployeeSuccess_thenReturnsUpdatedEmployee() {
        // Given
        EmployeeEntity existingEmployee = new EmployeeEntity(
                "21.365.459-4",
                "Vania",
                "Aristoza",
                "vanitas112@gmail.com",
                "+56978203641",
                false);

        EmployeeEntity updatedEmployee = new EmployeeEntity(
                null,
                "Ariana",
                null,
                "Malaria@gmail.com",
                null,
                false);

        when(employeeRepository.findById(existingEmployee.getRun())).thenReturn(Optional.of(existingEmployee));

        when(validationService.isValidName(anyString())).thenReturn(true);
        when(validationService.isValidEmail(anyString())).thenReturn(true);

        when(employeeRepository.save(any(EmployeeEntity.class))).thenAnswer(i -> i.getArgument(0));

        // When
        EmployeeEntity result = employeeService.updateEmployee(existingEmployee.getRun(), updatedEmployee);

        // Then
        assertThat(result.getName()).isEqualTo("Ariana");
        assertThat(result.getEmail()).isEqualTo("Malaria@gmail.com");
    }

    //Exceptions (employee not found and bad requests)
    @Test
    void whenUpdateEmployeeNotFound_thenThrowsNoSuchElementException() {
        // Given
        String run = "99.999.999-9";
        EmployeeEntity updates = new EmployeeEntity();

        when(employeeRepository.findById(run)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> employeeService.updateEmployee(run, updates))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void whenUpdateEmployeeInvalidName_thenThrowsIllegalArgumentException() {
        // Given
        EmployeeEntity existingEmployee = new EmployeeEntity(
                "21.365.459-4",
                "Vania",
                "Aristoza",
                "vanitas112@gmail.com",
                "+56978203641",
                false);

        EmployeeEntity updatedEmployee = new EmployeeEntity(
                null,
                "Ari#ana",
                null,
                null,
                null,
                false);

        when(employeeRepository.findById(existingEmployee.getRun())).thenReturn(Optional.of(existingEmployee));
        when(validationService.isValidName(updatedEmployee.getName())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> employeeService.updateEmployee(existingEmployee.getRun(), updatedEmployee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nombre del empleado invalido");
    }

    @Test
    void whenUpdateEmployeeInvalidSurname_thenThrowsIllegalArgumentException() {
        // Given
        EmployeeEntity existingEmployee = new EmployeeEntity(
                "21.365.459-4",
                "Vania",
                "Aristoza",
                "vanitas112@gmail.com",
                "+56978203641",
                false);

        EmployeeEntity updatedEmployee = new EmployeeEntity(
                null,
                null,
                "ALB(#Ear",
                null,
                null,
                false);

        when(employeeRepository.findById(existingEmployee.getRun())).thenReturn(Optional.of(existingEmployee));
        when(validationService.isValidName(updatedEmployee.getSurname())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> employeeService.updateEmployee(existingEmployee.getRun(), updatedEmployee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Apellido del empleado invalido");
    }

    @Test
    void whenUpdateEmployeeInvalidEmail_thenThrowsIllegalArgumentException() {
        // Given
        EmployeeEntity existingEmployee = new EmployeeEntity(
                "21.365.459-4",
                "Vania",
                "Aristoza",
                "vanitas112@gmail.com",
                "+56978203641",
                false);

        EmployeeEntity updatedEmployee = new EmployeeEntity(
                null,
                null,
                null,
                "aslerower@docker",
                null,
                false);

        when(employeeRepository.findById(existingEmployee.getRun())).thenReturn(Optional.of(existingEmployee));
        when(validationService.isValidEmail(updatedEmployee.getEmail())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> employeeService.updateEmployee(existingEmployee.getRun(), updatedEmployee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email del empleado invalido");
    }

    @Test
    void whenUpdateEmployeeInvalidCellphone_thenThrowsIllegalArgumentException() {
        // Given
        EmployeeEntity existingEmployee = new EmployeeEntity(
                "21.365.459-4",
                "Vania",
                "Aristoza",
                "vanitas112@gmail.com",
                "+56978203641",
                false);

        EmployeeEntity updatedEmployee = new EmployeeEntity(
                null,
                null,
                null,
                null,
                "898778",
                false);

        when(employeeRepository.findById(existingEmployee.getRun())).thenReturn(Optional.of(existingEmployee));
        when(validationService.isValidCellphone(updatedEmployee.getCellphone())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> employeeService.updateEmployee(existingEmployee.getRun(), updatedEmployee))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Teléfono del empleado invalido");
    }

    //deleteEmployeeByRun() tests
    //Normal flow case (success)
    @Test
    void whenDeleteEmployeeByRunExists_thenReturnsTrue() {
        // Given
        EmployeeEntity existingEmployee = new EmployeeEntity(
                "21.365.459-4",
                "Vania",
                "Aristoza",
                "vanitas112@gmail.com",
                "+56978203641",
                false);

        when(employeeRepository.existsById(existingEmployee.getRun())).thenReturn(true);

        // When
        boolean result = employeeService.deleteEmployeeByRun(existingEmployee.getRun());

        // Then
        assertThat(result).isTrue();
        verify(employeeRepository).deleteById(existingEmployee.getRun());
    }

    //Exceptions (employee not found)
    @Test
    void whenDeleteEmployeeByRunNotExists_thenReturnsFalse() {
        // Given
        String run = "99.999.999-9";
        when(employeeRepository.existsById(run)).thenReturn(false);

        // When
        boolean result = employeeService.deleteEmployeeByRun(run);

        // Then
        assertThat(result).isFalse();
        verify(employeeRepository, never()).deleteById(any());
    }

}
