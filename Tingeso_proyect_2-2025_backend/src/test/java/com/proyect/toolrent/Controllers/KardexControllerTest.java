package com.proyect.toolrent.Controllers;


import com.proyect.toolrent.Entities.KardexEntity;
import com.proyect.toolrent.Entities.ToolTypeEntity;
import com.proyect.toolrent.Enums.KardexOperationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyect.toolrent.Services.KardexService;
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

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = KardexController.class,
        excludeAutoConfiguration = { org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class KardexControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KardexService kardexService;

    @MockBean
    private ValidationService validationService;

    @MockBean
    private ToolTypeService toolTypeService;


    //createKardex() tests
    //Normal flow case (success)
    @Test
    public void createKardex_ShouldReturnNewKardex() throws Exception{

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

        KardexEntity newKardex = new KardexEntity(
                23L,
                KardexOperationType.PRESTAMO,
                LocalDate.of(2025, 5, 7),
                1,
                toolType);

        //Simulate that the kardex doesn't exist
        when(kardexService.exists(newKardex.getId())).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidOperationType(String.valueOf(newKardex.getOperationType()))).thenReturn(true);
        when(validationService.isValidDate(newKardex.getDate())).thenReturn(true);
        when(validationService.isValidNumber(newKardex.getStockInvolved())).thenReturn(true);

        given(kardexService.saveKardex(Mockito.any(KardexEntity.class))).willReturn(newKardex);

        String kardexJson = objectMapper.writeValueAsString(newKardex);

        mockMvc.perform(post("/api/kardexes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(kardexJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(23)));
    }

    //Exceptions (bad requests or existing kardex)
    @Test
    public void createKardex_ExistingKardex_ShouldReturnConflict() throws Exception{
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

        KardexEntity newKardex = new KardexEntity(
                23L,
                KardexOperationType.PRESTAMO,
                LocalDate.of(2025, 5, 7),
                1,
                toolType);

        String kardexJson = objectMapper.writeValueAsString(newKardex);

        //Simulate that the kardex already exist
        when(kardexService.exists(newKardex.getId())).thenReturn(true);

        mockMvc.perform(post("/api/kardexes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(kardexJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("El kardex ya existe en la base de datos"));
    }

    @Test
    public void createKardex_InvalidOperationType_ShouldReturnBadRequest() throws Exception{
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

        KardexEntity newKardex = new KardexEntity(
                23L,
                KardexOperationType.PRESTAMO,
                LocalDate.of(2025, 5, 7),
                1,
                toolType);

        //Simulate that the kardex doesn't exist
        when(kardexService.exists(newKardex.getId())).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidOperationType(String.valueOf(newKardex.getOperationType()))).thenReturn(false);
        when(validationService.isValidDate(newKardex.getDate())).thenReturn(true);
        when(validationService.isValidNumber(newKardex.getStockInvolved())).thenReturn(true);

        String kardexJson = objectMapper.writeValueAsString(newKardex);

        mockMvc.perform(post("/api/kardexes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(kardexJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El tipo de operación es inválido"));
    }

    @Test
    public void createKardex_InvalidDate_ShouldReturnBadRequest() throws Exception{
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

        KardexEntity newKardex = new KardexEntity(
                23L,
                KardexOperationType.PRESTAMO,
                LocalDate.of(1543, 5, 15),
                1,
                toolType);

        //Simulate that the kardex doesn't exist
        when(kardexService.exists(newKardex.getId())).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidOperationType(String.valueOf(newKardex.getOperationType()))).thenReturn(true);
        when(validationService.isValidDate(newKardex.getDate())).thenReturn(false);
        when(validationService.isValidNumber(newKardex.getStockInvolved())).thenReturn(true);

        String kardexJson = objectMapper.writeValueAsString(newKardex);

        mockMvc.perform(post("/api/kardexes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(kardexJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La fecha es inválida"));
    }

    @Test
    public void createKardex_InvalidStockInvolved_ShouldReturnBadRequest() throws Exception{
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

        KardexEntity newKardex = new KardexEntity(
                23L,
                KardexOperationType.PRESTAMO,
                LocalDate.of(2025, 5, 7),
                -45,
                toolType);

        //Simulate that the kardex doesn't exist
        when(kardexService.exists(newKardex.getId())).thenReturn(false);

        //Simulates verifications
        when(validationService.isValidOperationType(String.valueOf(newKardex.getOperationType()))).thenReturn(true);
        when(validationService.isValidDate(newKardex.getDate())).thenReturn(true);
        when(validationService.isValidNumber(newKardex.getStockInvolved())).thenReturn(false);

        String kardexJson = objectMapper.writeValueAsString(newKardex);

        mockMvc.perform(post("/api/kardexes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(kardexJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El stock involucrado es inválido"));
    }


    //getAllKardex() tests
    //Normal flow case (success)
    @Test
    public void getAllKardex_ShouldReturnKardexList() throws Exception{
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

        KardexEntity newKardex1 = new KardexEntity(
                23L,
                KardexOperationType.PRESTAMO,
                LocalDate.of(2024,7,8),
                1,
                toolType);

        KardexEntity newKardex2 = new KardexEntity(
                52L,
                KardexOperationType.DEVOLUCION,
                LocalDate.of(2025, 3, 21),
                3,
                toolType);

        ArrayList<KardexEntity> kardexes = new ArrayList<>(Arrays.asList(newKardex1, newKardex2));

        given(kardexService.getAllKardex()).willReturn(kardexes);

        mockMvc.perform(get("/api/kardexes/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(23)))
                .andExpect(jsonPath("$[1].id", is(52)));
    }

    //Exception (empty list)
    @Test
    public void getAllKardex_ShouldReturnEmptyList() throws Exception {

        given(kardexService.getAllKardex()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/kardexes/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(content().json("[]"));
    }

    //getKardexByToolName() tests
    //Normal flow case (success)
    @Test
    public void getKardexByToolName_ShouldReturnKardexList() throws Exception{
        ToolTypeEntity toolType1 = new ToolTypeEntity(
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
                1L,
                "CRAFTSMAN Martillo, fibra de vidrio, 16 onzas",
                "Martillo",
                "CMHT51398",
                10000,
                24,
                14,
                3000,
                6000);

        KardexEntity newKardex1 = new KardexEntity(
                23L,
                KardexOperationType.PRESTAMO,
                LocalDate.of(2024,7,8),
                1,
                toolType1);

        KardexEntity newKardex2 = new KardexEntity(
                52L,
                KardexOperationType.DEVOLUCION,
                LocalDate.of(2025, 3, 21),
                3,
                toolType1);

        KardexEntity newKardex3 = new KardexEntity(
                105L,
                KardexOperationType.BAJA,
                LocalDate.of(2025, 6, 19),
                1,
                toolType2);

        List<KardexEntity> kardexes = Arrays.asList(newKardex1, newKardex2);

        //Simulates that the tool exist
        when(toolTypeService.getToolTypeByName(toolType1.getName())).thenReturn(Optional.of(toolType1));

        //Simulates the filtering
        when(kardexService.getKardexByToolTypeName(toolType1.getName())).thenReturn(Optional.of(kardexes));

        mockMvc.perform(get("/api/kardexes/tool/{toolName}", toolType1.getName())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(23)))
                .andExpect(jsonPath("$[1].id", is(52)));
    }

    //Exception (tool doesn't exist)
    @Test
    public void getKardexByToolName_ToolNotFound_ShouldReturnNotFound() throws Exception {
        //Simulates the search in the service and the RuntimeException
        when(toolTypeService.getToolTypeByName("Misil nuclear")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/kardexes/tool/{toolType1.getName()}", "Misil nuclear")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Herramienta no encontrada en la base de datos"));
    }

    //getKardexByDateRange() tests
    //Normal flow case (success)
    @Test
    public void getKardexByDateRange_ShouldReturnKardexList() throws Exception {
        KardexEntity newKardex1 = new KardexEntity(
                23L,
                KardexOperationType.PRESTAMO,
                LocalDate.of(2024,7,8),
                1,
                null);

        KardexEntity newKardex2 = new KardexEntity(
                52L,
                KardexOperationType.DEVOLUCION,
                LocalDate.of(2025, 3, 21),
                3,
                null);

        KardexEntity newKardex3 = new KardexEntity(
                105L,
                KardexOperationType.BAJA,
                LocalDate.of(2025, 6, 19),
                1,
                null);

        List<KardexEntity> kardexes = Arrays.asList(newKardex2,  newKardex3);

        //Definition of dates
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 9, 30);

        //Simulates the filtering
        when(kardexService.getKardexByDateRange(startDate, endDate)).thenReturn(kardexes);

        mockMvc.perform(get("/api/kardexes/by-date")
                        .param("start", startDate.toString())
                        .param("end", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(52)))
                .andExpect(jsonPath("$[1].id", is(105)));
    }

    //Exception (start date is after end date)
    @Test
    public void getKardexByDateRange_StartIsAfterEnd_ShouldReturnBadRequest() throws Exception{
        //Definition of dates
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 9, 30);

        mockMvc.perform(get("/api/kardexes/by-date")
                        .param("start", startDate.toString())
                        .param("end", endDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("La fecha de inicio es posterior a la de finalización"));
    }

    //deleteKardexById() tests
    //Normal flow case (success)
    @Test
    public void deleteKardexById_ShouldReturnNoContent() throws Exception {
        when(kardexService.deleteKardexById(23L)).thenReturn(true);

        mockMvc.perform(delete("/api/kardexes/kardex/23", 23L))
                .andExpect(status().isNoContent());
    }

    //Exception (kardex doesn't exist)
    @Test
    public void deleteKardexById_KardexNotFound_ShouldReturnNotFound() throws Exception {
        when(kardexService.deleteKardexById(23L)).thenReturn(false);

        mockMvc.perform(delete("/api/kardexes/kardex/23", 23L))
                .andExpect(status().isNotFound());
    }
}

