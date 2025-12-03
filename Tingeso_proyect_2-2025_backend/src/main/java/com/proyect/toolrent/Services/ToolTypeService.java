package com.proyect.toolrent.Services;

import com.proyect.toolrent.Entities.ToolTypeEntity;
import com.proyect.toolrent.Repositories.ToolTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ToolTypeService {
    ToolTypeRepository toolTypeRepository;
    ValidationService validationService;
    KardexService kardexService;

    @Autowired
    public ToolTypeService(ToolTypeRepository toolTypeRepository, ValidationService validationService, KardexService kardexService) {
        this.toolTypeRepository = toolTypeRepository;
        this.validationService = validationService;
        this.kardexService = kardexService;
    }

    public List<ToolTypeEntity> getAllToolTypes(){
        return toolTypeRepository.findAll();
    }

    public Optional<ToolTypeEntity> getToolTypeById(Long id){
        return toolTypeRepository.findById(id);
    }

    public Optional<ToolTypeEntity> getToolTypeByName(String name){
        return toolTypeRepository.findByName(name);
    }

    public ToolTypeEntity createToolType(ToolTypeEntity newToolType){
        //Save the toolType
        ToolTypeEntity savedToolType = toolTypeRepository.save(newToolType);

        //Create and save the associated kardex
        kardexService.createRegisterToolTypeKardex(newToolType);

        return savedToolType;
    }

    public boolean exists(Long id){
        return toolTypeRepository.existsById(id);
    }

    public ToolTypeEntity updateToolType(Long id, ToolTypeEntity toolType){
        Optional<ToolTypeEntity> dbToolType = getToolTypeById(id);
        ToolTypeEntity dbToolTypeEnt = dbToolType.get();

        if(toolType.getName() != null){
            if(!validationService.isValidToolName(toolType.getName())){
                throw new IllegalArgumentException("Nombre de la herramienta inválido");
            }
            dbToolTypeEnt.setName(toolType.getName());
        }

        if(toolType.getCategory() != null){
            if(!validationService.isValidName(toolType.getCategory())){
                throw new IllegalArgumentException("Categoría de la herramienta inválido");
            }
            dbToolTypeEnt.setCategory(toolType.getCategory());
        }

        if(toolType.getModel() != null){
            if(!validationService.isValidToolName(toolType.getModel())){
                throw new IllegalArgumentException("Modelo de la herramienta inválido");
            }
            dbToolTypeEnt.setModel(toolType.getModel());
        }

        if(toolType.getReplacementValue() != null){
            if(!validationService.isValidNumber(toolType.getReplacementValue())){
                throw new IllegalArgumentException("Valor de reposición de la herramienta inválida");
            }
            dbToolTypeEnt.setReplacementValue(toolType.getReplacementValue());
        }

        if(toolType.getTotalStock() != null){
            if(!validationService.isValidNumber(toolType.getTotalStock())){
                throw new IllegalArgumentException("Valor del stock total de la herramienta inválido");
            }
            dbToolTypeEnt.setTotalStock(toolType.getTotalStock());
        }

        if(toolType.getAvailableStock() != null){
            if(!validationService.isValidNumber(toolType.getAvailableStock())){
                throw new IllegalArgumentException("Valor del stock disponible de la herramienta inválido");
            }
            dbToolTypeEnt.setAvailableStock(toolType.getAvailableStock());
        }

        if(toolType.getRentalFee() != null){
            if(!validationService.isValidNumber(toolType.getRentalFee())){
                throw new IllegalArgumentException("Tarifa de arriendo de la herramienta invalida");
            }
            dbToolTypeEnt.setRentalFee(toolType.getRentalFee());
        }

        if(toolType.getDamageFee() != null){
            if(!validationService.isValidNumber(toolType.getDamageFee())){
                throw new IllegalArgumentException("Tarifa de daño de la herramienta invalida");
            }
            dbToolTypeEnt.setDamageFee(toolType.getDamageFee());
        }

        return toolTypeRepository.save(dbToolTypeEnt);
    }

    public ToolTypeEntity increaseAvailableStock(ToolTypeEntity toolType, Integer quantity){
        Optional<ToolTypeEntity> dbToolType = getToolTypeById(toolType.getId());
        ToolTypeEntity dbToolTypeEnt = dbToolType.get();

        dbToolTypeEnt.setAvailableStock(dbToolTypeEnt.getAvailableStock() + quantity);
        return toolTypeRepository.save(dbToolTypeEnt);
    }

    public ToolTypeEntity decreaseAvailableStock(ToolTypeEntity toolType, Integer quantity){
        Optional<ToolTypeEntity> dbToolType = getToolTypeById(toolType.getId());
        ToolTypeEntity dbToolTypeEnt = dbToolType.get();

        dbToolTypeEnt.setAvailableStock(dbToolTypeEnt.getAvailableStock() - quantity);
        return toolTypeRepository.save(dbToolTypeEnt);
    }

    public ToolTypeEntity increaseTotalStock(ToolTypeEntity toolType, Integer quantity){
        Optional<ToolTypeEntity> dbToolType = getToolTypeById(toolType.getId());
        ToolTypeEntity dbToolTypeEnt = dbToolType.get();

        dbToolTypeEnt.setTotalStock(dbToolTypeEnt.getTotalStock() + quantity);
        return toolTypeRepository.save(dbToolTypeEnt);
    }

    public ToolTypeEntity decreaseTotalStock(ToolTypeEntity toolType, Integer quantity){
        Optional<ToolTypeEntity> dbToolType = getToolTypeById(toolType.getId());
        ToolTypeEntity dbToolTypeEnt = dbToolType.get();

        dbToolTypeEnt.setTotalStock(dbToolTypeEnt.getTotalStock() - quantity);
        return toolTypeRepository.save(dbToolTypeEnt);
    }

    public ToolTypeEntity increaseBothStocks(ToolTypeEntity toolType, Integer quantity){
        Optional<ToolTypeEntity> dbToolType = getToolTypeById(toolType.getId());
        ToolTypeEntity dbToolTypeEnt = dbToolType.get();

        dbToolTypeEnt.setTotalStock(dbToolTypeEnt.getTotalStock() + quantity);
        dbToolTypeEnt.setAvailableStock(dbToolTypeEnt.getAvailableStock() + quantity);

        return toolTypeRepository.save(dbToolTypeEnt);
    }

    public boolean deleteToolTypeById(Long id){
        if(toolTypeRepository.existsById(id)){
            toolTypeRepository.deleteById(id);
            return true;

        } else{
            return false;
        }
    }
}
