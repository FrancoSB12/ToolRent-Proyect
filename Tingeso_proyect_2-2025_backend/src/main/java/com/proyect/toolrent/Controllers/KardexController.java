package com.proyect.toolrent.Controllers;

import com.proyect.toolrent.Entities.KardexEntity;
import com.proyect.toolrent.Entities.ToolTypeEntity;
import com.proyect.toolrent.Services.KardexService;
import com.proyect.toolrent.Services.ToolTypeService;
import com.proyect.toolrent.Services.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/kardexes")
@CrossOrigin("*")
public class KardexController {
    private final ValidationService validationService;
    private final KardexService kardexService;
    private final ToolTypeService toolTypeService;

    @Autowired
    public KardexController(ValidationService validationService, KardexService kardexService, ToolTypeService toolTypeService) {
        this.validationService = validationService;
        this.kardexService = kardexService;
        this.toolTypeService = toolTypeService;
    }

    //Create kardex
    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @PostMapping
    public ResponseEntity<?> createKardex(@RequestBody KardexEntity kardex) {
        //First, it's verified that the kardex doesn't exist
        if (kardex.getId() != null && kardexService.exists(kardex.getId())) {
            return new ResponseEntity<>("El kardex ya existe en la base de datos", HttpStatus.CONFLICT);
        }

        //Then, the data is validated for accuracy
        if (!validationService.isValidOperationType(kardex.getOperationType().toString())) {
            return new ResponseEntity<>("El tipo de operación es inválido", HttpStatus.BAD_REQUEST);
        }

        if (!validationService.isValidDate(kardex.getDate())) {
            return new ResponseEntity<>("La fecha es inválida", HttpStatus.BAD_REQUEST);
        }

        if (!validationService.isValidNumber(kardex.getStockInvolved())) {
            return new ResponseEntity<>("El stock involucrado es inválido", HttpStatus.BAD_REQUEST);
        }

        KardexEntity newKardex = kardexService.saveKardex(kardex);
        return new ResponseEntity<>(newKardex, HttpStatus.CREATED);
    }

    //Get Kardex
    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/")
    public ResponseEntity<List<KardexEntity>> getAllKardex() {
        List<KardexEntity> kardexes = kardexService.getAllKardex();
        return new ResponseEntity<>(kardexes, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/tool/{toolName}")
    public ResponseEntity<?> getKardexByToolName(@PathVariable String toolName) {
        //First, it's verified that the tool exist
        Optional<ToolTypeEntity> existingTool = toolTypeService.getToolTypeByName(toolName);
        if(existingTool.isEmpty()){
            return new ResponseEntity<>("Herramienta no encontrada en la base de datos", HttpStatus.NOT_FOUND);
        }

        Optional<List<KardexEntity>> kardexes = kardexService.getKardexByToolTypeName(toolName);
        return new ResponseEntity<>(kardexes, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/by-date")
    public ResponseEntity<?> getKardexByDateRange(@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if(startDate.isAfter(endDate)){
            return new ResponseEntity<>("La fecha de inicio es posterior a la de finalización", HttpStatus.BAD_REQUEST);
        }

        List<KardexEntity> kardexes = kardexService.getKardexByDateRange(startDate, endDate);
        return new ResponseEntity<>(kardexes, HttpStatus.OK);
    }

    //Delete kardex
    @PreAuthorize("hasRole('Admin')")
    @DeleteMapping("/kardex/{id}")
    public ResponseEntity<String> deleteKardexById(@PathVariable Long id){
        boolean deletedKardex = kardexService.deleteKardexById(id);
        return deletedKardex ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}