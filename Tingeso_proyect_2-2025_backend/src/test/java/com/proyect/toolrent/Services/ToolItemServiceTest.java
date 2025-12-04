package com.proyect.toolrent.Services;

import com.proyect.toolrent.Entities.*;
import com.proyect.toolrent.Enums.ToolDamageLevel;
import com.proyect.toolrent.Enums.ToolStatus;
import com.proyect.toolrent.Repositories.ToolItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ToolItemServiceTest {

    ToolItemRepository toolItemRepository = mock(ToolItemRepository.class);
    ToolTypeService toolTypeService = mock(ToolTypeService.class);
    ClientService clientService = mock(ClientService.class);
    ValidationService validationService = mock(ValidationService.class);
    KardexService kardexService = mock(KardexService.class);
    LoanXToolItemService loanXToolItemService = mock(LoanXToolItemService.class);

    ToolItemService toolItemService = new ToolItemService(
            toolItemRepository, toolTypeService, clientService,
            validationService, kardexService, loanXToolItemService
    );

    //createToolItem() tests
    //Normal flow case (success)
    @Test
    void whenCreateToolItemGoodCondition_thenIncreasesBothStocks() {
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

        ToolItemEntity newToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType);

        when(toolTypeService.increaseBothStocks(toolType, 1)).thenReturn(toolType);
        when(toolItemRepository.save(newToolItem)).thenReturn(newToolItem);

        // When
        ToolItemEntity result = toolItemService.createToolItem(newToolItem);

        // Then
        assertThat(result).isNotNull();
        verify(toolTypeService).increaseBothStocks(toolType, 1);
        verify(kardexService).createRegisterToolItemKardex(newToolItem);
    }

    //Exception (tool item created in bad condition)
    @Test
    void whenCreateToolItemBadCondition_thenIncreasesTotalStockOnly() {
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

        ToolItemEntity newToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.EN_REPARACION,
                ToolDamageLevel.DANADA,
                toolType);

        when(toolTypeService.increaseTotalStock(toolType, 1)).thenReturn(toolType);
        when(toolItemRepository.save(newToolItem)).thenReturn(newToolItem);

        // When
        toolItemService.createToolItem(newToolItem);

        // Then
        verify(toolTypeService).increaseTotalStock(toolType, 1); // Solo stock total
        verify(toolTypeService, never()).increaseBothStocks(any(), anyInt());
        verify(kardexService).createRegisterToolItemKardex(newToolItem);
    }

    //getAllToolItems() tests
    //Normal flow case (success)
    @Test
    void whenGetAllToolItems_thenReturnsSortedList() {
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

        when(toolItemRepository.findAll(any(Sort.class))).thenReturn(List.of(toolItem, toolItem2));

        // When
        List<ToolItemEntity> result = toolItemService.getAllToolItems();

        // Then
        assertThat(result).hasSize(2);
        verify(toolItemRepository).findAll(any(Sort.class));
    }

    //getToolItemById() tests
    //Normal flow case (success)
    @Test
    void whenGetToolItemById_thenReturnsOptional() {
        // Given
        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        when(toolItemRepository.findById(toolItem.getId())).thenReturn(Optional.of(toolItem));

        // When
        Optional<ToolItemEntity> result = toolItemService.getToolItemById(toolItem.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(toolItem.getId());
    }

    //getToolItemBySerialNumber() tests
    //Normal flow case (success)
    @Test
    void whenGetToolItemBySerialNumber_thenReturnsOptional() {
        // Given
        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        when(toolItemRepository.findBySerialNumber(toolItem.getSerialNumber())).thenReturn(Optional.of(toolItem));

        // When
        Optional<ToolItemEntity> result = toolItemService.getToolItemBySerialNumber(toolItem.getSerialNumber());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getSerialNumber()).isEqualTo(toolItem.getSerialNumber());
    }

    //getFirstAvailableByType() tests
    //Normal flow case (success)
    @Test
    void whenGetFirstAvailableByType_thenReturnsItem() {
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

        when(toolItemRepository.findFirstByToolType_IdAndStatusAndDamageLevelIn(eq(toolType.getId()), eq(ToolStatus.DISPONIBLE), anyList()))
                .thenReturn(Optional.of(toolItem));

        // When
        ToolItemEntity result = toolItemService.getFirstAvailableByType(toolType.getId());

        // Then
        assertThat(result).isNotNull();
    }

    //Exception (no tool type)
    @Test
    void whenGetFirstAvailableByTypeNone_thenThrowsException() {
        // Given
        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        when(toolItemRepository.findFirstByToolType_IdAndStatusAndDamageLevelIn(any(), any(), any()))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> toolItemService.getFirstAvailableByType(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No hay unidades disponibles de este tipo de herramienta.");
    }

    //existsBy() tests
    //Normal flow case (success)
    @Test
    void whenExists_thenReturnsTrue() {
        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        when(toolItemRepository.existsById(toolItem.getId())).thenReturn(true);
        assertThat(toolItemService.exists(toolItem.getId())).isTrue();
    }

    @Test
    void whenExistsBySerialNumber_thenReturnsTrue() {
        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        when(toolItemRepository.existsBySerialNumber(toolItem.getSerialNumber())).thenReturn(true);
        assertThat(toolItemService.existsBySerialNumber(toolItem.getSerialNumber())).isTrue();
    }

    //updateToolItem() tests
    //Normal flow case (success)
    @Test
    void whenUpdateToolItemSuccess_thenUpdatesFields() {
        // Given
        ToolItemEntity existingToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        ToolItemEntity updatedToolItem = new ToolItemEntity(
                null,
                null,
                ToolStatus.EN_REPARACION,
                ToolDamageLevel.DANADA,
                null);

        when(toolItemRepository.findById(existingToolItem.getId())).thenReturn(Optional.of(existingToolItem));
        when(validationService.isValidToolStatus(anyString())).thenReturn(true);
        when(validationService.isValidDamageLevel(anyString())).thenReturn(true);
        when(toolItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        ToolItemEntity result = toolItemService.updateToolItem(existingToolItem.getId(), updatedToolItem);

        // Then
        assertThat(result.getStatus()).isEqualTo(ToolStatus.EN_REPARACION);
        assertThat(result.getDamageLevel()).isEqualTo(ToolDamageLevel.DANADA);
    }

    //Exceptions (bad requests)
    @Test
    void whenUpdateToolItemInvalidStatus_thenThrowsException() {
        // Given
        ToolItemEntity existingToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        ToolItemEntity updatedToolItem = new ToolItemEntity(
                null,
                null,
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.DANADA,
                null);

        when(toolItemRepository.findById(existingToolItem.getId())).thenReturn(Optional.of(existingToolItem));
        when(validationService.isValidToolStatus(anyString())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> toolItemService.updateToolItem(existingToolItem.getId(), updatedToolItem))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Estado de la herramienta inválido");
    }

    @Test
    void whenUpdateToolItemInvalidDamage_thenThrowsException() {
        // Given
        ToolItemEntity existingToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        ToolItemEntity updatedToolItem = new ToolItemEntity(
                null,
                null,
                ToolStatus.EN_REPARACION,
                ToolDamageLevel.DESUSO,
                null);

        when(toolItemRepository.findById(existingToolItem.getId())).thenReturn(Optional.of(existingToolItem));
        when(validationService.isValidToolStatus(anyString())).thenReturn(true);
        when(validationService.isValidDamageLevel(anyString())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> toolItemService.updateToolItem(existingToolItem.getId(), updatedToolItem))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nivel de daño de la herramienta inválido");
    }

    //disableToolItem() tests
    //Normal flow case (success)
    @Test
    void whenDisableToolItemIrreparable_thenDecreasesTotalStock() {
        // Given
        ToolItemEntity dbToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        ToolItemEntity requestToolItem = new ToolItemEntity(
                null,
                null,
                ToolStatus.DADA_DE_BAJA,
                ToolDamageLevel.IRREPARABLE,
                null);

        when(toolItemRepository.findById(dbToolItem.getId())).thenReturn(Optional.of(dbToolItem));
        when(toolTypeService.decreaseAvailableStock(any(), anyInt())).thenReturn(new ToolTypeEntity());
        when(toolTypeService.decreaseTotalStock(any(), anyInt())).thenReturn(new ToolTypeEntity());
        when(toolItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        ToolItemEntity result = toolItemService.disableToolItem(dbToolItem.getId(), requestToolItem);

        // Then
        assertThat(result.getStatus()).isEqualTo(ToolStatus.DADA_DE_BAJA);
        assertThat(result.getDamageLevel()).isEqualTo(ToolDamageLevel.IRREPARABLE);

        verify(toolTypeService).decreaseTotalStock(any(), eq(1));
        verify(kardexService).createDisableToolItemKardex(dbToolItem);
    }

    @Test
    void whenDisableToolItemReparable_thenDecreasesAvailableStockOnly() {
        // Given
        ToolItemEntity dbToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        ToolItemEntity requestToolItem = new ToolItemEntity(
                null,
                null,
                ToolStatus.EN_REPARACION,
                ToolDamageLevel.GRAVEMENTE_DANADA,
                null);

        when(toolItemRepository.findById(dbToolItem.getId())).thenReturn(Optional.of(dbToolItem));
        when(toolTypeService.decreaseAvailableStock(any(), anyInt())).thenReturn(new ToolTypeEntity());
        when(toolItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        ToolItemEntity result = toolItemService.disableToolItem(dbToolItem.getId(), requestToolItem);

        // Then
        assertThat(result.getStatus()).isEqualTo(ToolStatus.EN_REPARACION);

        verify(toolTypeService).decreaseAvailableStock(any(), eq(1));
        verify(toolTypeService, never()).decreaseTotalStock(any(), anyInt());
    }

    @Test
    void whenDisableToolItemDesuso_thenDecreasesTotalStock() {
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
                ToolDamageLevel.DESUSO,
                toolType);

        ToolItemEntity dbToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType);

        when(toolItemRepository.findById(dbToolItem.getId())).thenReturn(Optional.of(dbToolItem));
        when(toolTypeService.decreaseTotalStock(any(), anyInt())).thenReturn(new ToolTypeEntity());
        when(toolItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        ToolItemEntity result = toolItemService.disableToolItem(toolItem.getId(), toolItem);

        // Then
        assertThat(result.getStatus()).isEqualTo(ToolStatus.DADA_DE_BAJA);
        assertThat(result.getDamageLevel()).isEqualTo(ToolDamageLevel.DESUSO);

        verify(toolTypeService).decreaseTotalStock(any(), eq(1));
    }

    //enableToolItem() tests
    //Normal flow case (success)
    @Test
    void whenEnableToolItem_thenIncreasesAvailableStockAndSetsAvailable() {
        // Given
        ToolItemEntity dbToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.EN_REPARACION,
                ToolDamageLevel.DANADA,
                null);

        when(toolItemRepository.findById(dbToolItem.getId())).thenReturn(Optional.of(dbToolItem));
        when(toolTypeService.increaseAvailableStock(any(), anyInt())).thenReturn(new ToolTypeEntity());
        when(toolItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        ToolItemEntity result = toolItemService.enableToolItem(dbToolItem.getId());

        // Then
        assertThat(result.getStatus()).isEqualTo(ToolStatus.DISPONIBLE);
        assertThat(result.getDamageLevel()).isEqualTo(ToolDamageLevel.NO_DANADA);
        verify(toolTypeService).increaseAvailableStock(any(), eq(1));
    }

    //evaluateDamage() tests
    //Normal flow case (success)
    @Test
    void whenEvaluateDamageNoHistory_thenThrowsException() {
        // Given
        ToolItemEntity dbToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.EN_REPARACION,
                ToolDamageLevel.EN_EVALUACION,
                null);

        when(toolItemRepository.findById(dbToolItem.getId())).thenReturn(Optional.of(new ToolItemEntity()));
        when(loanXToolItemService.getHistoryByToolId(dbToolItem.getId())).thenReturn(Collections.emptyList());

        // When / Then
        assertThatThrownBy(() -> toolItemService.evaluateDamage(dbToolItem.getId(), new ToolItemEntity()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("nunca ha sido prestada");
    }

    @Test
    void whenEvaluateDamageNoDamage_thenEnablesToolAndRestoresClient() {
        // Given
        ToolItemEntity dbToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.EN_REPARACION,
                ToolDamageLevel.EN_EVALUACION,
                null);

        ToolItemEntity requestToolItem = new ToolItemEntity(
                null,
                null,
                ToolStatus.EN_REPARACION,
                ToolDamageLevel.NO_DANADA,
                null);

        ClientEntity client = new ClientEntity();
        client.setDebt(0);
        LoanEntity loan = new LoanEntity();
        loan.setClient(client);
        LoanXToolItemEntity history = new LoanXToolItemEntity();
        history.setLoan(loan);

        when(toolItemRepository.findById(dbToolItem.getId())).thenReturn(Optional.of(dbToolItem));
        when(loanXToolItemService.getHistoryByToolId(dbToolItem.getId())).thenReturn(List.of(history));
        when(toolItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        ToolItemEntity result = toolItemService.evaluateDamage(dbToolItem.getId(), requestToolItem);

        // Then
        assertThat(result.getStatus()).isEqualTo(ToolStatus.DISPONIBLE);
        verify(toolTypeService).increaseAvailableStock(any(), eq(1));

        // Verificar que se reactivó al cliente
        assertThat(client.getStatus()).isEqualTo("Activo");
        verify(clientService).saveClient(client);
    }

    @Test
    void whenEvaluateDamageIrreparable_thenChargesClientAndDisablesTool() {
        // Given
        ToolItemEntity dbToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.EN_REPARACION,
                ToolDamageLevel.EN_EVALUACION,
                null);

        ToolItemEntity requestToolItem = new ToolItemEntity(
                null,
                null,
                ToolStatus.EN_REPARACION,
                ToolDamageLevel.IRREPARABLE,
                null);

        ClientEntity client = new ClientEntity();
        LoanEntity loan = new LoanEntity(); loan.setClient(client);
        LoanXToolItemEntity history = new LoanXToolItemEntity(); history.setLoan(loan);

        when(toolItemRepository.findById(dbToolItem.getId())).thenReturn(Optional.of(dbToolItem));
        when(loanXToolItemService.getHistoryByToolId(dbToolItem.getId())).thenReturn(List.of(history));
        when(toolItemRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        ToolItemEntity result = toolItemService.evaluateDamage(dbToolItem.getId(), requestToolItem);

        // Then
        assertThat(result.getStatus()).isEqualTo(ToolStatus.DADA_DE_BAJA);
        verify(toolTypeService).decreaseTotalStock(any(), eq(1));
        verify(clientService).chargeClientFee(eq(client), any(), eq(ToolDamageLevel.IRREPARABLE));
    }

    //deleteToolItemById() tests
    //Normal flow case (success)
    @Test
    void whenDeleteToolItemByIdExists_thenReturnsTrue() {
        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.EN_REPARACION,
                ToolDamageLevel.EN_EVALUACION,
                null);
        when(toolItemRepository.existsById(toolItem.getId())).thenReturn(true);
        assertThat(toolItemService.deleteToolItemById(toolItem.getId())).isTrue();
        verify(toolItemRepository).deleteById(toolItem.getId());
    }

    @Test
    void whenDeleteToolItemByIdNotExists_thenReturnsFalse() {
        when(toolItemRepository.existsById(999L)).thenReturn(false);
        assertThat(toolItemService.deleteToolItemById(1L)).isFalse();
        verify(toolItemRepository, never()).deleteById(any());
    }
}