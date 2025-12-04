package com.proyect.toolrent.Services;

import com.proyect.toolrent.Entities.*;
import com.proyect.toolrent.Enums.ToolDamageLevel;
import com.proyect.toolrent.Enums.ToolStatus;
import com.proyect.toolrent.Repositories.LoanXToolItemRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class LoanXToolItemServiceTest {

    LoanXToolItemRepository loanXToolItemRepository = mock(LoanXToolItemRepository.class);

    LoanXToolItemService loanXToolItemService = new LoanXToolItemService(loanXToolItemRepository);

    //getAllLoanToolItemsByLoan_Id() tests
    //Normal flow case (success)
    @Test
    void whenGetAllLoanToolItemsByLoanId_thenReturnsList() {
        // Given
        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        ToolItemEntity toolItem2 = new ToolItemEntity(
                4L,
                "452103684",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        LoanEntity loan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                10000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        LoanXToolItemEntity loanXToolItem = new LoanXToolItemEntity(
                1L,
                loan,
                toolItem);

        LoanXToolItemEntity loanXToolItem2 = new LoanXToolItemEntity(
                2L,
                loan,
                toolItem2);

        List<LoanXToolItemEntity> loanToolsList = Arrays.asList(loanXToolItem, loanXToolItem2);

        when(loanXToolItemRepository.findByLoan_Id(loan.getId())).thenReturn(loanToolsList);

        // When
        List<LoanXToolItemEntity> result = loanXToolItemService.getAllLoanToolItemsByLoan_Id(loan.getId());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
    }

    //getLoanToolItemsWithToolTypesByLoan_Id() tests
    //Normal flow case (success)
    @Test
    void whenGetLoanToolItemsWithToolTypesByLoanId_thenReturnsList() {
        // Given
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

        ToolItemEntity toolItem2 = new ToolItemEntity(
                4L,
                "452103684",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType);

        LoanEntity loan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                10000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        LoanXToolItemEntity loanXToolItem = new LoanXToolItemEntity(
                1L,
                loan,
                toolItem);

        LoanXToolItemEntity loanXToolItem2 = new LoanXToolItemEntity(
                2L,
                loan,
                toolItem2);

        List<LoanXToolItemEntity> loanToolsList = Arrays.asList(loanXToolItem, loanXToolItem2);

        when(loanXToolItemRepository.findByLoanIdWithDetails(loan.getId())).thenReturn(loanToolsList);

        // When
        List<LoanXToolItemEntity> result = loanXToolItemService.getLoanToolItemsWithToolTypesByLoan_Id(loan.getId());

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
    }


    //getActiveLoansToolTypeIdsByClient() tests
    //Normal flow case (success)
    @Test
    void whenGetActiveLoansToolTypeIdsByClient_thenReturnsSetOfIds() {
        // Given
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

        ClientEntity client = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                0,
                0);
        Set<Long> ids = Set.of(toolType.getId(), toolType2.getId());
        when(loanXToolItemRepository.findActiveLoansToolTypeIdsByClient(client.getRun())).thenReturn(ids);

        // When
        Set<Long> result = loanXToolItemService.getActiveLoansToolTypeIdsByClient(client.getRun());

        // Then
        assertThat(result).containsExactlyInAnyOrder(1L, 19L);
    }

    //getHistoryByToolId() tests
    //Normal flow case (success)
    @Test
    void whenGetHistoryByToolId_thenReturnsList() {
        // Given
        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        LoanEntity loan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                10000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        LoanXToolItemEntity loanXToolItem = new LoanXToolItemEntity(
                1L,
                loan,
                toolItem);

        LoanXToolItemEntity loanXToolItem2 = new LoanXToolItemEntity(
                2L,
                loan,
                toolItem);

        LoanXToolItemEntity item = new LoanXToolItemEntity();
        when(loanXToolItemRepository.findHistoryByToolId(toolItem.getId())).thenReturn(List.of(loanXToolItem, loanXToolItem2));

        // When
        List<LoanXToolItemEntity> result = loanXToolItemService.getHistoryByToolId(toolItem.getId());

        // Then
        assertThat(result).hasSize(2);
    }

    //getMostLoanedToolsBetweenDates() tests
    //Normal flow case (success)
    @Test
    void whenGetMostLoanedToolsBetweenDates_thenReturnsObjectList() {
        // Given
        LocalDate start = LocalDate.of(2025, 5, 1);
        LocalDate end = LocalDate.of(2025, 5, 30);

        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        Object[] row = new Object[]{toolItem, 1L};
        List<Object[]> dbResult = new ArrayList<>();
        dbResult.add(row);

        when(loanXToolItemRepository.findMostLoanedToolsBetweenDates(start, end)).thenReturn(dbResult);

        // When
        List<Object[]> result = loanXToolItemService.getMostLoanedToolsBetweenDates(start, end);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)[1]).isEqualTo(1L);
    }

    //save() tests
    //Normal flow case (success)
    @Test
    void whenSave_thenReturnsSavedEntity() {
        // Given
        LoanXToolItemEntity loanXToolItem = new LoanXToolItemEntity(
                1L,
                null,
                null);
        when(loanXToolItemRepository.save(loanXToolItem)).thenReturn(loanXToolItem);

        // When
        LoanXToolItemEntity result = loanXToolItemService.save(loanXToolItem);

        // Then
        assertThat(result).isNotNull();
        verify(loanXToolItemRepository).save(loanXToolItem);
    }

    //getAvailableStock() tests
    //Normal flow case (success)
    @Test
    void whenGetAvailableStockValidTool_thenReturnsStock() {
        // Given
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

        // When
        Integer availableStock = loanXToolItemService.getAvailableStock(toolItem);

        // Then
        assertThat(availableStock).isEqualTo(7);
    }

    //Exceptions (null stock and type)
    @Test
    void whenGetAvailableStockNullTool_thenReturnsNull() {
        // When
        Integer stock = loanXToolItemService.getAvailableStock(null);

        // Then
        assertThat(stock).isNull();
    }

    @Test
    void whenGetAvailableStockToolWithoutType_thenReturnsNull() {
        // Given
        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        // When
        Integer stock = loanXToolItemService.getAvailableStock(toolItem);

        // Then
        assertThat(stock).isNull();
    }

    //existsByLoanId() tests
    //Normal flow case (success)
    @Test
    void whenExistsByLoanId_thenReturnsTrue() {
        // Given
        LoanEntity loan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                10000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        when(loanXToolItemRepository.existsByLoan_Id(loan.getId())).thenReturn(true);

        // When
        boolean exists = loanXToolItemService.existsByLoanId(loan.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void whenNotExistsByLoanId_thenReturnsFalse() {
        // Given
        Long loanId = 999L;
        when(loanXToolItemRepository.existsByLoan_Id(loanId)).thenReturn(false);

        // When
        boolean exists = loanXToolItemService.existsByLoanId(loanId);

        // Then
        assertThat(exists).isFalse();
    }
}
