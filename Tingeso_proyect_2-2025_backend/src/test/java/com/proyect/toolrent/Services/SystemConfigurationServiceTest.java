package com.proyect.toolrent.Services;

import com.proyect.toolrent.Entities.SystemConfigurationEntity;
import com.proyect.toolrent.Repositories.SystemConfigurationRepositpry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SystemConfigurationServiceTest {

    SystemConfigurationRepositpry sysConfigRepository = mock(SystemConfigurationRepositpry.class);

    SystemConfigurationService systemConfigurationService = new SystemConfigurationService(sysConfigRepository);

    //getLateReturnFee() tests
    //Normal flow case (success)
    @Test
    void whenGetLateReturnFeeExists_thenReturnsValue() {
        // Given
        SystemConfigurationEntity config = new SystemConfigurationEntity();
        config.setCurrentLateReturnFee(5000);

        when(sysConfigRepository.findById(1L)).thenReturn(Optional.of(config));

        // When
        Integer fee = systemConfigurationService.getLateReturnFee();

        // Then
        assertThat(fee).isEqualTo(5000);
    }

    //Exception (late return fee doesn't exists)
    @Test
    void whenGetLateReturnFeeNotExists_thenReturnsDefault() {
        // Given
        when(sysConfigRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Integer fee = systemConfigurationService.getLateReturnFee();

        // Then
        //The default value defined int the service is 2000
        assertThat(fee).isEqualTo(2000);
    }

    //updateLateReturnFee() tests
    //Normal flow case (success)
    @Test
    void whenUpdateLateReturnFeeExists_thenUpdatesExisting() {
        // Given
        SystemConfigurationEntity existingConfig = new SystemConfigurationEntity();
        existingConfig.setCurrentLateReturnFee(1000);

        when(sysConfigRepository.findById(1L)).thenReturn(Optional.of(existingConfig));

        // When
        systemConfigurationService.updateLateReturnFee(3000);

        // Then
        ArgumentCaptor<SystemConfigurationEntity> captor = ArgumentCaptor.forClass(SystemConfigurationEntity.class);
        verify(sysConfigRepository).save(captor.capture());

        SystemConfigurationEntity savedConfig = captor.getValue();
        assertThat(savedConfig.getId()).isEqualTo(1L);
        assertThat(savedConfig.getCurrentLateReturnFee()).isEqualTo(3000);
    }

    //Exception (late return fee doesn't exists)
    @Test
    void whenUpdateLateReturnFeeNotExists_thenCreatesNew() {
        // Given
        when(sysConfigRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        systemConfigurationService.updateLateReturnFee(3000);

        // Then
        ArgumentCaptor<SystemConfigurationEntity> captor = ArgumentCaptor.forClass(SystemConfigurationEntity.class);
        verify(sysConfigRepository).save(captor.capture());

        SystemConfigurationEntity savedConfig = captor.getValue();
        assertThat(savedConfig.getId()).isEqualTo(1L); // Se asegura que sea el ID 1
        assertThat(savedConfig.getCurrentLateReturnFee()).isEqualTo(3000);
    }
}
