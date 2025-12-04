package com.proyect.toolrent.Controllers;

import com.proyect.toolrent.Entities.ToolTypeEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyect.toolrent.Services.ToolTypeService;
import com.proyect.toolrent.Services.ValidationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ToolTypeController.class,
        excludeAutoConfiguration = { org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class ToolTypeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ToolTypeService toolTypeService;
    
    @MockBean
    private ValidationService validationService;
    @Autowired
    private ObjectMapper objectMapper;

    //createToolType() tests
    //Normal flow case (success)
    @Test
    public void createToolType_ShouldReturnNewToolType() throws Exception{
        ToolTypeEntity newToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        //Simulate that the tool type doesn't exist
        when(toolTypeService.exists(newToolType.getId())).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidToolName(newToolType.getName())).thenReturn(true);
        when(validationService.isValidName(newToolType.getCategory())).thenReturn(true);
        when(validationService.isValidToolName(newToolType.getModel())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getReplacementValue())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getRentalFee())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getDamageFee())).thenReturn(true);

        given(toolTypeService.createToolType(Mockito.any(ToolTypeEntity.class))).willReturn(newToolType);

        String toolTypeJson = """
                {
                    "id": 1,
                    "name": "Destornillador Phillips 2*150mm",
                    "category": "Destornillador de cruz",
                    "model": "RH-2863",
                    "replacementValue": 50000,
                    "totalStock": 12,
                    "availableStock": 7,
                    "rentalFee": 15000,
                    "damageFee": 25000
                }
                """;

        mockMvc.perform(post("/api/tool-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolTypeJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)));
    }

    //Exceptions (bad requests or existing tool type)
    @Test
    public void createToolType_ExistingToolType_ShouldReturnConflict() throws Exception {
        ToolTypeEntity newToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        String toolTypeJson = """
                {
                    "id": 1,
                    "name": "Destornillador Phillips 2*150mm",
                    "category": "Destornillador de cruz",
                    "model": "RH-2863",
                    "replacementValue": 50000,
                    "totalStock": 12,
                    "availableStock": 7,
                    "rentalFee": 15000,
                    "damageFee": 25000
                }
                """;

        //Simulate that the tool already exists
        when(toolTypeService.exists(newToolType.getId())).thenReturn(true);

        mockMvc.perform(post("/api/tool-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolTypeJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("El tipo de herramienta ya existe en la base de datos"));

    }

    @Test
    public void createToolType_InvalidToolName_ShouldReturnBadRequest() throws Exception {
        ToolTypeEntity newToolType = new ToolTypeEntity(
                1L,
                "Dest=ornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        //Simulate that the tool type doesn't exist
        when(toolTypeService.exists(newToolType.getId())).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidToolName(newToolType.getName())).thenReturn(false);
        when(validationService.isValidName(newToolType.getCategory())).thenReturn(true);
        when(validationService.isValidToolName(newToolType.getModel())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getReplacementValue())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getRentalFee())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getDamageFee())).thenReturn(true);

        given(toolTypeService.createToolType(Mockito.any(ToolTypeEntity.class))).willReturn(newToolType);

        String toolTypeJson = """
                {
                    "id": 1,
                    "name": "Dest=ornillador Phillips 2*150mm",
                    "category": "Destornillador de cruz",
                    "model": "RH-2863",
                    "replacementValue": 50000,
                    "totalStock": 12,
                    "availableStock": 7,
                    "rentalFee": 15000,
                    "damageFee": 25000
                }
                """;

        mockMvc.perform(post("/api/tool-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolTypeJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nombre de la herramienta inválido"));
    }

    @Test
    public void createToolType_InvalidCategory_ShouldReturnBadRequest() throws Exception {
        ToolTypeEntity newToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Dest!ornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        //Simulate that the tool type doesn't exist
        when(toolTypeService.exists(newToolType.getId())).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidToolName(newToolType.getName())).thenReturn(true);
        when(validationService.isValidName(newToolType.getCategory())).thenReturn(false);
        when(validationService.isValidToolName(newToolType.getModel())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getReplacementValue())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getRentalFee())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getDamageFee())).thenReturn(true);

        given(toolTypeService.createToolType(Mockito.any(ToolTypeEntity.class))).willReturn(newToolType);

        String toolTypeJson = """
                {
                    "id": 1,
                    "name": "Destornillador Phillips 2*150mm",
                    "category": "Dest!ornillador de cruz",
                    "model": "RH-2863",
                    "replacementValue": 50000,
                    "totalStock": 12,
                    "availableStock": 7,
                    "rentalFee": 15000,
                    "damageFee": 25000
                }
                """;

        mockMvc.perform(post("/api/tool-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolTypeJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nombre de la categoría de la herramienta inválida"));
    }

    @Test
    public void createToolType_InvalidModel_ShouldReturnBadRequest() throws Exception {
        ToolTypeEntity newToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-28@63",
                50000,
                12,
                7,
                15000,
                25000);

        //Simulate that the tool type doesn't exist
        when(toolTypeService.exists(newToolType.getId())).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidToolName(newToolType.getName())).thenReturn(true);
        when(validationService.isValidName(newToolType.getCategory())).thenReturn(true);
        when(validationService.isValidToolName(newToolType.getModel())).thenReturn(false);
        when(validationService.isValidNumber(newToolType.getReplacementValue())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getRentalFee())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getDamageFee())).thenReturn(true);

        given(toolTypeService.createToolType(Mockito.any(ToolTypeEntity.class))).willReturn(newToolType);

        String toolTypeJson = """
                {
                    "id": 1,
                    "name": "Destornillador Phillips 2*150mm",
                    "category": "Destornillador de cruz",
                    "model": "RH-28@63",
                    "replacementValue": 50000,
                    "totalStock": 12,
                    "availableStock": 7,
                    "rentalFee": 15000,
                    "damageFee": 25000
                }
                """;

        mockMvc.perform(post("/api/tool-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolTypeJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Modelo de la herramienta inválido"));
    }

    @Test
    public void createToolType_InvalidReplacementValue_ShouldReturnBadRequest() throws Exception {
        ToolTypeEntity newToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                -50000,
                12,
                7,
                15000,
                25000);

        //Simulate that the tool type doesn't exist
        when(toolTypeService.exists(newToolType.getId())).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidToolName(newToolType.getName())).thenReturn(true);
        when(validationService.isValidName(newToolType.getCategory())).thenReturn(true);
        when(validationService.isValidToolName(newToolType.getModel())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getReplacementValue())).thenReturn(false);
        when(validationService.isValidNumber(newToolType.getRentalFee())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getDamageFee())).thenReturn(true);

        given(toolTypeService.createToolType(Mockito.any(ToolTypeEntity.class))).willReturn(newToolType);

        String toolTypeJson = """
                {
                    "id": 1,
                    "name": "Destornillador Phillips 2*150mm",
                    "category": "Destornillador de cruz",
                    "model": "RH-2863",
                    "replacementValue": -50000,
                    "totalStock": 12,
                    "availableStock": 7,
                    "rentalFee": 15000,
                    "damageFee": 25000
                }
                """;

        mockMvc.perform(post("/api/tool-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolTypeJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Valor de reposición de la herramienta inválido"));
    }

    @Test
    public void createToolType_InvalidRentalFee_ShouldReturnBadRequest() throws Exception {
        ToolTypeEntity newToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                -15000,
                25000);

        //Simulate that the tool type doesn't exist
        when(toolTypeService.exists(newToolType.getId())).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidToolName(newToolType.getName())).thenReturn(true);
        when(validationService.isValidName(newToolType.getCategory())).thenReturn(true);
        when(validationService.isValidToolName(newToolType.getModel())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getReplacementValue())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getRentalFee())).thenReturn(false);
        when(validationService.isValidNumber(newToolType.getDamageFee())).thenReturn(true);

        given(toolTypeService.createToolType(Mockito.any(ToolTypeEntity.class))).willReturn(newToolType);

        String toolTypeJson = """
                {
                    "id": 1,
                    "name": "Destornillador Phillips 2*150mm",
                    "category": "Destornillador de cruz",
                    "model": "RH-2863",
                    "replacementValue": 50000,
                    "totalStock": 12,
                    "availableStock": 7,
                    "rentalFee": -15000,
                    "damageFee": 25000
                }
                """;

        mockMvc.perform(post("/api/tool-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolTypeJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Tarifa de arriendo de la herramienta inválida"));
    }

    @Test
    public void createTool_InvalidDamageFee_ShouldReturnBadRequest() throws Exception {
        ToolTypeEntity newToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                -25000);

        //Simulate that the tool type doesn't exist
        when(toolTypeService.exists(newToolType.getId())).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidToolName(newToolType.getName())).thenReturn(true);
        when(validationService.isValidName(newToolType.getCategory())).thenReturn(true);
        when(validationService.isValidToolName(newToolType.getModel())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getReplacementValue())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getRentalFee())).thenReturn(true);
        when(validationService.isValidNumber(newToolType.getDamageFee())).thenReturn(false);

        given(toolTypeService.createToolType(Mockito.any(ToolTypeEntity.class))).willReturn(newToolType);

        String toolTypeJson = """
                {
                    "id": 1,
                    "name": "Destornillador Phillips 2*150mm",
                    "category": "Destornillador de cruz",
                    "model": "RH-2863",
                    "replacementValue": 50000,
                    "totalStock": 12,
                    "availableStock": 7,
                    "rentalFee": 15000,
                    "damageFee": -25000
                }
                """;

        mockMvc.perform(post("/api/tool-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolTypeJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Tarifa de daño de la herramienta inválida"));
    }

    //getAllToolTypes() tests
    //Normal flow case (success)
    @Test
    public void getAllToolTypes_ShouldReturnToolTypeList() throws Exception {
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

        ToolTypeEntity toolType2 = new ToolTypeEntity(
                19L,
                "CRAFTSMAN Martillo, fibra de vidrio, 16 onzas",
                "Martillo",
                "CMHT51398",
                10000,
                24,
                14,
                3000,
                6000);

        ArrayList<ToolTypeEntity> toolTypes = new ArrayList<>(Arrays.asList(toolType, toolType2));

        given(toolTypeService.getAllToolTypes()).willReturn(toolTypes);

        mockMvc.perform(get("/api/tool-types/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(toolType.getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(toolType2.getId().intValue())));
    }

    //Exception (empty list)
    @Test
    public void getAllToolTypes_NoToolTypes_ShouldReturnEmptyList() throws Exception {

        given(toolTypeService.getAllToolTypes()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/tool-types/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(content().json("[]"));
    }

    //getToolTypeById() tests
    //Normal flow case (success)
    @Test
    public void getToolTypeById_ShouldReturnToolType() throws Exception {
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

        given(toolTypeService.getToolTypeById(toolType.getId())).willReturn(Optional.of(toolType));

        mockMvc.perform(get("/api/tool-types/{id}", toolType.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(toolType.getId().intValue())));
    }

    //Exception (tool type not found)
    @Test
    public void getToolTypeById_ToolTypeDoesntExist_ShouldReturnNotFound() throws Exception {
        given(toolTypeService.getToolTypeById(13L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/tool-types/{id}", 13L))
                .andExpect(status().isNotFound());
    }

    //getToolTypeByName() tests
    //Normal flow case (success)
    @Test
    public void getToolTypeByName_ShouldReturnToolType() throws Exception {
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

        given(toolTypeService.getToolTypeByName(toolType.getName())).willReturn(Optional.of(toolType));

        mockMvc.perform(get("/api/tool-types/name/{name}", toolType.getName()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(toolType.getName())));
    }

    //Exception (tool type not found)
    @Test
    public void getToolTypeByName_ToolTypeDoesntExist_ShouldReturnNotFound() throws Exception {
        String toolTypeName = "Kit de Herramientas Giratorias 170W + 126 Accesorios";
        given(toolTypeService.getToolTypeByName(toolTypeName)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/tools/name/{name}", toolTypeName))
                .andExpect(status().isNotFound());
    }

    //updateToolType() tests
    //Normal flow case (success)
    @Test
    public void updateToolType_ShouldReturnUpdatedToolType() throws Exception {
        ToolTypeEntity updatedToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        when(toolTypeService.exists(updatedToolType.getId())).thenReturn(true);

        given(toolTypeService.updateToolType(eq(updatedToolType.getId()), Mockito.any(ToolTypeEntity.class))).willReturn(updatedToolType);

        String toolTypeJson = """
                {
                    "id": 1,
                    "name": "Destornillador Phillips 2*150mm",
                    "category": "Destornillador de cruz",
                    "model": "RH-2863",
                    "replacementValue": 50000,
                    "totalStock": 12,
                    "availableStock": 7,
                    "rentalFee": 15000,
                    "damageFee": -25000
                }
                """;

        mockMvc.perform(put("/api/tool-types/tool-type/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolTypeJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    //Exception (Empty cases)
    @Test
    public void updateToolType_IdIsMissing_ShouldReturnBadRequest() throws Exception {
        ToolTypeEntity updatedToolType = new ToolTypeEntity(
                null,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                25000);

        //The controller stops in the firsts lines since there is that validation

        String toolTypeJson = objectMapper.writeValueAsString(updatedToolType);

        mockMvc.perform(put("/api/tool-types/tool-type/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolTypeJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El id no puede estar vacio o ser nulo"));
    }

    @Test
    public void updateToolType_ToolTypeDoesntExist_ShouldReturnNotFound() throws Exception {
        ToolTypeEntity updatedToolType = new ToolTypeEntity(
                1L,
                "Destornillador Phillips 2*150mm",
                "Destornillador de cruz",
                "RH-2863",
                50000,
                12,
                7,
                15000,
                -25000);

        when(toolTypeService.exists(updatedToolType.getId())).thenReturn(false);

        String toolTypeJson = """
                {
                    "id": 1,
                    "name": "Destornillador Phillips 2*150mm",
                    "category": "Destornillador de cruz",
                    "model": "RH-2863",
                    "replacementValue": 50000,
                    "totalStock": 12,
                    "availableStock": 7,
                    "rentalFee": 15000,
                    "damageFee": -25000
                }
                """;

        mockMvc.perform(put("/api/tool-types/tool-type/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolTypeJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("El tipo de herramienta no existe en la base de datos"));
    }

    //deleteToolTypeById() tests
    //Normal flow case (success)
    @Test
    public void deleteToolTypeById_ShouldReturnNoContent() throws Exception {
        when(toolTypeService.deleteToolTypeById(567L)).thenReturn(true);

        mockMvc.perform(delete("/api/tool-types/delete/{id}", 567))
                .andExpect(status().isNoContent());
    }

    //Exception (tool doesn't exist)
    @Test
    public void deleteToolTypeById_ToolTypeDoesntExist_ShouldReturnNotFound() throws Exception {
        when(toolTypeService.deleteToolTypeById(673L)).thenReturn(false);

        mockMvc.perform(delete("/api/tool-types/delete/{id}", 673))
                .andExpect(status().isNotFound());
    }
    
}
