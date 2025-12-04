package com.proyect.toolrent.Services;


import com.proyect.toolrent.Entities.ClientEntity;
import com.proyect.toolrent.Entities.ToolTypeEntity;
import com.proyect.toolrent.Enums.ToolDamageLevel;
import com.proyect.toolrent.Repositories.ClientRepository;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ClientServiceTest {
    ClientRepository clientRepository = mock(ClientRepository.class);
    ValidationService validationService = mock(ValidationService.class);

    ClientService clientService = new ClientService(clientRepository, validationService);


    //getAllClients() tests
    //Normal flow case (success)
    @Test
    void whenGetAllClients_thenReturnAllClientsList() throws Exception {
        //Given
        ClientEntity client1 = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                0,
                0);

        ClientEntity client2 = new ClientEntity(
                "22.394.207-5",
                "Roman",
                "Reigns",
                "roman.empire@gmail.com",
                "+56933541202",
                "Activo",
                0,
                0);

        ArrayList<ClientEntity> clients = new ArrayList<>(Arrays.asList(client1, client2));

        when(clientRepository.findAll()).thenReturn(clients);

        //When
        List<ClientEntity> clientList = clientService.getAllClients();

        //Then
        assertThat(clientList).isNotEmpty();
        assertThat(clientList).hasSize(2);
        assertThat(clientList.get(0).getName()).isEqualTo("Seth");
        assertThat(clientList.get(1).getName()).isEqualTo("Roman");
    }

    //Exception (empty list)
    @Test
    void whenGetAllClientsIsEmpty_thenReturnsEmptyList() {
        //Given
        when(clientRepository.findAll()).thenReturn(Collections.emptyList());

        //When
        List<ClientEntity> result = clientService.getAllClients();

        //Then
        assertThat(result).isEmpty();
    }

    //getAllClients() tests
    //Normal flow case (success)
    @Test
    void whenGetClientByRun_thenReturnsClient() {
        // Given
        ClientEntity client = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                0,
                0);

        when(clientRepository.findById(client.getRun())).thenReturn(Optional.of(client));

        // When
        Optional<ClientEntity> result = clientService.getClientByRun(client.getRun());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getRun()).isEqualTo(client.getRun());
    }


    //Exception (Client not found)
    @Test
    void whenGetClientByRunNotFound_thenReturnsEmpty() {
        // Given
        String run = "99.999.999-9";
        when(clientRepository.findById(run)).thenReturn(Optional.empty());

        // When
        Optional<ClientEntity> result = clientService.getClientByRun(run);

        // Then
        assertThat(result).isEmpty();
    }

    //getClientsByStatus() tests
    //Normal flow case (success)
    @Test
    void whenGetClientsByStatus_thenReturnsList() {
        // Given
        ClientEntity client = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                0,
                0);

        ClientEntity client2 = new ClientEntity(
                "22.394.207-5",
                "Roman",
                "Reigns",
                "roman.empire@gmail.com",
                "+56933541202",
                "Activo",
                0,
                0);

        when(clientRepository.findByStatus(client.getStatus())).thenReturn(List.of(client, client2));

        // When
        List<ClientEntity> result = clientService.getClientsByStatus(client.getStatus());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRun()).isEqualTo(client.getRun());
        assertThat(result.get(1).getRun()).isEqualTo(client2.getRun());
    }

    //Exception (null status)
    @Test
    void whenGetClientsByStatusNull_thenReturnsEmptyList() {
        // Given
        when(clientRepository.findByStatus("Restringido")).thenReturn(null);

        // When
        List<ClientEntity> result = clientService.getClientsByStatus("Restringido");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    //exists() tests
    //Normal flow case (success)
    @Test
    void whenExists_thenReturnsTrue() {
        // Given
        String run = "22.394.207-5";
        when(clientRepository.existsById(run)).thenReturn(true);

        // When
        boolean exists = clientService.exists(run);

        // Then
        assertThat(exists).isTrue();
    }

    //Exception (client doesn't exist)
    @Test
    void whenNotExists_thenReturnsFalse() {
        // Given
        String run = "99.999.999-9";
        when(clientRepository.existsById(run)).thenReturn(false);

        // When
        boolean exists = clientService.exists(run);

        // Then
        assertThat(exists).isFalse();
    }

    //saveClient() tests
    //Normal flow case (success)
    @Test
    void whenSaveClient_thenReturnsSavedClient() {
        // Given
        ClientEntity client = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                0,
                0);
        when(clientRepository.save(client)).thenReturn(client);

        // When
        ClientEntity result = clientService.saveClient(client);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRun()).isEqualTo(client.getRun());
    }

    //updateClient() tests
    //Normal flow case (success)
    @Test
    void whenUpdateClient_thenReturnsUpdatedClient() {
        // Given
        ClientEntity existingClient = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                0,
                0);

        ClientEntity updatedClient = new ClientEntity(
                null,
                "Roman",
                null,
                "new@mail.com",
                null,
                null,
                null,
                null);

        when(clientRepository.findById(existingClient.getRun())).thenReturn(Optional.of(existingClient));


        when(validationService.isValidName(updatedClient.getName())).thenReturn(true);
        when(validationService.isValidEmail(updatedClient.getEmail())).thenReturn(true);

        when(clientRepository.save(any(ClientEntity.class))).thenAnswer(i -> i.getArgument(0));

        // When
        ClientEntity result = clientService.updateClient(existingClient.getRun(), updatedClient);

        // Then
        assertThat(result.getName()).isEqualTo("Roman");
        assertThat(result.getEmail()).isEqualTo("new@mail.com");
    }

    @Test
    void whenUpdateClientNotFound_thenThrowsRuntimeException() {
        // Given
        String run = "99.999.999-9";

        ClientEntity updatedClient = new ClientEntity(
                null,
                "Roman",
                null,
                "new@mail.com",
                null,
                null,
                null,
                null);
        when(clientRepository.findById(run)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> clientService.updateClient(run, updatedClient))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cliente no encontrado en la base de datos");
    }

    @Test
    void whenUpdateClientInvalidName_thenThrowsIllegalArgumentException() {
        // Given
        ClientEntity existingClient = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                0,
                0);

        ClientEntity updatedClient = new ClientEntity(
                null,
                "Ro%$man",
                null,
                "new@mail.com",
                null,
                null,
                null,
                null);

        when(clientRepository.findById(existingClient.getRun())).thenReturn(Optional.of(existingClient));
        when(validationService.isValidName(updatedClient.getName())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> clientService.updateClient(existingClient.getRun(), updatedClient))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nombre del cliente invalido");
    }

    @Test
    void whenUpdateClientInvalidSurname_thenThrowsIllegalArgumentException() {
        // Given
        ClientEntity existingClient = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                0,
                0);

        ClientEntity updatedClient = new ClientEntity(
                null,
                null,
                "Re()ings",
                null,
                null,
                null,
                null,
                null);

        when(clientRepository.findById(existingClient.getRun())).thenReturn(Optional.of(existingClient));
        when(validationService.isValidName(updatedClient.getSurname())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> clientService.updateClient(existingClient.getRun(), updatedClient))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Apellido del cliente invalido");
    }

    @Test
    void whenUpdateClientInvalidEmail_thenThrowsIllegalArgumentException() {
        // Given
        ClientEntity existingClient = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                0,
                0);

        ClientEntity updatedClient = new ClientEntity(
                null,
                null,
                null,
                "asdfasd.cl",
                null,
                null,
                null,
                null);

        when(clientRepository.findById(existingClient.getRun())).thenReturn(Optional.of(existingClient));
        when(validationService.isValidEmail(updatedClient.getEmail())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> clientService.updateClient(existingClient.getRun(), updatedClient))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email del cliente invalido");
    }

    @Test
    void whenUpdateClientInvalidCellphone_thenThrowsIllegalArgumentException() {
        // Given
        ClientEntity existingClient = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                0,
                0);

        ClientEntity updatedClient = new ClientEntity(
                null,
                null,
                null,
                null,
                "56898",
                null,
                null,
                null);

        when(clientRepository.findById(existingClient.getRun())).thenReturn(Optional.of(existingClient));
        when(validationService.isValidCellphone(updatedClient.getCellphone())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> clientService.updateClient(existingClient.getRun(), updatedClient))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Teléfono del cliente invalido");
    }

    @Test
    void whenUpdateClientInvalidStatus_thenThrowsIllegalArgumentException() {
        // Given
        ClientEntity existingClient = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                0,
                0);

        ClientEntity updatedClient = new ClientEntity(
                null,
                null,
                null,
                null,
                null,
                "null",
                null,
                null);

        when(clientRepository.findById(existingClient.getRun())).thenReturn(Optional.of(existingClient));
        when(validationService.isValidStatus(updatedClient.getStatus())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> clientService.updateClient(existingClient.getRun(), updatedClient))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Estado del cliente invalido");
    }

    @Test
    void whenUpdateClientInvalidDebt_thenThrowsIllegalArgumentException() {
        // Given
        ClientEntity existingClient = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                0,
                0);

        ClientEntity updatedClient = new ClientEntity(
                null,
                null,
                null,
                null,
                null,
                null,
                -15000,
                null);

        when(clientRepository.findById(existingClient.getRun())).thenReturn(Optional.of(existingClient));
        when(validationService.isValidNumber(updatedClient.getDebt())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> clientService.updateClient(existingClient.getRun(), updatedClient))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor de la deuda del cliente invalido");
    }

    @Test
    void whenUpdateClientInvalidActiveLoans_thenThrowsIllegalArgumentException() {
        // Given
        ClientEntity existingClient = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                0,
                0);

        ClientEntity updatedClient = new ClientEntity(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                -8);

        when(clientRepository.findById(existingClient.getRun())).thenReturn(Optional.of(existingClient));
        when(validationService.isValidNumber(updatedClient.getActiveLoans())).thenReturn(false);

        // When / Then
        assertThatThrownBy(() -> clientService.updateClient(existingClient.getRun(), updatedClient))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número de herramientas prestadas del cliente invalido");
    }

    //chargeClientFee() tests
    //Normal flow case (success)
    @Test
    void whenChargeClientFeeMinorDamage_thenAddsDamageFee() {
        // Given
        ClientEntity client = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                10000,
                0);

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

        when(clientRepository.findById(client.getRun())).thenReturn(Optional.of(client));

        // When
        clientService.chargeClientFee(client, toolType, ToolDamageLevel.LEVEMENTE_DANADA);

        // Then
        assertThat(client.getDebt()).isEqualTo(35000); // 10000 + 25000
        assertThat(client.getStatus()).isEqualTo("Restringido");
        verify(clientRepository).save(client);
    }

    @Test
    void whenChargeClientFeeIrreparableDamage_thenAddsReplacementValue() {
        // Given
        ClientEntity client = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                10000,
                0);

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

        when(clientRepository.findById(client.getRun())).thenReturn(Optional.of(client));

        // When
        clientService.chargeClientFee(client, toolType, ToolDamageLevel.IRREPARABLE);

        // Then
        assertThat(client.getDebt()).isEqualTo(60000); // 10000 + 50000
        assertThat(client.getStatus()).isEqualTo("Restringido");
        verify(clientRepository).save(client);
    }

    //chargeClientLateReturnFee() tests
    //Normal flow case (success)
    @Test
    void whenChargeClientLateReturnFee_thenIncreasesDebt() {
        // Given
        ClientEntity client = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                10000,
                0);

        // When
        clientService.chargeClientLateReturnFee(client, 2000);

        // Then
        assertThat(client.getDebt()).isEqualTo(12000); // 10000 + 2000
        assertThat(client.getStatus()).isEqualTo("Restringido");
        verify(clientRepository).save(client);
    }

    //deleteClientByRun() tests
    //Normal flow case (success)
    @Test
    void whenDeleteClientByRunExists_thenReturnsTrue() {
        // Given
        ClientEntity client = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                0,
                0);

        when(clientRepository.existsById(client.getRun())).thenReturn(true);

        // When
        boolean result = clientService.deleteClientByRun(client.getRun());

        // Then
        assertThat(result).isTrue();
        verify(clientRepository).deleteById(client.getRun());
    }

    //Exception (client not found)
    @Test
    void whenDeleteClientByRunNotExists_thenReturnsFalse() {
        // Given
        String run = "99.999.999-9";
        when(clientRepository.existsById(run)).thenReturn(false);

        // When
        boolean result = clientService.deleteClientByRun(run);

        // Then
        assertThat(result).isFalse();
        verify(clientRepository, never()).deleteById(any());
    }

}
