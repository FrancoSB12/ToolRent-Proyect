package com.proyect.toolrent.Controllers;

import com.proyect.toolrent.Entities.LoanEntity;
import com.proyect.toolrent.Entities.LoanXToolItemEntity;
import com.proyect.toolrent.Entities.ToolItemEntity;
import com.proyect.toolrent.Enums.ToolDamageLevel;
import com.proyect.toolrent.Enums.ToolStatus;
import com.proyect.toolrent.Services.LoanService;
import com.proyect.toolrent.Services.LoanXToolItemService;
import org.junit.jupiter.api.Test;
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

@WebMvcTest(controllers = LoanXToolItemController.class,
        excludeAutoConfiguration = { org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
public class LoanXToolControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanXToolItemService loanXToolService;

    @MockBean
    private LoanService loanService;

    //getAllLoanToolItems() tests
    //Normal flow case (success)
    @Test
    public void getAllLoanToolItems_ShouldReturnLoanXToolItemList() throws Exception{
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

        LoanEntity loan = new LoanEntity(
                13L,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                10000,
                "Activo",
                "Vigente",
                null,
                null,
                null);

        LoanXToolItemEntity loanXToolItem = new LoanXToolItemEntity(
                1L,
                loan,
                toolItem);

        LoanXToolItemEntity loanXToolItem2 = new LoanXToolItemEntity(
                2L,
                loan,
                toolItem2);

        List<LoanXToolItemEntity> loanToolsList = Arrays.asList(loanXToolItem, loanXToolItem2);

        when(loanXToolService.existsByLoanId(loan.getId())).thenReturn(true);

        given(loanXToolService.getAllLoanToolItemsByLoan_Id(loan.getId())).willReturn(loanToolsList);

        mockMvc.perform(get("/api/loan-x-tool-items/{loanId}", loan.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    //Exception (empty list and inexistent loan)
    @Test
    public void getAllLoanToolItems_NoLoanXToolItems_ShouldReturnEmptyList() throws Exception {
        Long loanId = 23L;

        when(loanXToolService.existsByLoanId(loanId)).thenReturn(true);

        when(loanService.getLoanById(loanId)).thenReturn(Optional.of(new LoanEntity(
                loanId,
                LocalDate.of(2025, 5, 10),
                LocalDate.of(2025, 5, 24),
                10000,
                "Activo",
                "Vigente",
                null,
                null,
                null
        )));

        given(loanXToolService.getAllLoanToolItemsByLoan_Id(loanId)).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/loan-x-tool-items/{loanId}", loanId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(content().json("[]"));
    }

    @Test
    public void getAllLoanToolItems_LoanDoesntExist_ShouldReturnNotFound() throws Exception {
        Long loanId = 23L;

        when(loanXToolService.existsByLoanId(loanId)).thenReturn(false);

        mockMvc.perform(get("/api/loan-x-tool-items/{loanId}", loanId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("El prestamo no existe en la base de datos"));
    }
}
