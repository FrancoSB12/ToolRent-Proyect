package com.proyect.toolrent.Controllers;

import com.proyect.toolrent.Entities.ToolTypeEntity;
import com.proyect.toolrent.Services.ToolTypeService;
import com.proyect.toolrent.Services.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tool-types")
@CrossOrigin("*")
public class ToolTypeController {
    private final ToolTypeService toolTypeService;
    private final ValidationService validationService;

    @Autowired
    public ToolTypeController(ToolTypeService toolTypeService, ValidationService validationService) {
        this.toolTypeService = toolTypeService;
        this.validationService = validationService;
    }

    //Create tool type
    @PreAuthorize("hasRole('Admin')")
    @PostMapping
    public ResponseEntity<?> createToolType(@RequestBody ToolTypeEntity toolType){
        //First, it's verified that the tool type doesn't exist
        if (toolType.getId() != null && toolTypeService.exists(toolType.getId())) {
            return new ResponseEntity<>("El tipo de herramienta ya existe en la base de datos", HttpStatus.CONFLICT);
        }

        //Then, the data is validated for accuracy
        if (!validationService.isValidToolName(toolType.getName())) {
            return new ResponseEntity<>("Nombre de la herramienta inválido", HttpStatus.BAD_REQUEST);
        }

        if (!validationService.isValidName(toolType.getCategory())) {
            return new ResponseEntity<>("Nombre de la categoría de la herramienta inválida", HttpStatus.BAD_REQUEST);
        }

        if (!validationService.isValidToolName(toolType.getModel())) {
            return new ResponseEntity<>("Modelo de la herramienta inválido", HttpStatus.BAD_REQUEST);
        }

        if (!validationService.isValidNumber(toolType.getReplacementValue())) {
            return new ResponseEntity<>("Valor de reposición de la herramienta inválido", HttpStatus.BAD_REQUEST);
        }

        if (!validationService.isValidNumber(toolType.getRentalFee())) {
            return new ResponseEntity<>("Tarifa de arriendo de la herramienta inválida", HttpStatus.BAD_REQUEST);
        }

        if (!validationService.isValidNumber(toolType.getDamageFee())) {
            return new ResponseEntity<>("Tarifa de daño de la herramienta inválida", HttpStatus.BAD_REQUEST);
        }

        ToolTypeEntity newToolType = toolTypeService.createToolType(toolType);
        return new ResponseEntity<>(newToolType, HttpStatus.CREATED);
    }

    //Get tool type
    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/")
    public ResponseEntity<List<ToolTypeEntity>> getAllToolTypes(){
        List<ToolTypeEntity> toolTypes = toolTypeService.getAllToolTypes();
        return new ResponseEntity<>(toolTypes, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/{id}")
    public ResponseEntity<ToolTypeEntity> getToolTypeById(@PathVariable Long id){
        return toolTypeService.getToolTypeById(id)
                .map(toolType -> new ResponseEntity<>(toolType, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/name/{name}")
    public ResponseEntity<ToolTypeEntity> getToolTypeByName(@PathVariable String name) {
        return toolTypeService.getToolTypeByName(name)
                .map(toolType -> new ResponseEntity<>(toolType, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    //Update tool
    @PreAuthorize("hasRole('Admin')")
    @PutMapping("/tool-type/{id}")
    public ResponseEntity<?> updateToolType(@PathVariable Long id, @RequestBody ToolTypeEntity toolType){
        try {
            //Verify that the tool has an id and the id isn't null
            if (toolType.getId() == null) {
                return new ResponseEntity<>("El id no puede estar vacio o ser nulo", HttpStatus.BAD_REQUEST);
            }

            //Verify that the tool type exist in the database
            if (!toolTypeService.exists(id)) {
                return new ResponseEntity<>("El tipo de herramienta no existe en la base de datos", HttpStatus.NOT_FOUND);
            }

            ToolTypeEntity updatedToolType = toolTypeService.updateToolType(id, toolType);
            return new ResponseEntity<>(updatedToolType, HttpStatus.OK);

        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //Delete tool type
    @PreAuthorize("hasRole('Admin')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteToolTypeById(@PathVariable Long id){
        boolean deletedToolType = toolTypeService.deleteToolTypeById(id);
        return deletedToolType ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

}
