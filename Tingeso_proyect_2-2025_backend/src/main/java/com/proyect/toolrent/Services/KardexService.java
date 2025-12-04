package com.proyect.toolrent.Services;

import com.proyect.toolrent.Entities.KardexEntity;
import com.proyect.toolrent.Entities.ToolItemEntity;
import com.proyect.toolrent.Entities.ToolTypeEntity;
import com.proyect.toolrent.Enums.KardexOperationType;
import com.proyect.toolrent.Enums.ToolDamageLevel;
import com.proyect.toolrent.Enums.ToolStatus;
import com.proyect.toolrent.Repositories.KardexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class KardexService {
    KardexRepository kardexRepository;

    @Autowired
    public KardexService(KardexRepository kardexRepository){
        this.kardexRepository = kardexRepository;
    }

    public List<KardexEntity> getAllKardex(){
        return kardexRepository.findAll();
    }

    public Optional<List<KardexEntity>> getKardexByToolTypeName(String toolTypeName){
        return kardexRepository.findByToolType_Name(toolTypeName);
    }

    public List<KardexEntity> getKardexByDateRange(LocalDate startDate, LocalDate endDate){
        return kardexRepository.findByDateBetween(startDate, endDate);
    }

    public boolean exists(Long id){
        return kardexRepository.existsById(id);
    }

    public KardexEntity saveKardex(KardexEntity kardex){
        return kardexRepository.save(kardex);
    }

    public void createRegisterToolItemKardex(ToolItemEntity toolItem){
        KardexEntity kardex = new KardexEntity();
        kardex.setOperationType(KardexOperationType.REGISTRO);
        kardex.setDate(LocalDate.now());
        kardex.setStockInvolved(1);     //The stock is 1 because we're creating only one tool
        kardex.setToolType(toolItem.getToolType());
        kardexRepository.save(kardex);
    }

    public void createRegisterToolTypeKardex(ToolTypeEntity toolType){
        KardexEntity kardex = new KardexEntity();
        kardex.setOperationType(KardexOperationType.REGISTRO);
        kardex.setDate(LocalDate.now());
        kardex.setStockInvolved(toolType.getTotalStock());     //The stock is 1 because we're creating only one tool
        kardex.setToolType(toolType);
        kardexRepository.save(kardex);
    }

    public void createLoanKardex(ToolTypeEntity toolType){
        KardexEntity kardex = new KardexEntity();
        kardex.setOperationType(KardexOperationType.PRESTAMO);
        kardex.setDate(LocalDate.now());
        kardex.setStockInvolved(1); //It's 1 because a client can only order one tool item of the same type
        kardex.setToolType(toolType);
        kardexRepository.save(kardex);
    }

    public void createReturnKardex(ToolTypeEntity toolType){
        KardexEntity kardex = new KardexEntity();
        kardex.setOperationType(KardexOperationType.DEVOLUCION);
        kardex.setDate(LocalDate.now());
        kardex.setStockInvolved(1); //It's 1 because a client can only order one tool item of the same type
        kardex.setToolType(toolType);
        kardexRepository.save(kardex);
    }

    public void createDisableToolItemKardex(ToolItemEntity toolItem){
        KardexEntity kardex = new KardexEntity();

        if(toolItem.getDamageLevel() == ToolDamageLevel.IRREPARABLE || toolItem.getStatus() == ToolStatus.DADA_DE_BAJA){
            kardex.setOperationType(KardexOperationType.BAJA);
        } else if(toolItem.getDamageLevel() != ToolDamageLevel.NO_DANADA || toolItem.getStatus() == ToolStatus.EN_REPARACION){
            kardex.setOperationType(KardexOperationType.REPARACION);
        }
        kardex.setDate(LocalDate.now());
        kardex.setStockInvolved(1);                 //The stock is 1 because we're evaluating only one tool
        kardex.setToolType(toolItem.getToolType());
        kardexRepository.save(kardex);
    }


    public boolean deleteKardexById(Long id){
        if(kardexRepository.existsById(id)){
            kardexRepository.deleteById(id);
            return true;

        } else{
            return false;
        }
    }

}
