package com.proyect.toolrent.Services;

import com.proyect.toolrent.Entities.*;
import com.proyect.toolrent.Enums.ToolDamageLevel;
import com.proyect.toolrent.Enums.ToolStatus;
import com.proyect.toolrent.Repositories.LoanRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LoanServiceTest {
    LoanRepository loanRepository = mock(LoanRepository.class);
    ClientService clientService = mock(ClientService.class);
    LoanXToolItemService loanXToolItemService = mock(LoanXToolItemService.class);
    ToolTypeService toolTypeService = mock(ToolTypeService.class);
    ToolItemService toolItemService = mock(ToolItemService.class);
    KardexService kardexService = mock(KardexService.class);
    ValidationService validationService = mock(ValidationService.class);
    SystemConfigurationService sysConfigService = mock(SystemConfigurationService.class);
    EmployeeService employeeService = mock(EmployeeService.class);


    LoanService loanService = new LoanService(loanRepository, clientService, loanXToolItemService,
            toolTypeService, toolItemService, kardexService,
            validationService, sysConfigService, employeeService);

    //getAllLoans() tests
    //Normal flow case (success)
    @Test
    void whenGetAllLoans_thenReturnsList() {
        // Given
        LoanEntity loan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                10000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        LoanEntity loan2 = new LoanEntity(
                24L,
                LocalDate.of(2024, 7, 21),
                LocalTime.of(15, 25, 48),
                LocalDate.of(2024, 8, 21),
                LocalTime.of(15, 25, 48),
                20000,
                "Finalizado",
                "Vigente",
                null,
                null,
                null);

        when(loanRepository.findAllWithDetails()).thenReturn(List.of(loan, loan2));

        // When
        List<LoanEntity> result = loanService.getAllLoans();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(13L);
        assertThat(result.get(1).getId()).isEqualTo(24L);
    }

    //Exception (empty list)
    @Test
    void whenGetAllLoansEmpty_thenReturnsEmptyList() {
        // Given
        when(loanRepository.findAllWithDetails()).thenReturn(Collections.emptyList());

        // When
        List<LoanEntity> result = loanService.getAllLoans();

        // Then
        assertThat(result).isEmpty();
    }

    //getLoanById() tests
    //Normal flow case (success)
    @Test
    void whenGetLoanById_thenReturnsLoan() {
        // Given
        LoanEntity loan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                10000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        when(loanRepository.findByIdWithDetails(loan.getId())).thenReturn(Optional.of(loan));

        // When
        Optional<LoanEntity> result = loanService.getLoanById(loan.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(loan.getId());
    }

    //Exception (loan not found)
    @Test
    void whenGetLoanByIdNotFound_thenReturnsEmpty() {
        // Given
        Long id = 999L;
        when(loanRepository.findByIdWithDetails(id)).thenReturn(Optional.empty());

        // When
        Optional<LoanEntity> result = loanService.getLoanById(id);

        // Then
        assertThat(result).isEmpty();
    }

    //getActiveLoansByClient() tests
    //Normal flow case (success)
    @Test
    void whenGetActiveLoansByClient_thenReturnsList() {
        // Given
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        LoanEntity loan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                10000,
                "Activo",
                "Vigente",
                client,
                null,
                null);

        LoanEntity loan2 = new LoanEntity(
                24L,
                LocalDate.of(2024, 7, 21),
                LocalTime.of(15, 25, 48),
                LocalDate.of(2024, 8, 21),
                LocalTime.of(15, 25, 48),
                20000,
                "Finalizado",
                "Vigente",
                client,
                null,
                null);

        when(loanRepository.findActiveLoansByClient(client.getRun())).thenReturn(List.of(loan, loan2));

        // When
        List<LoanEntity> result = loanService.getActiveLoansByClient(client.getRun());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(loan.getId());
        assertThat(result.get(1).getId()).isEqualTo(loan2.getId());
    }

    //getLoanByStatus() tests
    //Normal flow case (success)
    @Test
    void whenGetLoanByStatus_thenReturnsList() {
        // Given
        LoanEntity loan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                10000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        LoanEntity loan2 = new LoanEntity(
                24L,
                LocalDate.of(2024, 7, 21),
                LocalTime.of(15, 25, 48),
                LocalDate.of(2024, 8, 21),
                LocalTime.of(15, 25, 48),
                20000,
                "Activo",
                "Vigente",
                null,
                null,
                null);
        when(loanRepository.findByStatusWithDetails("Activo")).thenReturn(List.of(loan, loan2));

        // When
        List<LoanEntity> result = loanService.getLoanByStatus("Activo");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(loan.getId());
        assertThat(result.get(1).getId()).isEqualTo(loan2.getId());
    }

    //getLoanByValidity() tests
    //Normal flow case (success)
    @Test
    void whenGetLoanByValidity_thenReturnsList() {
        // Given
        LoanEntity loan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                10000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        LoanEntity loan2 = new LoanEntity(
                24L,
                LocalDate.of(2024, 7, 21),
                LocalTime.of(15, 25, 48),
                LocalDate.of(2024, 8, 21),
                LocalTime.of(15, 25, 48),
                20000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        when(loanRepository.findByValidity("Vigente")).thenReturn(List.of(loan, loan2));

        // When
        List<LoanEntity> result = loanService.getLoanByValidity("Vigente");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(loan.getId());
        assertThat(result.get(1).getId()).isEqualTo(loan2.getId());
    }

    //getMostLoanedTools() tests
    //Normal flow case (success)
    @Test
    void whenGetMostLoanedTools_thenReturnsCorrectList() {
        // Given
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();

        ToolTypeEntity type = new ToolTypeEntity();
        type.setName("Taladro");
        ToolItemEntity item = new ToolItemEntity();
        item.setToolType(type);

        //It's simulated the native query response: Object[] { ToolItemEntity, Long (count) }
        Object[] row = new Object[]{item, 5L};
        List<Object[]> queryResult = new ArrayList<>();
        queryResult.add(row);

        when(loanXToolItemService.getMostLoanedToolsBetweenDates(start, end)).thenReturn(queryResult);

        // When
        List<Map<String, Object>> result = loanService.getMostLoanedTools(start, end);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("toolName")).isEqualTo("Taladro");
        assertThat(result.get(0).get("totalLoans")).isEqualTo(5L);
    }

    //Exception (two tools are the most loaned)
    @Test
    void whenGetMostLoanedToolsTie_thenReturnsBoth() {
        // Given
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);

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

        //It's simulated a draw
        Object[] row1 = new Object[]{toolItem, 5L};
        Object[] row2 = new Object[]{toolItem2, 5L};
        List<Object[]> queryResult = List.of(row1, row2);

        when(loanXToolItemService.getMostLoanedToolsBetweenDates(start, end)).thenReturn(queryResult);

        // When
        List<Map<String, Object>> result = loanService.getMostLoanedTools(start, end);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).get("totalLoans")).isEqualTo(5L);
    }

    //exists() tests
    //Normal flow case (success)
    @Test
    void whenExists_thenReturnsTrue() {
        LoanEntity loan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                10000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        when(loanRepository.existsById(loan.getId())).thenReturn(true);

        boolean exists = loanService.exists(loan.getId());

        assertThat(exists).isTrue();
    }

    //Exception (loan doesn't exist)
    @Test
    void whenNotExists_thenReturnsFalse() {
        // Given
        Long id = 999L;
        when(loanRepository.existsById(id)).thenReturn(false);

        // When
        boolean exists = loanService.exists(id);

        // Then
        assertThat(exists).isFalse();
    }

    //createLoan() tests
    //Normal flow case (success)
    @Test
    void whenCreateLoanSuccess_thenSavesLoanAndUpdatesStock() {
        // Given
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
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                10000,
                "Activo",
                "Vigente",
                client,
                employee,
                loanToolsList);

        // Mocks
        when(employeeService.getEmployeeByRun(employee.getRun())).thenReturn(Optional.of(employee));
        when(clientService.getClientByRun(client.getRun())).thenReturn(Optional.of(client));
        when(loanRepository.findOverdueLoans(eq(client.getRun()), any())).thenReturn(Collections.emptyList());
        when(loanXToolItemService.getActiveLoansToolTypeIdsByClient(client.getRun())).thenReturn(Collections.emptySet());
        when(toolItemService.getToolItemById(toolItem.getId())).thenReturn(Optional.of(toolItem));
        when(toolItemService.getToolItemById(toolItem2.getId())).thenReturn(Optional.of(toolItem2));
        when(sysConfigService.getLateReturnFee()).thenReturn(1000);
        when(loanRepository.save(any(LoanEntity.class))).thenAnswer(i -> i.getArgument(0));

        // When
        LoanEntity result = loanService.createLoan(newLoan, employee.getRun());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getClient()).isEqualTo(client);
        assertThat(result.getEmployee()).isEqualTo(employee);

        //It's verified that the stock and the status was updated
        verify(toolItemService).updateToolItem(eq(toolItem.getId()), argThat(t -> t.getStatus() == ToolStatus.PRESTADA));
        verify(toolTypeService).decreaseAvailableStock(toolType, 1);
        verify(kardexService).createLoanKardex(toolType);

        assertThat(client.getActiveLoans()).isEqualTo(1);
        verify(clientService).saveClient(client);
    }

    //Exceptions (bad requests and restrictions)
    @Test
    void whenCreateLoanClientRestricted_thenThrowsException() {
        // Given
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Restringido",
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
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                10000,
                "Activo",
                "Vigente",
                client,
                employee,
                loanToolsList);

        when(employeeService.getEmployeeByRun(anyString())).thenReturn(Optional.of(new EmployeeEntity()));
        when(clientService.getClientByRun(client.getRun())).thenReturn(Optional.of(client));

        // When / Then
        assertThatThrownBy(() -> loanService.createLoan(newLoan, employee.getRun()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("El cliente no puede arrendar debido a deudas impagas");
    }

    @Test
    void whenCreateLoanClientDebt_thenThrowsException() {
        // Given
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                15000,
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
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                10000,
                "Activo",
                "Vigente",
                client,
                employee,
                loanToolsList);

        when(employeeService.getEmployeeByRun(anyString())).thenReturn(Optional.of(new EmployeeEntity()));
        when(clientService.getClientByRun(client.getRun())).thenReturn(Optional.of(client));

        // When / Then
        assertThatThrownBy(() -> loanService.createLoan(newLoan, employee.getRun()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("El cliente no puede arrendar debido a deudas impagas");
    }

    @Test
    void whenCreateLoanClientMaxLoans_thenThrowsException() {
        // Given
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                5);

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
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                10000,
                "Activo",
                "Vigente",
                client,
                employee,
                loanToolsList);

        when(employeeService.getEmployeeByRun(anyString())).thenReturn(Optional.of(new EmployeeEntity()));
        when(clientService.getClientByRun(client.getRun())).thenReturn(Optional.of(client));

        // When / Then
        assertThatThrownBy(() -> loanService.createLoan(newLoan, employee.getRun()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("El cliente no puede tener más de 5 arriendos activos");
    }

    @Test
    void whenCreateLoanToolUnavailable_thenThrowsException() {
        // Given
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
                ToolStatus.PRESTADA,
                ToolDamageLevel.NO_DANADA,
                toolType);

        ToolItemEntity toolItem2 = new ToolItemEntity(
                4L,
                "452103684",
                ToolStatus.PRESTADA,
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
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                10000,
                "Activo",
                "Vigente",
                client,
                employee,
                loanToolsList);

        when(employeeService.getEmployeeByRun(anyString())).thenReturn(Optional.of(new EmployeeEntity()));
        when(clientService.getClientByRun(client.getRun())).thenReturn(Optional.of(client));
        when(loanRepository.findOverdueLoans(anyString(), any())).thenReturn(Collections.emptyList());
        when(loanXToolItemService.getActiveLoansToolTypeIdsByClient(anyString())).thenReturn(Collections.emptySet());
        when(toolItemService.getToolItemById(toolItem.getId())).thenReturn(Optional.of(toolItem));
        when(toolItemService.getToolItemById(toolItem2.getId())).thenReturn(Optional.of(toolItem2));

        // When / Then
        assertThatThrownBy(() -> loanService.createLoan(newLoan, employee.getRun()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no está disponible");
    }

    @Test
    void whenCreateLoanToolsListEmpty_thenThrowsException() {
        // Given
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
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                10000,
                "Activo",
                "Vigente",
                client,
                employee,
                null);

        when(employeeService.getEmployeeByRun(anyString())).thenReturn(Optional.of(new EmployeeEntity()));
        when(clientService.getClientByRun(any())).thenReturn(Optional.of(new ClientEntity()));

        // When / Then
        assertThatThrownBy(() -> loanService.createLoan(newLoan, employee.getRun()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La lista de herramientas no puede estar vacía.");
    }

    @Test
    void whenCreateLoanFeeNull_thenUsesSystemDefault() {
        // Given
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
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                null,
                "Activo",
                "Vigente",
                null,
                employee,
                null);


        when(employeeService.getEmployeeByRun(any())).thenReturn(Optional.of(new EmployeeEntity()));
        when(clientService.getClientByRun(any())).thenReturn(Optional.of(new ClientEntity()));
    }

    //returnLoan() tests
    //Normal flow case (success)
    @Test
    void whenReturnLoanSuccessNoDamage_thenUpdatesStockAndKardex() {
        // Given
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                1);

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

        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType);

        LoanXToolItemEntity loanTools = new LoanXToolItemEntity(
                2L,
                null,
                toolItem);

        List<LoanXToolItemEntity> loanToolsList = List.of(loanTools);

        LoanEntity loan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalTime.of(15, 25, 48),
                LocalDate.of(2026, 5, 24),
                LocalTime.of(15, 25, 48),
                10000,
                "Activo",
                "Vigente",
                client,
                employee,
                loanToolsList);

        // Mocks
        when(loanRepository.findByIdWithDetails(loan.getId())).thenReturn(Optional.of(loan));
        when(clientService.getClientByRun(client.getRun())).thenReturn(Optional.of(client));
        when(loanXToolItemService.getLoanToolItemsWithToolTypesByLoan_Id(loan.getId())).thenReturn(loanToolsList);
        when(loanRepository.save(any(LoanEntity.class))).thenAnswer(i -> i.getArgument(0));

        // When
        LoanEntity result = loanService.returnLoan(loan.getId(), loan);

        // Then
        //It's verified that the tool item is available and his stock raised
        verify(toolItemService).updateToolItem(eq(toolItem.getId()), argThat(t -> t.getStatus() == ToolStatus.DISPONIBLE));
        verify(toolTypeService).increaseAvailableStock(toolType, 1);
        verify(kardexService).createReturnKardex(toolType);

        //Client verification
        assertThat(client.getActiveLoans()).isEqualTo(0);
        verify(clientService).saveClient(client);

        assertThat(result.getStatus()).isEqualTo("Finalizado");
        assertThat(result.getValidity()).isEqualTo("Puntual");
    }

    //Exceptions (late and damaged cases)
    @Test
    void whenReturnLoanLate_thenChargesFeeAndValidityAtrasado() {
        // Given
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                1);

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

        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType);

        LoanXToolItemEntity loanTools = new LoanXToolItemEntity(
                2L,
                null,
                toolItem);

        List<LoanXToolItemEntity> loanToolsList = List.of(loanTools);

        LoanEntity loan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                1000,
                "Activo",
                "Vigente",
                client,
                employee,
                loanToolsList);

        when(loanRepository.findByIdWithDetails(loan.getId())).thenReturn(Optional.of(loan));
        when(clientService.getClientByRun(client.getRun())).thenReturn(Optional.of(client));
        when(loanXToolItemService.getLoanToolItemsWithToolTypesByLoan_Id(loan.getId())).thenReturn(loanToolsList);
        when(loanRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        LoanEntity result = loanService.returnLoan(loan.getId(), loan);

        // Then
        // 2 days * 1000 = 2000 fee
        verify(clientService).chargeClientLateReturnFee(eq(client), anyInt());
        assertThat(result.getValidity()).isEqualTo("Atrasado");
    }

    @Test
    void whenReturnLoanWithDamage_thenStatusEnReparacionAndRestrictedClient() {
        // Given
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                1);

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

        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.EN_REPARACION,
                ToolDamageLevel.EN_EVALUACION,
                toolType);

        LoanXToolItemEntity loanTools = new LoanXToolItemEntity(
                2L,
                null,
                toolItem);

        List<LoanXToolItemEntity> loanToolsList = List.of(loanTools);

        LoanEntity loan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                1000,
                "Activo",
                "Vigente",
                client,
                employee,
                loanToolsList);

        when(loanRepository.findByIdWithDetails(loan.getId())).thenReturn(Optional.of(loan));
        when(clientService.getClientByRun(client.getRun())).thenReturn(Optional.of(client));
        when(loanXToolItemService.getLoanToolItemsWithToolTypesByLoan_Id(loan.getId())).thenReturn(loanToolsList);
        when(loanRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        loanService.returnLoan(loan.getId(), loan);

        // Then
        //It's verified that the tool is EN_REPARACION
        verify(toolItemService).updateToolItem(eq(toolItem.getId()), argThat(tool -> tool.getStatus() == ToolStatus.EN_REPARACION));
        //It's verified that a baja/reparación kardex is created
        verify(kardexService).createDisableToolItemKardex(any(ToolItemEntity.class));
        //It's verified that the client is "Restringido"
        assertThat(client.getStatus()).isEqualTo("Restringido");
    }

    @Test
    void whenReturnLoanToolAlreadyDamaged_thenThrowsException() {
        // Given
        ClientEntity client = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        LoanEntity loan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                10000,
                "Activo",
                "Vigente",
                client,
                null,
                null);

        ToolItemEntity reqTool = new ToolItemEntity();
        reqTool.setId(100L);
        LoanXToolItemEntity reqLXT = new LoanXToolItemEntity(); reqLXT.setToolItem(reqTool);
        loan.setLoanTools(List.of(reqLXT));

        LoanEntity dbLoan = new LoanEntity(); dbLoan.setId(loan.getId());

        ToolItemEntity dbTool = new ToolItemEntity();
        dbTool.setId(100L);
        dbTool.setDamageLevel(ToolDamageLevel.GRAVEMENTE_DANADA);

        LoanXToolItemEntity dbLXT = new LoanXToolItemEntity(); dbLXT.setToolItem(dbTool);

        when(loanRepository.findByIdWithDetails(loan.getId())).thenReturn(Optional.of(dbLoan));
        when(clientService.getClientByRun(client.getRun())).thenReturn(Optional.of(client));
        when(loanXToolItemService.getLoanToolItemsWithToolTypesByLoan_Id(loan.getId())).thenReturn(List.of(dbLXT));

        // When / Then
        assertThatThrownBy(() -> loanService.returnLoan(loan.getId(), loan))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("La herramienta ya tiene un nivel de daño");
    }

    //updateLateReturnFee() tests
    //Normal flow case (success)
    @Test
    void whenUpdateLateReturnFee_thenUpdatesAmount() {
        // Given
        LoanEntity loanRequest = new LoanEntity(
                null,
                null,
                null,
                null,
                null,
                1000,
                null,
                null,
                null,
                null,
                null);

        LoanEntity dbLoan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                1000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        when(loanRepository.findById(dbLoan.getId())).thenReturn(Optional.of(dbLoan));
        when(validationService.isValidNumber(1000)).thenReturn(true);
        when(loanRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        LoanEntity result = loanService.updateLateReturnFee(dbLoan.getId(), loanRequest);

        // Then
        assertThat(result.getLateReturnFee()).isEqualTo(1000);
    }

    //Exception (bar request)
    @Test
    void whenUpdateLateReturnFeeInvalid_thenThrowsException() {
        // Given
        LoanEntity loanRequest = new LoanEntity(
                null,
                null,
                null,
                null,
                null,
                -1000,
                null,
                null,
                null,
                null,
                null);

        LoanEntity dbLoan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalTime.of(15, 25, 48),
                LocalDate.of(2025, 5, 24),
                LocalTime.of(15, 25, 48),
                1000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        when(loanRepository.findById(dbLoan.getId())).thenReturn(Optional.of(dbLoan));
        when(validationService.isValidNumber(-100)).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> loanService.updateLateReturnFee(dbLoan.getId(), loanRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Monto de multa por atraso incorrecta");
    }


}