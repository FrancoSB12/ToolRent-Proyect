package com.proyect.toolrent.Controllers;

import com.proyect.toolrent.Entities.*;
import com.proyect.toolrent.Enums.ToolDamageLevel;
import com.proyect.toolrent.Enums.ToolStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyect.toolrent.Services.*;
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

@WebMvcTest(controllers = ToolItemController.class,
        excludeAutoConfiguration = { org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class ToolItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ToolItemService toolItemService;

    @MockBean
    private LoanService loanService;

    @MockBean
    private ClientService clientService;

    @MockBean
    private ToolTypeService toolTypeService;

    @MockBean
    private ValidationService validationService;

    @Autowired
    private ObjectMapper objectMapper;

    //createToolItem() tests
    //Normal flow case (success)
    @Test
    public void createToolItem_ShouldReturnNewToolItem() throws Exception{
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

        ToolItemEntity newToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType);

        //Simulate that the tool item doesn't exist
        when(toolItemService.exists(newToolItem.getId())).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidSerialNumber(newToolItem.getSerialNumber())).thenReturn(true);
        when(validationService.isValidToolStatus(newToolItem.getStatus().toString())).thenReturn(true);
        when(validationService.isValidDamageLevel(newToolItem.getDamageLevel().toString())).thenReturn(true);

        //Simulate that the tool type exist
        when(toolTypeService.exists(newToolItem.getToolType().getId())).thenReturn(true);

        given(toolItemService.createToolItem(Mockito.any(ToolItemEntity.class))).willReturn(newToolItem);

        String toolItemJson = objectMapper.writeValueAsString(newToolItem);

        mockMvc.perform(post("/api/tool-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolItemJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)));
    }

    //Exceptions (bad requests or existing tool)
    @Test
    public void createToolItem_ExistingToolItem_ShouldReturnConflict() throws Exception {
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

        ToolItemEntity newToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType);

        String toolItemJson = objectMapper.writeValueAsString(newToolItem);

        //Simulate that the tool already exists
        when(toolItemService.exists(newToolItem.getId())).thenReturn(true);

        mockMvc.perform(post("/api/tool-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolItemJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("La unidad de herramienta ya existe en la base de datos"));

    }

    @Test
    public void createToolItem_InvalidSerialNumber_ShouldReturnBadRequest() throws Exception {
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

        ToolItemEntity newToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType);

        //Simulate that the tool doesn't exist
        when(toolItemService.exists(newToolItem.getId())).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidSerialNumber(newToolItem.getSerialNumber())).thenReturn(false);
        when(validationService.isValidToolStatus(newToolItem.getStatus().toString())).thenReturn(true);
        when(validationService.isValidDamageLevel(newToolItem.getDamageLevel().toString())).thenReturn(true);

        //Simulate that the tool type exist
        when(toolTypeService.exists(newToolItem.getToolType().getId())).thenReturn(true);

        given(toolItemService.createToolItem(Mockito.any(ToolItemEntity.class))).willReturn(newToolItem);

        String toolItemJson = objectMapper.writeValueAsString(newToolItem);

        mockMvc.perform(post("/api/tool-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolItemJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Número de serie de la herramienta inválido"));
    }

    @Test
    public void createToolItem_InvalidStatus_ShouldReturnBadRequest() throws Exception {
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

        ToolItemEntity newToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType);

        //Simulate that the tool doesn't exist
        when(toolItemService.exists(newToolItem.getId())).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidSerialNumber(newToolItem.getSerialNumber())).thenReturn(true);
        when(validationService.isValidToolStatus(newToolItem.getStatus().toString())).thenReturn(false);
        when(validationService.isValidDamageLevel(newToolItem.getDamageLevel().toString())).thenReturn(true);

        //Simulate that the tool type exist
        when(toolTypeService.exists(newToolItem.getToolType().getId())).thenReturn(true);

        given(toolItemService.createToolItem(Mockito.any(ToolItemEntity.class))).willReturn(newToolItem);

        String toolItemJson = objectMapper.writeValueAsString(newToolItem);

        mockMvc.perform(post("/api/tool-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolItemJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Estado de la herramienta inválido"));
    }

    @Test
    public void createToolItem_InvalidDamageLevel_ShouldReturnBadRequest() throws Exception {
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

        ToolItemEntity newToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType);

        //Simulate that the tool doesn't exist
        when(toolItemService.exists(newToolItem.getId())).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidSerialNumber(newToolItem.getSerialNumber())).thenReturn(true);
        when(validationService.isValidToolStatus(newToolItem.getStatus().toString())).thenReturn(true);
        when(validationService.isValidDamageLevel(newToolItem.getDamageLevel().toString())).thenReturn(false);

        //Simulate that the tool type exist
        when(toolTypeService.exists(newToolItem.getToolType().getId())).thenReturn(true);

        given(toolItemService.createToolItem(Mockito.any(ToolItemEntity.class))).willReturn(newToolItem);

        String toolItemJson = objectMapper.writeValueAsString(newToolItem);

        mockMvc.perform(post("/api/tool-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolItemJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nivel de daño de la herramienta inválido"));
    }

    @Test
    public void createToolItem_ToolTypeNotFound_ShouldReturnNotFound() throws Exception {
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

        ToolItemEntity newToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                toolType);

        //Simulate that the tool doesn't exist
        when(toolItemService.exists(newToolItem.getId())).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidSerialNumber(newToolItem.getSerialNumber())).thenReturn(true);
        when(validationService.isValidToolStatus(newToolItem.getStatus().toString())).thenReturn(true);
        when(validationService.isValidDamageLevel(newToolItem.getDamageLevel().toString())).thenReturn(true);

        //Simulate that the tool type exist
        when(toolTypeService.exists(newToolItem.getToolType().getId())).thenReturn(false);

        given(toolItemService.createToolItem(Mockito.any(ToolItemEntity.class))).willReturn(newToolItem);

        String toolItemJson = objectMapper.writeValueAsString(newToolItem);

        mockMvc.perform(post("/api/tool-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolItemJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("El tipo de herramienta no se encontró en la base de datos"));
    }

    //getAllToolItems() tests
    //Normal flow case (success)
    @Test
    public void getAllToolItems_ShouldReturnToolItemList() throws Exception {
        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        ToolItemEntity toolItem2 = new ToolItemEntity(
                4L,
                "452103684",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        ArrayList<ToolItemEntity> toolItems = new ArrayList<>(Arrays.asList(toolItem, toolItem2));

        given(toolItemService.getAllToolItems()).willReturn(toolItems);

        mockMvc.perform(get("/api/tool-items/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(toolItem.getId().intValue())))
                .andExpect(jsonPath("$[1].id", is(toolItem2.getId().intValue())));
    }

    //Exception (empty list)
    @Test
    public void getAllToolItems_NoToolItems_ShouldReturnEmptyList() throws Exception {
        given(toolItemService.getAllToolItems()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/tool-items/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(content().json("[]"));
    }

    //getToolItemById() tests
    //Normal flow case (success)
    @Test
    public void getToolItemById_ShouldReturnToolItem() throws Exception {
        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        given(toolItemService.getToolItemById(toolItem.getId())).willReturn(Optional.of(toolItem));

        mockMvc.perform(get("/api/tool-items/{id}", toolItem.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(toolItem.getId().intValue())));
    }

    //Exception (tool item not found)
    @Test
    public void getToolItemById_ToolItemDoesntExist_ShouldReturnNotFound() throws Exception {
        given(toolItemService.getToolItemById(16L)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/tool-items/{id}", 13L))
                .andExpect(status().isNotFound());
    }

    //getToolItemBySerialNumber() tests
    //Normal flow case (success)
    @Test
    public void getToolItemBySerialNumber_ShouldReturnToolItem() throws Exception {
        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        given(toolItemService.getToolItemBySerialNumber(toolItem.getSerialNumber())).willReturn(Optional.of(toolItem));

        mockMvc.perform(get("/api/tool-items/serial-number/{serialNumber}", toolItem.getSerialNumber()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.serialNumber", is(toolItem.getSerialNumber())));
    }

    //Exception (tool item not found)
    @Test
    public void getToolItemBySerialNumber_ToolItemDoesntExist_ShouldReturnNotFound() throws Exception {
        given(toolItemService.getToolItemBySerialNumber("65412547")).willReturn(Optional.empty());

        mockMvc.perform(get("/api/tool-items/serial-number/{serialNumber}", "65412547"))
                .andExpect(status().isNotFound());
    }

    //updateToolItem() tests
    //Normal flow case (success)
    @Test
    public void updateToolItem_ShouldReturnUpdatedToolItem() throws Exception {
        ToolItemEntity updatedToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        when(toolItemService.exists(updatedToolItem.getId())).thenReturn(true);

        given(toolItemService.updateToolItem(eq(updatedToolItem.getId()), Mockito.any(ToolItemEntity.class))).willReturn(updatedToolItem);

        String toolItemJson = objectMapper.writeValueAsString(updatedToolItem);

        mockMvc.perform(put("/api/tool-items/tool-item/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolItemJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    //Exception (Empty cases)
    @Test
    public void updateToolItem_IdIsMissing_ShouldReturnBadRequest() throws Exception {
        ToolItemEntity updatedToolItem = new ToolItemEntity(
                null,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        //The controller stops in the firsts lines since there is that validation

        String toolItemJson = objectMapper.writeValueAsString(updatedToolItem);

        mockMvc.perform(put("/api/tool-items/tool-item/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolItemJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El id no puede estar vacio o ser nulo"));
    }

    @Test
    public void updateToolItem_ToolItemDoesntExist_ShouldReturnNotFound() throws Exception {
        ToolItemEntity updatedToolItem = new ToolItemEntity(
                16L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        when(toolItemService.exists(updatedToolItem.getId())).thenReturn(false);

        String toolItemJson = objectMapper.writeValueAsString(updatedToolItem);

        mockMvc.perform(put("/api/tool-items/tool-item/{id}", 16L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolItemJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("La herramienta no existe en la base de datos"));
    }

    //disableToolItem() tests
    //Normal flow case (success)
    @Test
    public void disableToolItem_ShouldReturnDisabledToolItem() throws Exception {
        ToolItemEntity disabledToolItem = new ToolItemEntity(
                16L,
                "458754621",
                ToolStatus.DADA_DE_BAJA,
                ToolDamageLevel.IRREPARABLE,
                null);

        //Simulate that the tool exist
        when(toolItemService.getToolItemBySerialNumber(disabledToolItem.getSerialNumber())).thenReturn(Optional.of(disabledToolItem));

        given(toolItemService.disableToolItem(eq(disabledToolItem.getId()), Mockito.any(ToolItemEntity.class))).willReturn(disabledToolItem);

        String toolItemJson = objectMapper.writeValueAsString(disabledToolItem);

        mockMvc.perform(put("/api/tool-items/disable-tool-item/{serialNumber}", disabledToolItem.getSerialNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolItemJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(disabledToolItem.getId().intValue())));
    }

    //Exception (tool item doesn't exist)
    @Test
    public void disableToolItem_ToolItemDoesntExist_ShouldReturnNotFound() throws Exception {
        ToolItemEntity nonExistingDisabledToolItem = new ToolItemEntity(
                16L,
                "458754621",
                ToolStatus.DADA_DE_BAJA,
                ToolDamageLevel.IRREPARABLE,
                null);

        //Simulate that the tool doesn't exist
        when(toolItemService.getToolItemBySerialNumber(nonExistingDisabledToolItem.getSerialNumber())).thenReturn(Optional.empty());

        given(toolItemService.disableToolItem(eq(nonExistingDisabledToolItem.getId()), Mockito.any(ToolItemEntity.class))).willReturn(nonExistingDisabledToolItem);

        String toolItemJson = objectMapper.writeValueAsString(nonExistingDisabledToolItem);

        mockMvc.perform(put("/api/tool-items/disable-tool-item/{serialNumber}", nonExistingDisabledToolItem.getSerialNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolItemJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("La herramienta no existe en la base de datos"));
    }

    //enableToolItem() tests
    //Normal flow case (success)
    @Test
    public void enableToolItem_ShouldReturnEnabledToolItem() throws Exception {
        ToolItemEntity enabledToolItem = new ToolItemEntity(
                16L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        //Simulate that the tool exist
        when(toolItemService.getToolItemBySerialNumber(enabledToolItem.getSerialNumber())).thenReturn(Optional.of(enabledToolItem));

        given(toolItemService.enableToolItem(anyLong())).willReturn(enabledToolItem);

        String toolItemJson = objectMapper.writeValueAsString(enabledToolItem);

        mockMvc.perform(put("/api/tool-items/enable-tool-item/{serialNumber}", enabledToolItem.getSerialNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolItemJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(enabledToolItem.getId().intValue())));
    }

    //Exception (tool item doesn't exist and bad request)
    @Test
    public void enableToolItem_ToolItemDoesntExist_ShouldReturnNotFound() throws Exception {
        ToolItemEntity nonExistingEnabledToolItem = new ToolItemEntity(
                16L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        //Simulate that the tool doesn't exist
        when(toolItemService.getToolItemBySerialNumber(nonExistingEnabledToolItem.getSerialNumber())).thenReturn(Optional.empty());

        String toolItemJson = objectMapper.writeValueAsString(nonExistingEnabledToolItem);

        mockMvc.perform(put("/api/tool-items/enable-tool-item/{serialNumber}", nonExistingEnabledToolItem.getSerialNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolItemJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("La herramienta no existe en la base de datos"));
    }

    @Test
    public void enableToolItem_ToolItemIsIrreparable_ShouldReturnBadRequest() throws Exception {
        ToolItemEntity enabledToolItem = new ToolItemEntity(
                16L,
                "458754621",
                ToolStatus.DISPONIBLE,
                ToolDamageLevel.NO_DANADA,
                null);

        ToolItemEntity dbEnabledToolItem = new ToolItemEntity(
                16L,
                "458754621",
                ToolStatus.EN_REPARACION,
                ToolDamageLevel.IRREPARABLE,
                null);

        //Simulate that the tool exist
        when(toolItemService.getToolItemBySerialNumber(enabledToolItem.getSerialNumber())).thenReturn(Optional.of(dbEnabledToolItem));

        given(toolItemService.enableToolItem(enabledToolItem.getId())).willReturn(dbEnabledToolItem);

        String toolItemJson = objectMapper.writeValueAsString(dbEnabledToolItem);

        mockMvc.perform(put("/api/tool-items/enable-tool-item/{serialNumber}", enabledToolItem.getSerialNumber())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolItemJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La herramienta tiene un daño irreparable, por ende, no puede volver a estar disponible"));
    }

    //evaluateDamage() tests
    //Normal flow case (success)
    @Test
    public void evaluateDamage_ShouldReturnEvaluatedToolItem() throws Exception {
        ToolDamageLevel newDamageLevel = ToolDamageLevel.LEVEMENTE_DANADA;

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

        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                newDamageLevel,
                toolType);

        //Simulate that the tool exist
        when(toolItemService.getToolItemById(toolItem.getId())).thenReturn(Optional.of(toolItem));

        //Simulate validation
        when(validationService.isValidDamageLevel(newDamageLevel.toString())).thenReturn(true);

        given(toolItemService.evaluateDamage(eq(toolItem.getId()), Mockito.any(ToolItemEntity.class))).willReturn(toolItem);

        String requestJson = objectMapper.writeValueAsString(toolItem);

        mockMvc.perform(put("/api/tool-items/evaluate-damage/{toolId}", toolItem.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(toolItem.getId().intValue())));

    }

    //Exceptions (tool doesn't exist or bad request)
    @Test
    public void evaluateDamage_ToolItemDoesntExist_ShouldReturnNotFound() throws Exception {
        ToolDamageLevel newDamageLevel = ToolDamageLevel.LEVEMENTE_DANADA;

        ToolItemEntity nonExistingToolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                newDamageLevel,
                null);

        //Simulate that the tool doesn't exist
        when(toolItemService.getToolItemById(nonExistingToolItem.getId())).thenReturn(Optional.empty());

        //Simulate validation
        when(validationService.isValidDamageLevel(newDamageLevel.toString())).thenReturn(true);

        String requestJson = objectMapper.writeValueAsString(nonExistingToolItem);

        mockMvc.perform(put("/api/tool-items/evaluate-damage/{toolId}", nonExistingToolItem.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("La herramienta no existe en la base de datos"));
    }

    @Test
    public void evaluateDamage_InvalidToolDamageLevel_ShouldReturnBadRequest() throws Exception {
        ToolDamageLevel newDamageLevel = ToolDamageLevel.LEVEMENTE_DANADA;

        ToolItemEntity toolItem = new ToolItemEntity(
                1L,
                "458754621",
                ToolStatus.DISPONIBLE,
                newDamageLevel,
                null);

        //Simulate that the tool exist
        when(toolItemService.getToolItemById(toolItem.getId())).thenReturn(Optional.of(toolItem));

        //Simulate validation
        when(validationService.isValidDamageLevel(newDamageLevel.toString())).thenReturn(false);

        String requestJson = objectMapper.writeValueAsString(toolItem);

        mockMvc.perform(put("/api/tool-items/evaluate-damage/{toolId}", toolItem.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nivel de daño de la herramienta inválido"));
    }

    //deleteToolById() tests
    //Normal flow case (success)
    @Test
    public void deleteToolItemById_ShouldReturnNoContent() throws Exception {
        when(toolItemService.deleteToolItemById(567L)).thenReturn(true);

        mockMvc.perform(delete("/api/tool-items/delete/{id}", 567))
                .andExpect(status().isNoContent());
    }

    //Exception (tool item doesn't exist)
    @Test
    public void deleteToolById_ToolDoesntExist_ShouldReturnNotFound() throws Exception {
        when(toolItemService.deleteToolItemById(673L)).thenReturn(false);

        mockMvc.perform(delete("/api/tool-items/delete/{id}", 673))
                .andExpect(status().isNotFound());
    }
}
