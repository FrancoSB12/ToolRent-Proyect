package com.proyect.toolrent.Controllers;

import com.proyect.toolrent.Entities.ClientEntity;
import com.proyect.toolrent.Services.ClientService;
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

@WebMvcTest(controllers = ClientController.class,
excludeAutoConfiguration = { org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class,
                             org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class ClientControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @MockBean
    private ValidationService validationService;

    //createClient() tests
    //Normal flow case (success)
    @Test
    public void createClient_ShouldReturnNewClient() throws Exception {
        ClientEntity newClient = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        //Simulate that the client doesn't exist
        when(clientService.exists("20.372.403-9")).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidRun(newClient.getRun())).thenReturn(true);
        when(validationService.isValidName(newClient.getName())).thenReturn(true);
        when(validationService.isValidName(newClient.getSurname())).thenReturn(true);
        when(validationService.isValidEmail(newClient.getEmail())).thenReturn(true);
        when(validationService.isValidCellphone(newClient.getCellphone())).thenReturn(true);

        given(clientService.saveClient(Mockito.any(ClientEntity.class))).willReturn(newClient);

        String clientJson = """
                {
                    "run": "20.372.403-9",
                    "name": "Dante",
                    "surname": "Vex",
                    "email": "dante.vex@usach.cl",
                    "cellphone": "+56956982015",
                    "status": "Activo",
                    "debt": 0,
                    "borrowedTools": 0
                }
                """;

        mockMvc.perform(post("/api/clients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(clientJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Dante")));
    }

    //Exceptions (bad requests or existing client)
    @Test
    public void createClient_ExistingClient_ShouldReturnConflict() throws Exception {
        String clientJson = """
                {
                    "run": "20.372.403-9",
                    "name": "Dante",
                    "surname": "Vex",
                    "email": "dante.vex@usach.cl",
                    "cellphone": "+56956982015",
                    "status": "Activo",
                    "debt": 0,
                    "borrowedTools": 0
                }
                """;

        //Simulate that the client already exists
        when(clientService.exists("20.372.403-9")).thenReturn(true);


        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("El cliente ya existe"));

    }

    @Test
    public void createCliente_invalidRun_ShouldReturnBadRequest() throws Exception {
        ClientEntity newClient = new ClientEntity(
                "20.372.403-4",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        //Simulate that the client doesn't exist
        when(clientService.exists("20.372.403-9")).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidRun(newClient.getRun())).thenReturn(false);
        when(validationService.isValidName(newClient.getName())).thenReturn(true);
        when(validationService.isValidName(newClient.getSurname())).thenReturn(true);
        when(validationService.isValidEmail(newClient.getEmail())).thenReturn(true);
        when(validationService.isValidCellphone(newClient.getCellphone())).thenReturn(true);

        String clientJson = """
                {
                    "run": "20.372.403-4",
                    "name": "Dante",
                    "surname": "Vex",
                    "email": "dante.vex@usach.cl",
                    "cellphone": "+56956982015",
                    "status": "Activo",
                    "debt": 0,
                    "borrowedTools": 0
                }
                """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Run del cliente invalido"));
    }

    @Test
    public void createCliente_invalidName_ShouldReturnBadRequest() throws Exception {
        ClientEntity newClient = new ClientEntity(
                "20.372.403-9",
                "Dan)te",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        //Simulate that the client doesn't exist
        when(clientService.exists("20.372.403-9")).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidRun(newClient.getRun())).thenReturn(true);
        when(validationService.isValidName(newClient.getName())).thenReturn(false);
        when(validationService.isValidName(newClient.getSurname())).thenReturn(true);
        when(validationService.isValidEmail(newClient.getEmail())).thenReturn(true);
        when(validationService.isValidCellphone(newClient.getCellphone())).thenReturn(true);

        String clientJson = """
                {
                    "run": "20.372.403-9",
                    "name": "Dan)te",
                    "surname": "Vex",
                    "email": "dante.vex@usach.cl",
                    "cellphone": "+56956982015",
                    "status": "Activo",
                    "debt": 0,
                    "borrowedTools": 0
                }
                """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nombre del cliente invalido"));
    }

    @Test
    public void createCliente_invalidSurname_ShouldReturnBadRequest() throws Exception {
        ClientEntity newClient = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Ve!x",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        //Simulate that the client doesn't exist
        when(clientService.exists("20.372.403-9")).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidRun(newClient.getRun())).thenReturn(true);
        when(validationService.isValidName(newClient.getName())).thenReturn(true);
        when(validationService.isValidName(newClient.getSurname())).thenReturn(false);
        when(validationService.isValidEmail(newClient.getEmail())).thenReturn(true);
        when(validationService.isValidCellphone(newClient.getCellphone())).thenReturn(true);

        String clientJson = """
                {
                    "run": "20.372.403-9",
                    "name": "Dante",
                    "surname": "Ve!x",
                    "email": "dante.vex@usach.cl",
                    "cellphone": "+56956982015",
                    "status": "Activo",
                    "debt": 0,
                    "borrowedTools": 0
                }
                """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Apellido del cliente invalido"));
    }

    @Test
    public void createCliente_invalidEmail_ShouldReturnBadRequest() throws Exception {
        ClientEntity newClient = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex.usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        //Simulate that the client doesn't exist
        when(clientService.exists("20.372.403-9")).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidRun(newClient.getRun())).thenReturn(true);
        when(validationService.isValidName(newClient.getName())).thenReturn(true);
        when(validationService.isValidName(newClient.getSurname())).thenReturn(true);
        when(validationService.isValidEmail(newClient.getEmail())).thenReturn(false);
        when(validationService.isValidCellphone(newClient.getCellphone())).thenReturn(true);

        String clientJson = """
                {
                    "run": "20.372.403-9",
                    "name": "Dante",
                    "surname": "Vex",
                    "email": "dante.vex.usach.cl",
                    "cellphone": "+56956982015",
                    "status": "Activo",
                    "debt": 0,
                    "borrowedTools": 0
                }
                """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email del cliente invalido"));
    }

    @Test
    public void createCliente_invalidCellphone_ShouldReturnBadRequest() throws Exception {
        ClientEntity newClient = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+5695015",
                "Activo",
                0,
                0);

        //Simulate that the client doesn't exist
        when(clientService.exists("20.372.403-9")).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidRun(newClient.getRun())).thenReturn(true);
        when(validationService.isValidName(newClient.getName())).thenReturn(true);
        when(validationService.isValidName(newClient.getSurname())).thenReturn(true);
        when(validationService.isValidEmail(newClient.getEmail())).thenReturn(true);
        when(validationService.isValidCellphone(newClient.getCellphone())).thenReturn(false);

        String clientJson = """
                {
                    "run": "20.372.403-9",
                    "name": "Dante",
                    "surname": "Vex",
                    "email": "dante.vex@usach.cl",
                    "cellphone": "+5695015",
                    "status": "Activo",
                    "debt": 0,
                    "borrowedTools": 0
                }
                """;

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Teléfono del cliente invalido"));
    }

    //getAllClients() tests
    //Normal flow case (success)
    @Test
    public void getAllClients_ShouldReturnListOfClients() throws Exception {
        ClientEntity newClient1 = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Activo",
                0,
                0);

        ClientEntity newClient2 = new ClientEntity(
                "22.394.207-5",
                "Roman",
                "Reigns",
                "roman.empire@gmail.com",
                "+56933541202",
                "Activo",
                0,
                0);

        ArrayList<ClientEntity> clients = new ArrayList<>(Arrays.asList(newClient1, newClient2));

        given(clientService.getAllClients()).willReturn(clients);

        mockMvc.perform(get("/api/clients/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Seth")))
                .andExpect(jsonPath("$[1].name", is("Roman")));
    }

    //Exception (empty list)
    @Test
    public void getAllClients_ShouldReturnEmptyList() throws Exception {

        given(clientService.getAllClients()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/clients/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(content().json("[]"));
    }

    //getClientByRun() tests
    //Normal flow case (success)
    @Test
    public void getClientByRun_ShouldReturnClient() throws Exception {
        ClientEntity newClient = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                0,
                0);

        given(clientService.getClientByRun(newClient.getRun())).willReturn(Optional.of(newClient));

        mockMvc.perform(get("/api/clients/{clientRun}", "20.372.403-9"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Dante")));
    }

    //Exception (client not found)
    @Test
    public void getClientByRun_ShouldReturnNotFound() throws Exception {
        given(clientService.getClientByRun("20.372.403-9")).willReturn(Optional.empty());

        mockMvc.perform(get("/api/clients/{clientRun}", "20.372.403-9"))
                .andExpect(status().isNotFound());
    }

    //getClientsByStatus()
    //Normal flow case (success)
    @Test
    public void getClientsByStatus_ShouldReturnClientList() throws Exception {
        ClientEntity newClient1 = new ClientEntity(
                "21.864.613-1",
                "Seth",
                "Rollings",
                "sethTheBest@yahoo.com",
                "+56975130986",
                "Restringido",
                55000,
                3);

        ClientEntity newClient2 = new ClientEntity(
                "22.394.207-5",
                "Roman",
                "Reigns",
                "roman.empire@gmail.com",
                "+56933541202",
                "Activo",
                0,
                1);

        ClientEntity newClient3 = new ClientEntity(
                "12.346.576-8",
                "Maite",
                "Mesa",
                "maite123@gmail.com",
                "+56936854123",
                "Restringido",
                15000,
                0);

        ClientEntity newClient4 = new ClientEntity(
                "20.346.864-3",
                "Gragas",
                "Schrodinger",
                "migato@gmail.com",
                "+56936988245",
                "Activo",
                0,
                5);

        List<ClientEntity> clients = Arrays.asList(newClient1, newClient2, newClient3, newClient4);

        //Simulate filtering with status "Activo"
        List<ClientEntity> activeClients = clients.stream()
                        .filter(c -> c.getStatus().equals("Activo"))
                        .toList();

        given(clientService.getClientsByStatus("Activo")).willReturn(activeClients);

        mockMvc.perform(get("/api/clients/status/{status}", "Activo"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(activeClients.size())))
                .andExpect(jsonPath("$[0].name", is("Roman")))
                .andExpect(jsonPath("$[1].name", is("Gragas")));
    }

    //Exception (no clients has that status)
    @Test
    public void getClientsByStatus_NoClients_ShouldReturnEmptyList() throws Exception {
        given(clientService.getClientsByStatus("Restringido")).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/clients/status/{status}", "Restringido"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    //updateClient() test
    //Normal flow case (success)
    @Test
    public void updateClient_ShouldReturnUpdatedClient() throws Exception {
        ClientEntity updatedClient = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                50000,
                0);

        when(clientService.getClientByRun(updatedClient.getRun())).thenReturn(Optional.of(updatedClient));

        given(clientService.updateClient(eq(updatedClient.getRun()), Mockito.any(ClientEntity.class))).willReturn(updatedClient);

        String clientJson = """
                {
                    "run": "20.372.403-9",
                    "name": "Dante",
                    "surname": "Vex",
                    "email": "dante.vex@usach.cl",
                    "cellphone": "+56956982015",
                    "status": "Activo",
                    "debt": 50000,
                    "borrowedTools": 0
                }
                """;

        mockMvc.perform(put("/api/clients/client/{run}", "20.372.403-9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Dante")));
    }

    //Exceptions (Empty cases)
    @Test
    public void updateClient_RunIsMissing_ShouldReturnBadRequest() throws Exception {
        ClientEntity updatedClient = new ClientEntity(
                "",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                50000,
                0);

        when(clientService.getClientByRun(updatedClient.getRun())).thenReturn(Optional.of(updatedClient));

        given(clientService.updateClient(eq(updatedClient.getRun()), Mockito.any(ClientEntity.class))).willReturn(updatedClient);

        String clientJson = """
                {
                    "run": "",
                    "name": "Dante",
                    "surname": "Vex",
                    "email": "dante.vex@usach.cl",
                    "cellphone": "+56956982015",
                    "status": "Activo",
                    "debt": 50000,
                    "borrowedTools": 0
                }
                """;

        mockMvc.perform(put("/api/clients/client/{run}", "20.372.403-9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El run no puede estar vacio o ser nulo"));
    }

    @Test
    public void updateClient_ClientDoesntExist_ShouldReturnNotFound() throws Exception {
        ClientEntity updatedClient = new ClientEntity(
                "20.372.403-9",
                "Dante",
                "Vex",
                "dante.vex@usach.cl",
                "+56956982015",
                "Activo",
                50000,
                0);

        when(clientService.getClientByRun(updatedClient.getRun())).thenReturn(Optional.empty());

        String clientJson = """
                {
                    "run": "20.372.403-9",
                    "name": "Dante",
                    "surname": "Vex",
                    "email": "dante.vex@usach.cl",
                    "cellphone": "+56956982015",
                    "status": "Activo",
                    "debt": 50000,
                    "borrowedTools": 0
                }
                """;

        mockMvc.perform(put("/api/clients/client/{run}", "20.372.403-9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("El cliente no existe en la base de datos"));
    }

}
