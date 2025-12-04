package com.proyect.toolrent.Services;

import com.proyect.toolrent.Entities.ToolTypeEntity;
import com.proyect.toolrent.Repositories.ToolTypeRepository;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ToolTypeServiceTest {

    ToolTypeRepository toolTypeRepository = mock(ToolTypeRepository.class);
    ValidationService validationService = mock(ValidationService.class);
    KardexService kardexService = mock(KardexService.class);

    ToolTypeService toolTypeService = new ToolTypeService(toolTypeRepository, validationService, kardexService);

    //getAllToolTypes() tests
    //Normal flow case (success)
    @Test
    void whenGetAllToolTypes_thenReturnsList() {
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

        when(toolTypeRepository.findAll()).thenReturn(List.of(toolType, toolType2));

        // When
        List<ToolTypeEntity> result = toolTypeService.getAllToolTypes();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Destornillador Phillips 2*150mm");
        assertThat(result.get(1).getName()).isEqualTo("CRAFTSMAN Martillo, fibra de vidrio, 16 onzas");
    }

    //Exception (empty list)
    @Test
    void whenGetAllToolTypesEmpty_thenReturnsEmptyList() {
        // Given
        when(toolTypeRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<ToolTypeEntity> result = toolTypeService.getAllToolTypes();

        // Then
        assertThat(result).isEmpty();
    }

    //getToolTypeById() tests
    //Normal flow case (success)
    @Test
    void whenGetToolTypeById_thenReturnsToolType() {
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

        when(toolTypeRepository.findById(toolType.getId())).thenReturn(Optional.of(toolType));

        // When
        Optional<ToolTypeEntity> result = toolTypeService.getToolTypeById(toolType.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(toolType.getId());
    }

    //Exception (tool type not found)
    @Test
    void whenGetToolTypeByIdNotFound_thenReturnsEmpty() {
        // Given
        Long id = 999L;
        when(toolTypeRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<ToolTypeEntity> result = toolTypeService.getToolTypeById(id);

        // Then
        assertThat(result).isEmpty();
    }

    //getToolTypeByName() tests
    //Normal flow case (success)
    @Test
    void whenGetToolTypeByName_thenReturnsToolType() {
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

        when(toolTypeRepository.findByName(toolType.getName())).thenReturn(Optional.of(toolType));

        // When
        Optional<ToolTypeEntity> result = toolTypeService.getToolTypeByName(toolType.getName());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo(toolType.getName());
    }

    //createToolType() tests
    //Normal flow case (success)
    @Test
    void whenCreateToolType_thenSavesAndCreatesKardex() {
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

        when(toolTypeRepository.save(toolType)).thenReturn(toolType);

        // When
        ToolTypeEntity result = toolTypeService.createToolType(toolType);

        // Then
        assertThat(result).isNotNull();
        verify(toolTypeRepository).save(toolType);
        verify(kardexService).createRegisterToolTypeKardex(toolType);
    }

    //exists() tests
    //Normal flow case (success)
    @Test
    void whenExists_thenReturnsTrue() {
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

        when(toolTypeRepository.existsById(toolType.getId())).thenReturn(true);
        assertThat(toolTypeService.exists(toolType.getId())).isTrue();
    }

    //Exception (tool don't exist)
    @Test
    void whenNotExists_thenReturnsFalse() {
        when(toolTypeRepository.existsById(999L)).thenReturn(false);
        assertThat(toolTypeService.exists(999L)).isFalse();
    }

    //updateToolType() tests
    //Normal flow case (success)
    @Test
    void whenUpdateToolTypeSuccess_thenUpdatesFields() {
        // Given
        ToolTypeEntity existingToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        ToolTypeEntity updatedToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips",
                "Destornillador",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        when(toolTypeRepository.findById(existingToolType.getId())).thenReturn(Optional.of(existingToolType));

        when(validationService.isValidToolName(updatedToolType.getName())).thenReturn(true);
        when(validationService.isValidName(updatedToolType.getCategory())).thenReturn(true);
        when(validationService.isValidToolName(updatedToolType.getModel())).thenReturn(true);
        when(validationService.isValidNumber(updatedToolType.getReplacementValue())).thenReturn(true);
        when(validationService.isValidNumber(updatedToolType.getTotalStock())).thenReturn(true);
        when(validationService.isValidNumber(updatedToolType.getAvailableStock())).thenReturn(true);
        when(validationService.isValidNumber(updatedToolType.getRentalFee())).thenReturn(true);
        when(validationService.isValidNumber(updatedToolType.getDamageFee())).thenReturn(true);

        when(toolTypeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        ToolTypeEntity result = toolTypeService.updateToolType(existingToolType.getId(), updatedToolType);

        // Then
        assertThat(result.getName()).isEqualTo("Destornillador Phillips");
        assertThat(result.getCategory()).isEqualTo("Destornillador");
        assertThat(result.getTotalStock()).isEqualTo(12);
    }

    //Exceptions (bad requests)
    @Test
    void whenUpdateToolTypeInvalidName_thenThrowsException() {
        ToolTypeEntity existingToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        ToolTypeEntity updatedToolType = new ToolTypeEntity(
                1L,
                "Desto=rnillador Phillips",
                "Destornillador",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        when(toolTypeRepository.findById(existingToolType.getId())).thenReturn(Optional.of(existingToolType));
        when(validationService.isValidToolName(updatedToolType.getName())).thenReturn(false);

        assertThatThrownBy(() -> toolTypeService.updateToolType(existingToolType.getId(), updatedToolType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nombre de la herramienta inválido");
    }

    @Test
    void whenUpdateToolTypeInvalidCategory_thenThrowsException() {
        ToolTypeEntity existingToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        ToolTypeEntity updatedToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips",
                "Destorn?¡illador",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        when(toolTypeRepository.findById(existingToolType.getId())).thenReturn(Optional.of(existingToolType));
        when(validationService.isValidToolName(updatedToolType.getName())).thenReturn(true);
        when(validationService.isValidName(updatedToolType.getCategory())).thenReturn(false);

        assertThatThrownBy(() -> toolTypeService.updateToolType(existingToolType.getId(), updatedToolType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Categoría de la herramienta inválido");
    }

    @Test
    void whenUpdateToolTypeInvalidModel_thenThrowsException() {
        ToolTypeEntity existingToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        ToolTypeEntity updatedToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-@2863",
                50000,
                12,
                7,
                15000,
                25000);

        when(toolTypeRepository.findById(existingToolType.getId())).thenReturn(Optional.of(existingToolType));
        when(validationService.isValidToolName(updatedToolType.getName())).thenReturn(true);
        when(validationService.isValidName(updatedToolType.getCategory())).thenReturn(true);
        when(validationService.isValidToolName(updatedToolType.getModel())).thenReturn(false);

        assertThatThrownBy(() -> toolTypeService.updateToolType(existingToolType.getId(), updatedToolType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Modelo de la herramienta inválido");
    }

    @Test
    void whenUpdateToolTypeInvalidReplacementValue_thenThrowsException() {
        ToolTypeEntity existingToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        ToolTypeEntity updatedToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                -50000,
                12,
                7,
                15000,
                25000);

        when(toolTypeRepository.findById(existingToolType.getId())).thenReturn(Optional.of(new ToolTypeEntity()));
        when(validationService.isValidToolName(updatedToolType.getName())).thenReturn(true);
        when(validationService.isValidName(updatedToolType.getCategory())).thenReturn(true);
        when(validationService.isValidToolName(updatedToolType.getModel())).thenReturn(true);
        when(validationService.isValidNumber(updatedToolType.getReplacementValue())).thenReturn(false);

        assertThatThrownBy(() -> toolTypeService.updateToolType(existingToolType.getId(), updatedToolType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor de reposición de la herramienta inválida");
    }

    @Test
    void whenUpdateToolTypeInvalidTotalStock_thenThrowsException() {
        ToolTypeEntity existingToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        ToolTypeEntity updatedToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                -12,
                7,
                15000,
                25000);

        when(toolTypeRepository.findById(existingToolType.getId())).thenReturn(Optional.of(new ToolTypeEntity()));
        when(validationService.isValidToolName(updatedToolType.getName())).thenReturn(true);
        when(validationService.isValidName(updatedToolType.getCategory())).thenReturn(true);
        when(validationService.isValidToolName(updatedToolType.getModel())).thenReturn(true);
        when(validationService.isValidNumber(updatedToolType.getReplacementValue())).thenReturn(true);
        when(validationService.isValidNumber(updatedToolType.getTotalStock())).thenReturn(false);

        assertThatThrownBy(() -> toolTypeService.updateToolType(existingToolType.getId(), updatedToolType))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor del stock total de la herramienta inválido");
    }

    //increaseAvailableStock() tests
    //Normal flow case (success)
    @Test
    void whenIncreaseAvailableStock_thenAddsQuantity() {
        // Given
        ToolTypeEntity existingToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        when(toolTypeRepository.findById(existingToolType.getId())).thenReturn(Optional.of(existingToolType));
        when(toolTypeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        ToolTypeEntity result = toolTypeService.increaseAvailableStock(existingToolType, 3);

        // Then
        assertThat(result.getAvailableStock()).isEqualTo(10); // 7 + 3
    }

    //decreaseAvailableStock() tests
    //Normal flow case (success)
    @Test
    void whenDecreaseAvailableStock_thenSubtractsQuantity() {
        // Given
        ToolTypeEntity existingToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        when(toolTypeRepository.findById(existingToolType.getId())).thenReturn(Optional.of(existingToolType));
        when(toolTypeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        ToolTypeEntity result = toolTypeService.decreaseAvailableStock(existingToolType, 2);

        // Then
        assertThat(result.getAvailableStock()).isEqualTo(5); // 7 - 2
    }

    //increaseTotalStock() tests
    //Normal flow case (success)
    @Test
    void whenIncreaseTotalStock_thenAddsQuantity() {
        // Given
        ToolTypeEntity existingToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        when(toolTypeRepository.findById(existingToolType.getId())).thenReturn(Optional.of(existingToolType));
        when(toolTypeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        ToolTypeEntity result = toolTypeService.increaseTotalStock(existingToolType, 5);

        // Then
        assertThat(result.getTotalStock()).isEqualTo(17); // 12 + 5
    }

    //decreaseTotalStock() tests
    //Normal flow case (success)
    @Test
    void whenDecreaseTotalStock_thenSubtractsQuantity() {
        // Given
        ToolTypeEntity existingToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        when(toolTypeRepository.findById(existingToolType.getId())).thenReturn(Optional.of(existingToolType));
        when(toolTypeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        ToolTypeEntity result = toolTypeService.decreaseTotalStock(existingToolType, 3);

        // Then
        assertThat(result.getTotalStock()).isEqualTo(9); // 12 - 3
    }

    //increaseBothStocks() tests
    //Normal flow case (success)
    @Test
    void whenIncreaseBothStocks_thenAddsQuantityToBoth() {
        // Given
        ToolTypeEntity existingToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        when(toolTypeRepository.findById(existingToolType.getId())).thenReturn(Optional.of(existingToolType));
        when(toolTypeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        ToolTypeEntity result = toolTypeService.increaseBothStocks(existingToolType, 2);

        // Then
        assertThat(result.getTotalStock()).isEqualTo(14); // 12 + 2
        assertThat(result.getAvailableStock()).isEqualTo(9); // 7 + 2
    }

    //deleteToolTypeById() tests
    //Normal flow case (success)
    @Test
    void whenDeleteToolTypeByIdExists_thenReturnsTrue() {
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

        when(toolTypeRepository.existsById(toolType.getId())).thenReturn(true);
        assertThat(toolTypeService.deleteToolTypeById(toolType.getId())).isTrue();
        verify(toolTypeRepository).deleteById(toolType.getId());
    }

    @Test
    void whenDeleteToolTypeByIdNotExists_thenReturnsFalse() {
        when(toolTypeRepository.existsById(1L)).thenReturn(false);
        assertThat(toolTypeService.deleteToolTypeById(1L)).isFalse();
        verify(toolTypeRepository, never()).deleteById(any());
    }
}
