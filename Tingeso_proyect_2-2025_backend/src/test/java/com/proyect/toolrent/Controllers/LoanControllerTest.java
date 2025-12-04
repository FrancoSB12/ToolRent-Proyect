package com.proyect.toolrent.Controllers;

import com.proyect.toolrent.Entities.*;
import com.proyect.toolrent.Enums.ToolDamageLevel;
import com.proyect.toolrent.Enums.ToolStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyect.toolrent.Services.ClientService;
import com.proyect.toolrent.Services.LoanService;
import com.proyect.toolrent.Services.SystemConfigurationService;
import com.proyect.toolrent.Services.ValidationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LoanController.class,
        excludeAutoConfiguration = { org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class LoanControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LoanService loanService;

    @MockBean
    private ValidationService validationService;

    @MockBean
    private ClientService clientService;

    @MockBean
    private SystemConfigurationService sysConfigService;

    //createLoan() tests
    //Normal flow case (success)
    @Test
    public void createLoan_ShouldReturnNewLoan() throws Exception {
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        EmployeeEntity employee = new EmployeeEntity(
                "12.346.576-8",
                "Maite",
                "Mesa",
                "maite123@gmail.com",
                "+56936854123",
                false);

        ToolTypeEntity toolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        ToolTypeEntity toolType2 = new ToolTypeEntity(
                19L,
                "CRAFTSMAN Martillo, fibra de vidrio, 16 onzas",
                "Martillo",
                "CMHT51398",
                10000,
                24,
                14,
                3000,
                6000);

        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType);

        ToolItemEntity toolItem2 = new ToolItemEntity(
                4L,
                "452103684",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType2);

        LoanXToolItemEntity loanTools = new LoanXToolItemEntity(
                2L,
                null,
                toolItem);

        LoanXToolItemEntity loanTools2 = new LoanXToolItemEntity(
                3L,
                null,
                toolItem2);

        List<LoanXToolItemEntity> loanToolsList = Arrays.asList(loanTools, loanTools2);

        LoanEntity newLoan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                10000,
                "Activo",
                "Vigente",
                client,
                employee,
                loanToolsList);

        //Simulate that the loan doesn't exist
        when(loanService.exists(anyLong())).thenReturn(false);

        //Simulate that the client exist
        when(clientService.exists(client.getRun())).thenReturn(true);

        //Simulates verifications
        when(validationService.isValidDate(newLoan.getLoanDate())).thenReturn(true);
        when(validationService.isValidReturnDate(newLoan.getReturnDate(), newLoan.getLoanDate())).thenReturn(true);

        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn(employee.getRun());

        given(loanService.createLoan(Mockito.any(LoanEntity.class), eq(employee.getRun()))).willReturn(newLoan);

        String loanJson = objectMapper.writeValueAsString(newLoan);

        mockMvc.perform(post("/api/loans")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(13)));

    }

    //Exceptions (bad requests or existing loan)
    @Test
    public void createLoan_ExistingLoan_ShouldReturnConflict() throws Exception {
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        EmployeeEntity employee = new EmployeeEntity(
                "12.346.576-8",
                "Maite",
                "Mesa",
                "maite123@gmail.com",
                "+56936854123",
                false);


        LoanEntity newLoan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                10000,
                "Activo",
                "Vigente",
                client,
                employee,
                null);

        String loanJson = objectMapper.writeValueAsString(newLoan);

        //Simulate that the loan already exists
        when(loanService.exists(anyLong())).thenReturn(true);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("El prestamo ya existe en la base de datos"));

    }

    @Test
    public void createLoan_NonExistingClient_ShouldReturnNotFound() throws Exception {
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        EmployeeEntity employee = new EmployeeEntity(
                "12.346.576-8",
                "Maite",
                "Mesa",
                "maite123@gmail.com",
                "+56936854123",
                false);

        LoanEntity newLoan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                10000,
                "Activo",
                "Vigente",
                client,
                employee,
                null);

        String loanJson = objectMapper.writeValueAsString(newLoan);

        //Simulate that the loan doesn't exists
        when(loanService.exists(anyLong())).thenReturn(false);

        //Simulate that the client doesn't exists
        when(clientService.exists(anyString())).thenReturn(false);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cliente no encontrado en la base de datos"));
    }

    @Test
    public void createLoan_InvalidLoanDate_ShouldReturnBadRequest() throws Exception {
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        EmployeeEntity employee = new EmployeeEntity(
                "12.346.576-8",
                "Maite",
                "Mesa",
                "maite123@gmail.com",
                "+56936854123",
                false);

        LoanEntity newLoan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10).minusDays(3),
                LocalDate.of(2025, 5, 24),
                10000,
                "Activo",
                "Vigente",
                client,
                employee,
                null);

        //Simulate that the loan doesn't exist
        when(loanService.getLoanById(newLoan.getId())).thenReturn(Optional.empty());

        //Simulate that the client exist
        when(clientService.exists(anyString())).thenReturn(true);

        //Simulates verifications
        when(validationService.isValidDate(newLoan.getLoanDate())).thenReturn(false);
        when(validationService.isValidReturnDate(newLoan.getReturnDate(), newLoan.getLoanDate())).thenReturn(true);

        String loanJson = objectMapper.writeValueAsString(newLoan);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Fecha de arriendo incorrecta"));
    }

    @Test
    public void createLoan_InvalidReturnDate_ShouldReturnBadRequest() throws Exception {
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        EmployeeEntity employee = new EmployeeEntity(
                "12.346.576-8",
                "Maite",
                "Mesa",
                "maite123@gmail.com",
                "+56936854123",
                false);

        LoanEntity newLoan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24).minusDays(20),
                10000,
                "Activo",
                "Vigente",
                client,
                employee,
                null);

        //Simulate that the loan doesn't exist
        when(loanService.getLoanById(newLoan.getId())).thenReturn(Optional.empty());

        //Simulate that the client exist
        when(clientService.exists(anyString())).thenReturn(true);

        //Simulates verifications
        when(validationService.isValidDate(newLoan.getLoanDate())).thenReturn(true);
        when(validationService.isValidReturnDate(newLoan.getReturnDate(), newLoan.getLoanDate())).thenReturn(false);

        String loanJson = objectMapper.writeValueAsString(newLoan);

        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Fecha de devolución incorrecta"));
    }


    //getAllLoans() tests
    //Normal flow case (success)
    @Test
    public void getAllLoans_ShouldReturnLoanList() throws Exception {
        LoanEntity loan1 = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                15000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        LoanEntity loan2 = new LoanEntity(
                24L,
                LocalDate.of(2024, 7, 21),
                LocalDate.of(2024, 8, 21),
                20000,
                "Finalizado",
                "Vigente",
                null,
                null,
                null);

        ArrayList<LoanEntity> loans = new ArrayList<>(Arrays.asList(loan1, loan2));

        given(loanService.getAllLoans()).willReturn(loans);

        mockMvc.perform(get("/api/loans/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(loan1.getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(loan2.getId().intValue())));
    }

    //Exception (empty list)
    @Test
    public void getAllLoans_NoLoans_ShouldReturnEmptyList() throws Exception {

        given(loanService.getAllLoans()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/loans/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(content().json("[]"));
    }

    //getLoanById() tests
    //Normal flow case (success)
    @Test
    public void getLoanById_ShouldReturnLoan() throws Exception {
        LoanEntity loan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                15000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        given(loanService.getLoanById(loan.getId())).willReturn(Optional.of(loan));

        mockMvc.perform(get("/api/loans/{id}", loan.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(loan.getId().intValue())));
    }

    //Exception (loan not found)
    @Test
    public void getLoanById_LoanDoesntExist_ShouldReturnNotFound() throws Exception {
        given(loanService.getLoanById(13L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/loans/{id}", 13L))
                .andExpect(status().isNotFound());
    }

    //getActiveLoansByClient() tests
    //Normal flow case (success)
    @Test
    public void getActiveLoansByClient_ShouldReturnActiveLoanList() throws Exception {
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        LoanEntity loan1 = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                15000,
                "Activo",
                "Vigente",
                client,
                null,
                null);

        LoanEntity loan2 = new LoanEntity(
                52L,
                LocalDate.of(2024, 3, 17),
                LocalDate.of(2024, 3, 31),
                23000,
                "Finalizado",
                "Vigente",
                null,
                null,
                null);

        LoanEntity loan3 = new LoanEntity(
                104L,
                LocalDate.of(2025, 1, 5),
                LocalDate.of(2025, 2, 5),
                23000,
                "Finalizado",
                "Vigente",
                client,
                null,
                null);

        ArrayList<LoanEntity> loans = new ArrayList<>(Arrays.asList(loan1, loan2, loan3));

        //Simulate that the client exist
        when(clientService.exists(anyString())).thenReturn(true);

        given(loanService.getActiveLoansByClient(client.getRun())).willReturn(loans);

        mockMvc.perform(get("/api/loans/client/{run}", client.getRun()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(loan1.getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(loan2.getId().intValue())))
                .andExpect(jsonPath("$[2].id", is(loan3.getId().intValue())));
    }

    //Exceptions (client not found, and empty list)
    @Test
    public void getActiveLoansByClient_ClientDoesntExist_ShouldReturnNotFound() throws Exception {
        when(clientService.exists(anyString())).thenReturn(false);

        mockMvc.perform(get("/api/loans/client/{run}", "20.372.403-0"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cliente no encontrado en la base de datos"));
    }

    @Test
    public void getActiveLoansByClient_NoLoans_ShouldReturnEmptyList() throws Exception {
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        //Simulate that the client exist
        when(clientService.exists(anyString())).thenReturn(true);

        given(loanService.getActiveLoansByClient(client.getRun())).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/loans/client/{run}", client.getRun()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(content().json("[]"));
    }

    //getLoanByStatus() tests
    //Normal flow case (success)
    @Test
    public void getLoanByStatus_ShouldReturnLoanList() throws Exception {
        LoanEntity loan1 = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                15000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        LoanEntity loan2 = new LoanEntity(
                52L,
                LocalDate.of(2024, 3, 17),
                LocalDate.of(2024, 3, 31),
                23000,
                "Finalizado",
                "Vigente",
                null,
                null,
                null);

        LoanEntity loan3 = new LoanEntity(
                104L,
                LocalDate.of(2025, 1, 5),
                LocalDate.of(2025, 2, 5),
                23000,
                "Finalizado",
                "Vigente",
                null,
                null,
                null);

        List<LoanEntity> loans =  Arrays.asList(loan1, loan2, loan3);

        //Simulate filtering with status "Finalizado"
        List<LoanEntity> finishedLoans = loans.stream()
                .filter(c -> c.getStatus().equals("Finalizado"))
                .toList();

        given(loanService.getLoanByStatus("Finalizado")).willReturn(finishedLoans);

        mockMvc.perform(get("/api/loans/status/{status}", "Finalizado"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(finishedLoans.size())))
                .andExpect(jsonPath("$[0].id", is(loan2.getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(loan3.getId().intValue())));
    }

    //Exception (no loan has that status)
    @Test
    public void getLoanByStatus_NoLoans_ShouldReturnEmptyList() throws Exception {
        given(loanService.getLoanByStatus("Activo")).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/loans/status/{status}", "Activo"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    //getLoanByValidity() tests
    //Normal flow case (success)
    @Test
    public void getLoanByValidity_ShouldReturnLoanList() throws Exception {
        LoanEntity loan1 = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2026, 5, 24),
                15000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        LoanEntity loan2 = new LoanEntity(
                52L,
                LocalDate.of(2024, 3, 17),
                LocalDate.of(2024, 3, 31),
                23000,
                "Finalizado",
                "Atrasado",
                null,
                null,
                null);

        LoanEntity loan3 = new LoanEntity(
                104L,
                LocalDate.of(2025, 1, 5),
                LocalDate.of(2025, 2, 5),
                23000,
                "Finalizado",
                "Atrasado",
                null,
                null,
                null);

        List<LoanEntity> loans =  Arrays.asList(loan1, loan2, loan3);

        //Simulate filtering with validity "Atrasado"
        List<LoanEntity> OverdueLoans = loans.stream()
                .filter(c -> c.getValidity().equals("Atrasado"))
                .toList();

        given(loanService.getLoanByValidity("Atrasado")).willReturn(OverdueLoans);

        mockMvc.perform(get("/api/loans/validity/{validity}", "Atrasado"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(OverdueLoans.size())))
                .andExpect(jsonPath("$[0].id", is(loan2.getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(loan3.getId().intValue())));
    }

    //Exception (no loan has that validity)
    @Test
    public void getLoanByValidity_NoLoans_ShouldReturnEmptyList() throws Exception {
        given(loanService.getLoanByValidity("Atrasado")).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/loans/validity/{validity}", "Atrasado"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    //getMostLoanedTools() tests
    //Normal flow case (success)
    @Test
    public void getMostLoanedTools_ShouldReturnMostLoanedTool() throws Exception {
        ToolTypeEntity toolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        ToolTypeEntity toolType2 = new ToolTypeEntity(
                19L,
                "CRAFTSMAN Martillo, fibra de vidrio, 16 onzas",
                "Martillo",
                "CMHT51398",
                10000,
                24,
                14,
                3000,
                6000);

        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType);

        ToolItemEntity toolItem2 = new ToolItemEntity(
                4L,
                "452103684",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType2);

        LoanXToolItemEntity loanTools = new LoanXToolItemEntity(
                2L,
                null,
                toolItem);

        LoanXToolItemEntity loanTools2 = new LoanXToolItemEntity(
                3L,
                null,
                toolItem2);

        LoanXToolItemEntity loanTools3 = new LoanXToolItemEntity(
                2L,
                null,
                toolItem);

        LoanXToolItemEntity loanTools4 = new LoanXToolItemEntity(
                3L,
                null,
                toolItem);

        List<LoanXToolItemEntity> loanToolsList_Tool = Arrays.asList(loanTools, loanTools3, loanTools4);
        List<LoanXToolItemEntity> loanToolsList_Tool2 = List.of(loanTools2);

        LoanEntity loan1 = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                15000,
                "Activo",
                "Vigente",
                null,
                null,
                loanToolsList_Tool);

        LoanEntity loan2 = new LoanEntity(
                52L,
                LocalDate.of(2024, 3, 17),
                LocalDate.of(2024, 3, 31),
                23000,
                "Finalizado",
                "Vigente",
                null,
                null,
                loanToolsList_Tool2);

        LoanEntity loan3 = new LoanEntity(
                104L,
                LocalDate.of(2025, 1, 5),
                LocalDate.of(2025, 2, 5),
                23000,
                "Finalizado",
                "Atrasado",
                null,
                null,
                loanToolsList_Tool);

        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);

        //Simulates the result mapping (list content)
        Map<String, Object> toolTypeLoans = Map.of(
                "toolName", "Destornillador Phillips 2*150mm",
                "totalLoans", 3L
        );

        Map<String, Object> toolType2Loans = Map.of(
                "toolName", "CRAFTSMAN Martillo, fibra de vidrio, 16 onzas",
                "totalLoans", 1L
        );

        //Create the mostLoanedTools list
        List<Map<String, Object>> results = List.of(toolTypeLoans);

        given(loanService.getMostLoanedTools(startDate, endDate)).willReturn(results);

        mockMvc.perform(get("/api/loans/most-loaned-tools")
                        .param("start", startDate.toString())
                        .param("end", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].toolName", is("Destornillador Phillips 2*150mm")));

    }

    //Exception (more than one tool is the most loaned)
    @Test
    public void getMostLoanedTools_MoreThanOneTool_ShouldReturnMostLoanedToolsList() throws Exception {
        ToolTypeEntity toolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        ToolTypeEntity toolType2 = new ToolTypeEntity(
                19L,
                "CRAFTSMAN Martillo, fibra de vidrio, 16 onzas",
                "Martillo",
                "CMHT51398",
                10000,
                24,
                14,
                3000,
                6000);

        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType);

        ToolItemEntity toolItem2 = new ToolItemEntity(
                4L,
                "452103684",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType2);

        LoanXToolItemEntity loanTools = new LoanXToolItemEntity(
                2L,
                null,
                toolItem);

        LoanXToolItemEntity loanTools2 = new LoanXToolItemEntity(
                3L,
                null,
                toolItem2);

        LoanXToolItemEntity loanTools3 = new LoanXToolItemEntity(
                2L,
                null,
                toolItem);

        LoanXToolItemEntity loanTools4 = new LoanXToolItemEntity(
                3L,
                null,
                toolItem);

        List<LoanXToolItemEntity> loanToolsList_Tool = Arrays.asList(loanTools, loanTools4);
        List<LoanXToolItemEntity> loanToolsList_Tool2 = Arrays.asList(loanTools2, loanTools3);

        LoanEntity loan1 = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                15000,
                "Activo",
                "Vigente",
                null,
                null,
                loanToolsList_Tool);

        LoanEntity loan2 = new LoanEntity(
                52L,
                LocalDate.of(2024, 3, 17),
                LocalDate.of(2024, 3, 31),
                23000,
                "Finalizado",
                "Vigente",
                null,
                null,
                loanToolsList_Tool2);

        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);

        //Simulates the result mapping (list content)
        Map<String, Object> toolTypeLoans = Map.of(
                "toolName", "Destornillador Phillips 2*150mm",
                "totalLoans", 3L
        );

        Map<String, Object> toolType2Loans = Map.of(
                "toolName", "CRAFTSMAN Martillo, fibra de vidrio, 16 onzas",
                "totalLoans", 1L
        );

        //Create the mostLoanedTools list
        List<Map<String, Object>> results = Arrays.asList(toolTypeLoans, toolType2Loans);

        given(loanService.getMostLoanedTools(startDate, endDate)).willReturn(results);

        mockMvc.perform(get("/api/loans/most-loaned-tools")
                        .param("start", startDate.toString())
                        .param("end", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].toolName", is("Destornillador Phillips 2*150mm")))
                .andExpect(jsonPath("$[1].toolName", is("CRAFTSMAN Martillo, fibra de vidrio, 16 onzas")));

    }

    //getCurrentLateFee() tests
    //Normal flow case (success)
    @Test
    public void getCurrentLateFee_ShouldReturnUpdated() throws Exception {
        //It's simulated that the service returns 5000
        when(sysConfigService.getLateReturnFee()).thenReturn(5000);

        mockMvc.perform(get("/api/loans/configuration/late-fee")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("5000"));

        //It's verified that the service was called
        verify(sysConfigService, times(1)).getLateReturnFee();
    }

    //This method doesn't have exceptions since is a call to something that always exists

    //returnLoan() tests
    //Normal flow case (success)
    @Test
    public void returnLoan_ShouldReturnReturnedLoan() throws Exception {
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        LoanEntity loanToReturn = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                15000,
                "Activo",
                "Vigente",
                client,
                null,
                null);

        LoanEntity returnedLoan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                15000,
                "Finalizado",
                "Vigente",
                client,
                null,
                null);

        //Simulate that the loan exist
        when(loanService.getLoanById(loanToReturn.getId())).thenReturn(Optional.of(loanToReturn));

        //Simulate that the client exist
        when(clientService.exists(anyString())).thenReturn(true);

        given(loanService.returnLoan(anyLong(), Mockito.any(LoanEntity.class))).willReturn(returnedLoan);

        String loanJson = objectMapper.writeValueAsString(loanToReturn);

        mockMvc.perform(put("/api/loans/return/{id}", loanToReturn.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(13)))
                .andExpect(jsonPath("$.status", is("Finalizado")));
    }

    //Exception (the loan or the client doesn't exist, or it's already returned)
    @Test
    public void returnLoan_LoanDoesntExist_ShouldReturnNotFound() throws Exception {
        LoanEntity nonExistingLoan = new LoanEntity(
                215L,
                LocalDate.of(2025, 7, 17),
                LocalDate.of(2025, 7, 29),
                15000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        given(loanService.getLoanById(nonExistingLoan.getId())).willReturn(Optional.empty());

        //Simulate that the client exist
        when(clientService.exists(anyString())).thenReturn(true);

        String loanJson = objectMapper.writeValueAsString(nonExistingLoan);

        mockMvc.perform(put("/api/loans/return/{id}", nonExistingLoan.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("El prestamo no existe en la base de datos"));
    }

    @Test
    public void returnLoan_ClientDoesntExist_ShouldReturnNotFound() throws Exception {
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        LoanEntity loanToReturn = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                15000,
                "Activo",
                "Vigente",
                client,
                null,
                null);

        LoanEntity returnedLoan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                15000,
                "Finalizado",
                "Vigente",
                client,
                null,
                null);

        //Simulate that the loan exist
        when(loanService.getLoanById(loanToReturn.getId())).thenReturn(Optional.of(loanToReturn));

        //Simulate that the client doesn't exist
        when(clientService.exists(anyString())).thenReturn(false);

        given(loanService.returnLoan(anyLong(), Mockito.any(LoanEntity.class))).willReturn(returnedLoan);

        String loanJson = objectMapper.writeValueAsString(loanToReturn);

        mockMvc.perform(put("/api/loans/return/{id}", loanToReturn.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cliente no encontrado en la base de datos"));
    }

    @Test
    public void returnLoan_LoanAlreadyReturned_ShouldReturnConflict() throws Exception {
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        LoanEntity loanToReturn = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                15000,
                "Activo",
                "Vigente",
                client,
                null,
                null);

        LoanEntity alreadyReturnedLoan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                15000,
                "Finalizado",
                "Vigente",
                client,
                null,
                null);

        //Simulates the search and returns the same loan, but with "Finalizado" status
        given(loanService.getLoanById(loanToReturn.getId())).willReturn(Optional.of(alreadyReturnedLoan));

        //Simulate that the client exist
        when(clientService.exists(anyString())).thenReturn(true);

        String loanJson = objectMapper.writeValueAsString(loanToReturn);

        mockMvc.perform(put("/api/loans/return/{id}", loanToReturn.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("El prestamo ya está finalizado"));
    }

    //updateLateReturnFee() tests
    //Normal flow case (success)
    @Test
    public void updateLateReturnFee_ShouldReturnUpdatedLoan() throws Exception {
        LoanEntity updatedLoan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                15000,
                "Finalizado",
                "Vigente",
                null,
                null,
                null);

        when(loanService.getLoanById(updatedLoan.getId())).thenReturn(Optional.of(updatedLoan));

        given(loanService.updateLateReturnFee(eq(updatedLoan.getId()), Mockito.any(LoanEntity.class))).willReturn(updatedLoan);

        String loanJson = objectMapper.writeValueAsString(updatedLoan);

        mockMvc.perform(put("/api/loans/loan/{id}", 13L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(13)));
    }

    //Exception (Empty cases)
    @Test
    public void updateLateReturnFee_IdIsMissing_ShouldReturnBadRequest() throws Exception {
        LoanEntity updatedLoan = new LoanEntity(
                null,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                15000,
                "Finalizado",
                "Vigente",
                null,
                null,
                null);

        when(loanService.getLoanById(updatedLoan.getId())).thenReturn(Optional.of(updatedLoan));

        given(loanService.updateLateReturnFee(eq(updatedLoan.getId()), Mockito.any(LoanEntity.class))).willReturn(updatedLoan);

        String loanJson = objectMapper.writeValueAsString(updatedLoan);

        mockMvc.perform(put("/api/loans/loan/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El id no puede estar vacio o ser nulo"));
    }

    @Test
    public void updateLateReturnFee_LoanDoesntExist_ShouldReturnNotFound() throws Exception {
        LoanEntity updatedLoan = new LoanEntity(
                16L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                15000,
                "Finalizado",
                "Vigente",
                null,
                null,
                null);

        when(loanService.getLoanById(updatedLoan.getId())).thenReturn(Optional.empty());

        String loanJson = objectMapper.writeValueAsString(updatedLoan);

        mockMvc.perform(put("/api/loans/loan/{id}", 16L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("El prestamo no existe en la base de datos"));
    }

    //updateGlobalLateFee() tests
    //Normal flow case (success)
    @Test
    public void updateGlobalLateFee_ShouldUpdateAndReturnOk() throws Exception {
        Integer newFee = 3500;

        when(validationService.isValidNumber(newFee)).thenReturn(true);

        mockMvc.perform(put("/api/loans/configuration/late-fee")
                        .param("amount", String.valueOf(newFee))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Tarifa actualizada para futuros préstamos"));

        //It's verified that the service was called
        verify(sysConfigService, times(1)).updateLateReturnFee(newFee);
    }

    //Exceptions (Bad request)
    @Test
    public void updateGlobalLateFee_NegativeAmount_ShouldReturnBadRequest() throws Exception {
        Integer invalidFee = -100;

        when(validationService.isValidNumber(invalidFee)).thenReturn(false);

        mockMvc.perform(put("/api/loans/configuration/late-fee")
                        .param("amount", String.valueOf(invalidFee))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El monto no puede ser negativo"));

        //It's verified that the service was never called
        verify(sysConfigService, never()).updateLateReturnFee(anyInt());
    }

    //updateValidity() tests
    //Normal flow case (success)
    @Test
    public void updateValidity_ShouldCallServiceAndReturnNoContent() throws Exception {
        LoanEntity loanToUpdate = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                15000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        //when is not used because the return of the service is void

        String loanJson = objectMapper.writeValueAsString(loanToUpdate);

        mockMvc.perform(put("/api/loans/validity/{id}", loanToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loanJson))
                .andExpect(status().isNoContent());

        //It is verified that updateValidity in loanService was called only once with ANY LoanEntity object
        verify(loanService, times(1)).updateValidity(eq(loanToUpdate.getId()), Mockito.any(LoanEntity.class));
    }

}
