package com.proyect.toolrent.Services;

import com.proyect.toolrent.Entities.ClientEntity;
import com.proyect.toolrent.Entities.ToolTypeEntity;
import com.proyect.toolrent.Enums.ToolDamageLevel;
import com.proyect.toolrent.Repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService {
    private final ValidationService validationService;
    ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository, ValidationService validationService) {
        this.clientRepository = clientRepository;
        this.validationService = validationService;
    }

    public List<ClientEntity> getAllClients() {
        return clientRepository.findAll();
    }

    public Optional<ClientEntity> getClientByRun(String run) {
        return clientRepository.findById(run);
    }

    public List<ClientEntity> getClientsByStatus(String status) {
        List<ClientEntity> clients = clientRepository.findByStatus(status);
        return clients != null ? clients : Collections.emptyList();
    }

    public boolean exists(String run){
        return clientRepository.existsById(run);
    }

    public ClientEntity saveClient(ClientEntity client){
        return clientRepository.save(client);
    }

    public ClientEntity updateClient(String run, ClientEntity client){
        //The client is searched in the database
        ClientEntity dbClient = getClientByRun(run)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado en la base de datos"));

        //Each attribute is checked to see which one was updated
        if(client.getName() != null){
            if(!validationService.isValidName(client.getName())){
                throw new IllegalArgumentException("Nombre del cliente invalido");
            }
            dbClient.setName(client.getName());
        }

        if(client.getSurname() != null){
            if(!validationService.isValidName(client.getSurname())){
                throw new IllegalArgumentException("Apellido del cliente invalido");
            }
            dbClient.setSurname(client.getSurname());
        }

        if(client.getEmail() != null){
            if(!validationService.isValidEmail(client.getEmail())){
                throw new IllegalArgumentException("Email del cliente invalido");
            }
            dbClient.setEmail(client.getEmail());
        }

        if(client.getCellphone() != null){
            if(!validationService.isValidCellphone(client.getCellphone())){
                throw new IllegalArgumentException("Teléfono del cliente invalido");
            }
            dbClient.setCellphone(client.getCellphone());
        }

        if(client.getStatus() != null){
            if(!validationService.isValidStatus(client.getStatus())){
                throw new IllegalArgumentException("Estado del cliente invalido");
            }
            dbClient.setStatus(client.getStatus());
        }

        if(client.getDebt() != null){
            if(!validationService.isValidNumber(client.getDebt())){
                throw new IllegalArgumentException("Valor de la deuda del cliente invalido");
            }
            dbClient.setDebt(client.getDebt());
        }

        if(client.getActiveLoans() != null){
            if(!validationService.isValidNumber(client.getActiveLoans())){
                throw new IllegalArgumentException("Número de herramientas prestadas del cliente invalido");
            }
            dbClient.setActiveLoans(client.getActiveLoans());
        }

        return clientRepository.save(dbClient);
    }

    public void chargeClientFee(ClientEntity client, ToolTypeEntity toolType, ToolDamageLevel toolDamageLevel) {
        //The client is searched in the database
        Optional<ClientEntity> dbClient = getClientByRun(client.getRun());
        ClientEntity dbClientEnt = dbClient.get();

        if(toolDamageLevel == ToolDamageLevel.IRREPARABLE) {
            dbClientEnt.setDebt(dbClientEnt.getDebt() + toolType.getReplacementValue());
        } else {
            dbClientEnt.setDebt(dbClientEnt.getDebt() + toolType.getDamageFee());
        }

        dbClientEnt.setStatus("Restringido");

        clientRepository.save(dbClientEnt);
    }

    public void chargeClientLateReturnFee(ClientEntity dbClient, Integer lateReturnFee){
        dbClient.setDebt(dbClient.getDebt() + lateReturnFee);
        dbClient.setStatus("Restringido");
        clientRepository.save(dbClient);
    }

    public boolean deleteClientByRun(String run){
        if(clientRepository.existsById(run)){
            clientRepository.deleteById(run);
            return true;

        } else{
            return false;
        }
    }
}
