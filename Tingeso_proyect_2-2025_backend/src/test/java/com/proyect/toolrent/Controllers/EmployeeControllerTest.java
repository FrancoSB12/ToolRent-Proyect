package com.proyect.toolrent.Controllers;

import com.proyect.toolrent.Entities.EmployeeEntity;
import com.proyect.toolrent.Services.ClientService;
import com.proyect.toolrent.Services.EmployeeService;
import com.proyect.toolrent.Services.ValidationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeeController.class,
        excludeAutoConfiguration = { org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private ValidationService validationService;

    @MockBean
    private ClientService clientService;

    //createEmployee() tests
    //Normal flow case (success)
    @Test
    public void createEmployee_ShouldReturnNewEmployee() throws Exception {
        EmployeeEntity newEmployee = new EmployeeEntity(
                "20.372.403-0",
                "Virgil",
                "Vex",
                "virgil.vex@usach.cl",
                "+56945203964",
                false);

        //Simulate that the employee doesn't exist
        when(employeeService.exists("20.372.403-0")).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidRun(newEmployee.getRun())).thenReturn(true);
        when(validationService.isValidName(newEmployee.getName())).thenReturn(true);
        when(validationService.isValidName(newEmployee.getSurname())).thenReturn(true);
        when(validationService.isValidEmail(newEmployee.getEmail())).thenReturn(true);
        when(validationService.isValidCellphone(newEmployee.getCellphone())).thenReturn(true);

        given(employeeService.registerEmployee(Mockito.any(EmployeeEntity.class), eq("BerryDelight1"))).willReturn(newEmployee);

        String employeeJson = """
                {
                    "run": "20.372.403-0",
                    "name": "Virgil",
                    "surname": "Vex",
                    "email": "virgil.vex@usach.cl",
                    "cellphone": "+56945203964",
                    "isAdmin": "false"
                }
                """;

        mockMvc.perform(post("/api/employees")
                        .param("password", "BerryDelight1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Virgil")));
    }

    //Exceptions (bad requests or existing employee)
    @Test
    public void createEmployee_ExistingEmployee_ShouldReturnConflict() throws Exception {
        String employeeJson = """
                {
                    "run": "20.372.403-0",
                    "name": "Virgil",
                    "surname": "Vex",
                    "email": "virgil.vex@usach.cl",
                    "cellphone": "+56945203964",
                    "isAdmin": "false"
                }
                """;

        //Simulate that the employee already exists
        when(employeeService.exists("20.372.403-0")).thenReturn(true);

        mockMvc.perform(post("/api/employees")
                        .param("password", "BerryDelight1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("El empleado ya existe"));

    }

    @Test
    public void createEmployee_InvalidRun_ShouldReturnBadRequest() throws Exception {
        EmployeeEntity newEmployee = new EmployeeEntity(
                "20.565.403-0",
                "Virgil",
                "Vex",
                "virgil.vex@usach.cl",
                "+56945203964",
                false);

        //Simulate that the employee doesn't exist
        when(employeeService.exists("20.372.403-0")).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidRun(newEmployee.getRun())).thenReturn(false);
        when(validationService.isValidName(newEmployee.getName())).thenReturn(true);
        when(validationService.isValidName(newEmployee.getSurname())).thenReturn(true);
        when(validationService.isValidEmail(newEmployee.getEmail())).thenReturn(true);
        when(validationService.isValidCellphone(newEmployee.getCellphone())).thenReturn(true);

        String employeeJson = """
                {
                    "run": "20.565.403-0",
                    "name": "Virgil",
                    "surname": "Vex",
                    "email": "virgil.vex@usach.cl",
                    "cellphone": "+56945203964",
                    "isAdmin": "false"
                }
                """;

        mockMvc.perform(post("/api/employees")
                        .param("password", "BerryDelight1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Run del empleado invalido"));
    }

    @Test
    public void createEmployee_InvalidName_ShouldReturnBadRequest() throws Exception {
        EmployeeEntity newEmployee = new EmployeeEntity(
                "20.372.403-0",
                "Vi=rgil",
                "Vex",
                "virgil.vex@usach.cl",
                "+56945203964",
                false);

        //Simulate that the employee doesn't exist
        when(employeeService.exists("20.372.403-0")).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidRun(newEmployee.getRun())).thenReturn(true);
        when(validationService.isValidName(newEmployee.getName())).thenReturn(false);
        when(validationService.isValidName(newEmployee.getSurname())).thenReturn(true);
        when(validationService.isValidEmail(newEmployee.getEmail())).thenReturn(true);
        when(validationService.isValidCellphone(newEmployee.getCellphone())).thenReturn(true);

        String employeeJson = """
                {
                    "run": "20.372.403-0",
                    "name": "Vi=rgil",
                    "surname": "Vex",
                    "email": "virgil.vex@usach.cl",
                    "cellphone": "+56945203964",
                    "isAdmin": "false"
                }
                """;

        mockMvc.perform(post("/api/employees")
                        .param("password", "BerryDelight1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nombre del empleado invalido"));
    }

    @Test
    public void createEmployee_InvalidSurname_ShouldReturnBadRequest() throws Exception {
        EmployeeEntity newEmployee = new EmployeeEntity(
                "20.372.403-0",
                "Virgil",
                "V$ex",
                "virgil.vex@usach.cl",
                "+56945203964",
                false);

        //Simulate that the employee doesn't exist
        when(employeeService.exists("20.372.403-0")).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidRun(newEmployee.getRun())).thenReturn(true);
        when(validationService.isValidName(newEmployee.getName())).thenReturn(true);
        when(validationService.isValidName(newEmployee.getSurname())).thenReturn(false);
        when(validationService.isValidEmail(newEmployee.getEmail())).thenReturn(true);
        when(validationService.isValidCellphone(newEmployee.getCellphone())).thenReturn(true);

        String employeeJson = """
                {
                    "run": "20.372.403-0",
                    "name": "Virgil",
                    "surname": "V$ex",
                    "email": "virgil.vex@usach.cl",
                    "cellphone": "+56945203964",
                    "isAdmin": "false"
                }
                """;

        mockMvc.perform(post("/api/employees")
                        .param("password", "BerryDelight1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Apellido del empleado invalido"));
    }

    @Test
    public void createEmployee_InvalidEmail_ShouldReturnBadRequest() throws Exception {
        EmployeeEntity newEmployee = new EmployeeEntity(
                "20.372.403-0",
                "Virgil",
                "Vex",
                "virgil.vex@usachcl",
                "+56945203964",
                false);

        //Simulate that the employee doesn't exist
        when(employeeService.exists("20.372.403-0")).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidRun(newEmployee.getRun())).thenReturn(true);
        when(validationService.isValidName(newEmployee.getName())).thenReturn(true);
        when(validationService.isValidName(newEmployee.getSurname())).thenReturn(true);
        when(validationService.isValidEmail(newEmployee.getEmail())).thenReturn(false);
        when(validationService.isValidCellphone(newEmployee.getCellphone())).thenReturn(true);

        String employeeJson = """
                {
                    "run": "20.372.403-0",
                    "name": "Virgil",
                    "surname": "Vex",
                    "email": "virgil.vex@usachcl",
                    "cellphone": "+56945203964",
                    "isAdmin": "false"
                }
                """;

        mockMvc.perform(post("/api/employees")
                        .param("password", "BerryDelight1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email del empleado invalido"));
    }

    @Test
    public void createEmployee_InvalidCellphone_ShouldReturnBadRequest() throws Exception {
        EmployeeEntity newEmployee = new EmployeeEntity(
                "20.372.403-0",
                "Virgil",
                "Vex",
                "virgil.vex@usach.cl",
                "+5203964",
                false);

        //Simulate that the employee doesn't exist
        when(employeeService.exists("20.372.403-0")).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidRun(newEmployee.getRun())).thenReturn(true);
        when(validationService.isValidName(newEmployee.getName())).thenReturn(true);
        when(validationService.isValidName(newEmployee.getSurname())).thenReturn(true);
        when(validationService.isValidEmail(newEmployee.getEmail())).thenReturn(true);
        when(validationService.isValidCellphone(newEmployee.getCellphone())).thenReturn(false);

        String employeeJson = """
                {
                    "run": "20.372.403-0",
                    "name": "Virgil",
                    "surname": "Vex",
                    "email": "virgil.vex@usach.cl",
                    "cellphone": "+5203964",
                    "isAdmin": "false"
                }
                """;

        mockMvc.perform(post("/api/employees")
                        .param("password", "BerryDelight1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Telefono del empleado invalido"));
    }

    //getAllEmployees() tests
    //Normal flow case (success)
    @Test
    public void getAllEmployees_ShouldReturnEmployeeList() throws Exception {
        EmployeeEntity newEmployee1 = new EmployeeEntity(
                "21.365.459-4",
                "Vania",
                "Aristoza",
                "vanitas112@gmail.com",
                "+56978203641",
                false);

        EmployeeEntity newEmployee2 = new EmployeeEntity(
                "19.453.751-7",
                "Nicolas",
                "Reslen",
                "reslenMax@hotmail.com",
                "+56978124565",
                true);

        ArrayList<EmployeeEntity> employees = new ArrayList<>(Arrays.asList(newEmployee1, newEmployee2));

        given(employeeService.getAllEmployees()).willReturn(employees);

        mockMvc.perform(get("/api/employees/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Vania")))
                .andExpect(jsonPath("$[1].name", is("Nicolas")));
    }

    //Exception (empty list)
    @Test
    public void getAllEmployees_NoEmployees_ShouldReturnEmptyList() throws Exception {

        given(employeeService.getAllEmployees()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/employees/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(content().json("[]"));
    }

    //getEmployeeByRun() tests
    //Normal flow case (success)
    @Test
    public void getEmployeeByRun_ShouldReturnEmployee() throws Exception {
        EmployeeEntity employee = new EmployeeEntity(
                "20.372.403-0",
                "Virgil",
                "Vex",
                "virgil.vex@usach.cl",
                "+56945203964",
                false);

        given(employeeService.getEmployeeByRun(employee.getRun())).willReturn(Optional.of(employee));

        mockMvc.perform(get("/api/employees/{employeeRun}", "20.372.403-0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Virgil")));
    }

    //Exception (employee not found)
    @Test
    public void getEmployeeByRun_EmployeeDoesntExist_ShouldReturnNotFound() throws Exception {
        given(employeeService.getEmployeeByRun("20.372.403-0")).willReturn(Optional.empty());

        mockMvc.perform(get("/api/employees/{employeeRun}", "20.372.403-0"))
                .andExpect(status().isNotFound());
    }

    //getEmployeeByIsAdmin()
    //Normal flow case (success)
    @Test
    public void getEmployeeByIsAdmin_ShouldReturnEmployeeList() throws Exception {
        EmployeeEntity newEmployee1 = new EmployeeEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                true);

        EmployeeEntity newEmployee2 = new EmployeeEntity(
                "22.394.207-5",
                "Roman",
                "Reigns",
                "roman.empire@gmail.com",
                "+56933541202",
                true);

        EmployeeEntity newEmployee3 = new EmployeeEntity(
                "12.346.576-8",
                "Maite",
                "Mesa",
                "maite123@gmail.com",
                "+56936854123",
                false);

        EmployeeEntity newEmployee4 = new EmployeeEntity(
                "20.346.864-3",
                "Gragas",
                "Schrodinger",
                "migato@gmail.com",
                "+56936988245",
                false);

        List<EmployeeEntity> employees = Arrays.asList(newEmployee1, newEmployee2, newEmployee3, newEmployee4);

        //Simulate filtering with true
        List<EmployeeEntity> adminEmployees = employees.stream()
                .filter(EmployeeEntity::isAdmin)
                .toList();

        given(employeeService.getEmployeeIfItIsAdmin()).willReturn(adminEmployees);

        mockMvc.perform(get("/api/employees/isAdmin"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(adminEmployees.size())))
                .andExpect(jsonPath("$[0].name", is("Seth")))
                .andExpect(jsonPath("$[1].name", is("Roman")));
    }

    //Exception (there isn't admin employee)
    @Test
    public void getEmployeeByIsAdmin_NoAdmins_ShouldReturnEmptyList() throws Exception {
        given(employeeService.getEmployeeIfItIsAdmin()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/employees/isAdmin"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    //updateEmployee()
    //Normal flow case (success)
    @Test
    public void updateEmployee_ShouldReturnUpdatedEmployee() throws Exception {
        EmployeeEntity updatedEmployee = new EmployeeEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                false);

        when(employeeService.getEmployeeByRun(updatedEmployee.getRun())).thenReturn(Optional.of(updatedEmployee));

        given(employeeService.updateEmployee(eq(updatedEmployee.getRun()), Mockito.any(EmployeeEntity.class))).willReturn(updatedEmployee);

        String employeeJson = """
                {
                    "run": "20.372.403-9",
                    "name": "Dante",
                    "surname": "Vex",
                    "email": "dante.vex@usach.cl",
                    "cellphone": "+56956982015",
                    "isAdmin": false
                }
                """;

        mockMvc.perform(put("/api/employees/employee/{run}", "20.372.403-9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Dante")));
    }

    //Exceptions (empty case)
    @Test
    public void updateEmployee_RunIsMissing_ShouldReturnBadRequest() throws Exception {
        EmployeeEntity updatedEmployee = new EmployeeEntity(
                "",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                false);

        when(employeeService.getEmployeeByRun(updatedEmployee.getRun())).thenReturn(Optional.of(updatedEmployee));

        given(employeeService.updateEmployee(eq(updatedEmployee.getRun()), Mockito.any(EmployeeEntity.class))).willReturn(updatedEmployee);

        String employeeJson = """
                {
                    "run": "",
                    "name": "Dante",
                    "surname": "Vex",
                    "email": "dante.vex@usach.cl",
                    "cellphone": "+56956982015",
                    "isAdmin": false
                }
                """;

        mockMvc.perform(put("/api/employees/employee/{run}", "20.372.403-9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El run no puede estar vacio o ser nulo"));
    }

    @Test
    public void updateEmployee_EmployeeDoesntExist_ShouldReturnNotFound() throws Exception {
        EmployeeEntity updatedEmployee = new EmployeeEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                false);

        when(employeeService.getEmployeeByRun(updatedEmployee.getRun())).thenReturn(Optional.empty());

        String employeeJson = """
                {
                    "run": "20.372.403-9",
                    "name": "Dante",
                    "surname": "Vex",
                    "email": "dante.vex@usach.cl",
                    "cellphone": "+56956982015",
                    "isAdmin": false
                }
                """;

        mockMvc.perform(put("/api/employees/employee/{run}", "20.372.403-9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employeeJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("El empleado no existe en la base de datos"));
    }

    //deleteEmployeeByRun() tests
    //Normal flow case (success)
    @Test
    public void deleteEmployeeByRun_ShouldReturnNoContent() throws Exception {
        when(employeeService.deleteEmployeeByRun("20.372.403-9")).thenReturn(true);

        mockMvc.perform(delete("/api/employees/delete/{employeeRun}", "20.372.403-9"))
                .andExpect(status().isNoContent());
    }

    //Exception (employee doesn't exist)
    @Test
    public void deleteEmployeeByRun_EmployeeDoesntExist_ShouldReturnNotFound() throws Exception {
        when(employeeService.deleteEmployeeByRun("20.372.403-9")).thenReturn(false);

        mockMvc.perform(delete("/api/employees/delete/{employeeRun}", "20.372.403-9"))
                .andExpect(status().isNotFound());
    }

    //deleteClientByRun() tests
    //Normal flow case (success)
    @Test
    public void deleteClientByRun_ShouldReturnNoContent() throws Exception {
        when(clientService.deleteClientByRun("20.372.403-0")).thenReturn(true);

        mockMvc.perform(delete("/api/employees/client/delete/{clientRun}", "20.372.403-0"))
                .andExpect(status().isNoContent());
    }

    //Exception (client doesn't exist)
    @Test
    public void deleteClientByRun_ClientDoesntExist_ShouldReturnNotFound() throws Exception {
        when(clientService.deleteClientByRun("20.372.403-0")).thenReturn(false);

        mockMvc.perform(delete("/api/employees/client/delete/{clientRun}", "20.372.403-0"))
                .andExpect(status().isNotFound());
    }



}
