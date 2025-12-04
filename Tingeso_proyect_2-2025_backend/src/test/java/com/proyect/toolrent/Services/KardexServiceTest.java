package com.proyect.toolrent.Services;

import com.proyect.toolrent.Entities.KardexEntity;
import com.proyect.toolrent.Entities.ToolItemEntity;
import com.proyect.toolrent.Entities.ToolTypeEntity;
import com.proyect.toolrent.Enums.KardexOperationType;
import com.proyect.toolrent.Enums.ToolDamageLevel;
import com.proyect.toolrent.Enums.ToolStatus;
import com.proyect.toolrent.Repositories.KardexRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class KardexServiceTest {
    KardexRepository kardexRepository = mock(KardexRepository.class);

    KardexService kardexService = new KardexService(kardexRepository);

    //getAllKardex() tests
    //Normal flow case (success)
    @Test
    void whenGetAllKardex_thenReturnsList() {
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

        KardexEntity newKardex = new KardexEntity(
                23L,
                KardexOperationType.PRESTAMO,
                LocalDate.of(2025, 5, 7),
                1,
                toolType);

        KardexEntity newKardex2 = new KardexEntity(
                27L,
                KardexOperationType.DEVOLUCION,
                LocalDate.of(2025, 5, 21),
                1,
                toolType);

        when(kardexRepository.findAll()).thenReturn(List.of(newKardex, newKardex2));

        // When
        List<KardexEntity> result = kardexService.getAllKardex();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(23L);
        assertThat(result.get(1).getId()).isEqualTo(27L);
    }

    //Exception (empty case)
    @Test
    void whenGetAllKardexEmpty_thenReturnsEmptyList() {
        // Given
        when(kardexRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<KardexEntity> result = kardexService.getAllKardex();

        // Then
        assertThat(result).isEmpty();
    }

    //getKardexByToolTypeName() tests
    //Normal flow case (success)
    @Test
    void whenGetKardexByToolTypeName_thenReturnsList() {
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

        KardexEntity kardex = new KardexEntity(
                23L,
                KardexOperationType.PRESTAMO,
                LocalDate.of(2025, 5, 7),
                1,
                toolType);

        KardexEntity kardex2 = new KardexEntity(
                237L,
                KardexOperationType.BAJA,
                LocalDate.of(2025, 10, 17),
                1,
                toolType);

        when(kardexRepository.findByToolType_Name(toolType.getName())).thenReturn(Optional.of(List.of(kardex, kardex2)));

        // When
        Optional<List<KardexEntity>> result = kardexService.getKardexByToolTypeName(toolType.getName());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2);
        assertThat(result.get().get(0).getId()).isEqualTo(23L);
        assertThat(result.get().get(1).getId()).isEqualTo(237L);
    }

    //getKardexByDateRange() tests
    //Normal flow case (success)
    @Test
    void whenGetKardexByDateRange_thenReturnsList() {
        // Given
        LocalDate start = LocalDate.of(2025, 3, 1);
        LocalDate end = LocalDate.of(2025, 11, 30);

        KardexEntity kardex = new KardexEntity(
                23L,
                KardexOperationType.PRESTAMO,
                LocalDate.of(2025, 5, 7),
                1,
                null);

        KardexEntity kardex2 = new KardexEntity(
                237L,
                KardexOperationType.BAJA,
                LocalDate.of(2025, 10, 17),
                1,
                null);

        when(kardexRepository.findByDateBetween(start, end)).thenReturn(List.of(kardex, kardex2));

        // When
        List<KardexEntity> result = kardexService.getKardexByDateRange(start, end);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(23L);
        assertThat(result.get(1).getId()).isEqualTo(237L);
    }

    //exists() tests
    //Normal flow case (success)
    @Test
    void whenExists_thenReturnsTrue() {
        // Given
        KardexEntity kardex = new KardexEntity(
                23L,
                KardexOperationType.PRESTAMO,
                LocalDate.of(2025, 5, 7),
                1,
                null);

        when(kardexRepository.existsById(kardex.getId())).thenReturn(true);

        // When
        boolean exists = kardexService.exists(kardex.getId());

        // Then
        assertThat(exists).isTrue();
    }

    //Exception (kardex doesn't exists)
    @Test
    void whenNotExists_thenReturnsFalse() {
        // Given
        Long id = 999L;
        when(kardexRepository.existsById(id)).thenReturn(false);

        // When
        boolean exists = kardexService.exists(id);

        // Then
        assertThat(exists).isFalse();
    }

    //saveKardex() tests
    //Normal flow case (success)
    @Test
    void whenSaveKardex_thenReturnsSavedEntity() {
        // Given
        KardexEntity kardex = new KardexEntity(
                23L,
                KardexOperationType.PRESTAMO,
                LocalDate.of(2025, 5, 7),
                1,
                null);

        when(kardexRepository.save(kardex)).thenReturn(kardex);

        // When
        KardexEntity result = kardexService.saveKardex(kardex);

        // Then
        assertThat(result).isNotNull();
        verify(kardexRepository).save(kardex);
    }

    //createRegisterToolItemKardex() tests
    //Normal flow case (success)
    @Test
    void whenCreateRegisterToolItemKardex_thenSavesRegistroOperation() {
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
        kardexService.createRegisterToolItemKardex(toolItem);

        // Then
        ArgumentCaptor<KardexEntity> captor = ArgumentCaptor.forClass(KardexEntity.class);
        verify(kardexRepository).save(captor.capture());

        KardexEntity savedKardex = captor.getValue();
        assertThat(savedKardex.getOperationType()).isEqualTo(KardexOperationType.REGISTRO);
        assertThat(savedKardex.getStockInvolved()).isEqualTo(1);
        assertThat(savedKardex.getDate()).isEqualTo(LocalDate.now());
        assertThat(savedKardex.getToolType()).isEqualTo(toolType);
    }

    //createRegisterToolTypeKardex() tests
    //Normal flow case (success)
    @Test
    void whenCreateRegisterToolTypeKardex_thenSavesRegistroOperationWithTotalStock() {
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

        // When
        kardexService.createRegisterToolTypeKardex(toolType);

        // Then
        ArgumentCaptor<KardexEntity> captor = ArgumentCaptor.forClass(KardexEntity.class);
        verify(kardexRepository).save(captor.capture());

        KardexEntity savedKardex = captor.getValue();
        assertThat(savedKardex.getOperationType()).isEqualTo(KardexOperationType.REGISTRO);
        assertThat(savedKardex.getStockInvolved()).isEqualTo(12);
    }

    //createLoanKardex() tests
    //Normal flow case (success)
    @Test
    void whenCreateLoanKardex_thenSavesPrestamoOperation() {
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

        // When
        kardexService.createLoanKardex(toolType);

        // Then
        ArgumentCaptor<KardexEntity> captor = ArgumentCaptor.forClass(KardexEntity.class);
        verify(kardexRepository).save(captor.capture());

        KardexEntity savedKardex = captor.getValue();
        assertThat(savedKardex.getOperationType()).isEqualTo(KardexOperationType.PRESTAMO);
        assertThat(savedKardex.getStockInvolved()).isEqualTo(1);
    }

    //createReturnKardex() tests
    //Normal flow case (success)
    @Test
    void whenCreateReturnKardex_thenSavesDevolucionOperation() {
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

        // When
        kardexService.createReturnKardex(toolType);

        // Then
        ArgumentCaptor<KardexEntity> captor = ArgumentCaptor.forClass(KardexEntity.class);
        verify(kardexRepository).save(captor.capture());

        KardexEntity savedKardex = captor.getValue();
        assertThat(savedKardex.getOperationType()).isEqualTo(KardexOperationType.DEVOLUCION);
        assertThat(savedKardex.getStockInvolved()).isEqualTo(1);
    }

    //createDisableToolItemKardex() tests
    //Normal flow case (success)
    @Test
    void whenDisableToolItemIrreparable_thenSavesBajaOperation() {
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
                ToolStatus.DADA_DE_BAJA,
                ToolDamageLevel.IRREPARABLE,
                toolType);

        // When
        kardexService.createDisableToolItemKardex(toolItem);

        // Then
        ArgumentCaptor<KardexEntity> captor = ArgumentCaptor.forClass(KardexEntity.class);
        verify(kardexRepository).save(captor.capture());

        assertThat(captor.getValue().getOperationType()).isEqualTo(KardexOperationType.BAJA);
    }

    @Test
    void whenDisableToolItemDamagedButReparable_thenSavesReparacionOperation() {
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
                ToolStatus.EN_REPARACION,
                ToolDamageLevel.DANADA,
                toolType);

        // When
        kardexService.createDisableToolItemKardex(toolItem);

        // Then
        ArgumentCaptor<KardexEntity> captor = ArgumentCaptor.forClass(KardexEntity.class);
        verify(kardexRepository).save(captor.capture());

        assertThat(captor.getValue().getOperationType()).isEqualTo(KardexOperationType.REPARACION);
    }

    //deleteKardexById() tests
    //Normal flow case (success)
    @Test
    void whenDeleteKardexByIdExists_thenReturnsTrue() {
        // Given
        KardexEntity kardex = new KardexEntity(
                23L,
                KardexOperationType.PRESTAMO,
                LocalDate.of(2025, 5, 7),
                1,
                null);

        when(kardexRepository.existsById(kardex.getId())).thenReturn(true);

        // When
        boolean result = kardexService.deleteKardexById(kardex.getId());

        // Then
        assertThat(result).isTrue();
        verify(kardexRepository).deleteById(kardex.getId());
    }

    @Test
    void whenDeleteKardexByIdNotExists_thenReturnsFalse() {
        // Given
        Long id = 999L;
        when(kardexRepository.existsById(id)).thenReturn(false);

        // When
        boolean result = kardexService.deleteKardexById(id);

        // Then
        assertThat(result).isFalse();
        verify(kardexRepository, never()).deleteById(any());
    }

}
