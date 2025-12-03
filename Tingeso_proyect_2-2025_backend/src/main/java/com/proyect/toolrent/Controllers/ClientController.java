package com.proyect.toolrent.Controllers;

import com.proyect.toolrent.Entities.ClientEntity;
import com.proyect.toolrent.Services.ClientService;
import com.proyect.toolrent.Services.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@CrossOrigin("*")
public class ClientController {
    private final ValidationService validationService;
    private final ClientService clientService;

    @Autowired
    public ClientController(ValidationService validationService,  ClientService clientService) {
        this.validationService = validationService;
        this.clientService = clientService;
    }

    //Create client
    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @PostMapping
    public ResponseEntity<?> createClient(@RequestBody ClientEntity client){
        //First, it's verified that the client doesn't exist
        if(clientService.exists(client.getRun())){
            return new ResponseEntity<>("El cliente ya existe", HttpStatus.CONFLICT);
        }

        //Then, the data is validated for accuracy
        if(!validationService.isValidRun(client.getRun())){
            return new ResponseEntity<>("Run del cliente invalido", HttpStatus.BAD_REQUEST);
        }

        if(!validationService.isValidName(client.getName())){
            return new ResponseEntity<>("Nombre del cliente invalido", HttpStatus.BAD_REQUEST);
        }

        if(!validationService.isValidName(client.getSurname())){
            return new ResponseEntity<>("Apellido del cliente invalido", HttpStatus.BAD_REQUEST);
        }

        if(!validationService.isValidEmail(client.getEmail())){
            return new ResponseEntity<>("Email del cliente invalido", HttpStatus.BAD_REQUEST);
        }

        if(!validationService.isValidCellphone(client.getCellphone())){
            return new ResponseEntity<>("Teléfono del cliente invalido", HttpStatus.BAD_REQUEST);
        }

        ClientEntity newClient = clientService.saveClient(client);
        return new ResponseEntity<>(newClient, HttpStatus.CREATED);
    }

    //Get client
    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/")
    public ResponseEntity<List<ClientEntity>> getAllClients(){
        List<ClientEntity> clients = clientService.getAllClients();
        return new ResponseEntity<>(clients, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/{clientRun}")
    public ResponseEntity<ClientEntity> getClientByRun(@PathVariable("clientRun") String run){
        return clientService.getClientByRun(run)
                .map(client -> new ResponseEntity<>(client, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ClientEntity>> getClientsByStatus(@PathVariable String status){
        List<ClientEntity> clients = clientService.getClientsByStatus(status);
        return new ResponseEntity<>(clients, HttpStatus.OK);
    }

    //Update client
    @PreAuthorize("hasRole('Admin')")
    @PutMapping("/client/{run}")
    public ResponseEntity<?> updateClient(@PathVariable String run, @RequestBody ClientEntity client){
        try {
            //Verify that the client has a run and isn't null
            if (client.getRun() == null || client.getRun().isEmpty()) {
                return new ResponseEntity<>("El run no puede estar vacio o ser nulo", HttpStatus.BAD_REQUEST);
            }

            //Verify that the client exist in the database
            if (clientService.getClientByRun(run).isEmpty()) {
                return new ResponseEntity<>("El cliente no existe en la base de datos", HttpStatus.NOT_FOUND);
            }

            ClientEntity updatedClient = clientService.updateClient(run, client);
            return new ResponseEntity<>(updatedClient, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //There is no delete client since only admins are allowed to delete users
}
