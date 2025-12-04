package com.proyect.toolrent.Services;

import com.proyect.toolrent.Entities.LoanXToolItemEntity;
import com.proyect.toolrent.Entities.ToolItemEntity;
import com.proyect.toolrent.Entities.ToolTypeEntity;
import com.proyect.toolrent.Enums.ToolDamageLevel;
import com.proyect.toolrent.Enums.ToolStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ValidationServiceTest {

    LoanXToolItemService loanXToolItemService = mock(LoanXToolItemService.class);

    ValidationService validationService = new ValidationService(loanXToolItemService);

    //hasMaliciousQuery() tests
    //Normal flow case (success)
    @Test
    void whenHasMaliciousQuery_thenReturnsTrue() {
        assertThat(validationService.hasMaliciousQuery("SELECT * FROM users")).isTrue();
        assertThat(validationService.hasMaliciousQuery("DROP TABLE clients")).isTrue();
        assertThat(validationService.hasMaliciousQuery("admin --")).isTrue();
        assertThat(validationService.hasMaliciousQuery("1; DROP DATABASE")).isTrue();
    }

    //Exception (no malicious query)
    @Test
    void whenHasSafeQuery_thenReturnsFalse() {
        assertThat(validationService.hasMaliciousQuery("Juan Perez")).isFalse();
        assertThat(validationService.hasMaliciousQuery("tool-box-123")).isFalse();
    }

    //isValidRun() tests
    //Normal flow case (success)
    @Test
    void whenIsValidRun_thenReturnsTrue() {
        assertThat(validationService.isValidRun("12.345.678-9")).isTrue();
        assertThat(validationService.isValidRun("123456789")).isTrue();
    }

    //Exception (invalid run)
    @Test
    void whenIsValidRunInvalid_thenReturnsFalse() {
        assertThat(validationService.isValidRun(null)).isFalse();
        assertThat(validationService.isValidRun("")).isFalse();
        assertThat(validationService.isValidRun("1; DROP")).isFalse(); //Malicious
    }

    //isValidName() tests
    //Normal flow case (success)
    @Test
    void whenIsValidName_thenReturnsTrue() {
        assertThat(validationService.isValidName("Juan")).isTrue();
        assertThat(validationService.isValidName("María José")).isTrue();
    }

    //Exception (invalid name)
    @Test
    void whenIsValidNameInvalid_thenReturnsFalse() {
        assertThat(validationService.isValidName("Juan123")).isFalse(); //Numbers are not permitted
        assertThat(validationService.isValidName("Juan; DROP")).isFalse();
        assertThat(validationService.isValidName(null)).isFalse();
    }

    //isValidEmail() tests
    //Normal flow case (success)
    @Test
    void whenIsValidEmail_thenReturnsTrue() {
        assertThat(validationService.isValidEmail("test@example.com")).isTrue();
        assertThat(validationService.isValidEmail("user.name@domain.cl")).isTrue();
    }

    //Exception (invalid email)
    @Test
    void whenIsValidEmailInvalid_thenReturnsFalse() {
        assertThat(validationService.isValidEmail("testexample.com")).isFalse(); //@ missing
        assertThat(validationService.isValidEmail("test@.com")).isFalse();
        assertThat(validationService.isValidEmail("test@com")).isFalse(); //Dot missing
    }

    //isValidCellphone() tests
    //Normal flow case (success)
    @Test
    void whenIsValidCellphone_thenReturnsTrue() {
        // Probamos los formatos que reformatCellphone debería arreglar
        assertThat(validationService.isValidCellphone("+56912345678")).isTrue();
        assertThat(validationService.isValidCellphone("912345678")).isTrue();
        assertThat(validationService.isValidCellphone("0912345678")).isTrue();
    }

    //Exception (invalid cellphone)
    @Test
    void whenIsValidCellphoneInvalid_thenReturnsFalse() {
        assertThat(validationService.isValidCellphone("123456")).isFalse();
        assertThat(validationService.isValidCellphone("abc")).isFalse();
        assertThat(validationService.isValidCellphone(null)).isFalse();
    }

    //isValidStatus() tests
    //Normal flow case (success)
    @Test
    void whenIsValidStatus_thenReturnsTrue() {
        assertThat(validationService.isValidStatus("Activo")).isTrue();
        assertThat(validationService.isValidStatus("restringido")).isTrue(); // Case insensitive
    }

    //Exception (invalid status)
    @Test
    void whenIsValidStatusInvalid_thenReturnsFalse() {
        assertThat(validationService.isValidStatus("Inactivo")).isFalse();
        assertThat(validationService.isValidStatus("")).isFalse();
    }

    //isValidNumber() tests
    //Normal flow case (success)
    @Test
    void whenIsValidNumber_thenReturnsTrue() {
        assertThat(validationService.isValidNumber(0)).isTrue();
        assertThat(validationService.isValidNumber(100)).isTrue();
    }

    //Exception (invalid number)
    @Test
    void whenIsValidNumberInvalid_thenReturnsFalse() {
        assertThat(validationService.isValidNumber(-1)).isFalse();
        assertThat(validationService.isValidNumber(null)).isFalse();
    }

    //isValidToolStatus() tests
    //Normal flow case (success)
    @Test
    void whenIsValidToolStatus_thenReturnsTrue() {
        assertThat(validationService.isValidToolStatus("Disponible")).isTrue();
        assertThat(validationService.isValidToolStatus("EN_REPARACION")).isTrue();
    }

    //Exception (invalid status)
    @Test
    void whenIsValidToolStatusInvalid_thenReturnsFalse() {
        assertThat(validationService.isValidToolStatus("Perdida")).isFalse();
    }

    //isValidDamageLevel() tests
    //Normal flow case (success)
    @Test
    void whenIsValidDamageLevel_thenReturnsTrue() {
        assertThat(validationService.isValidDamageLevel("NO_DANADA")).isTrue();
        assertThat(validationService.isValidDamageLevel("IRREPARABLE")).isTrue();
    }

    //Exception (invalid tool damage)
    @Test
    void whenIsValidDamageLevelInvalid_thenReturnsFalse() {
        assertThat(validationService.isValidToolStatus("Doblada")).isFalse();
    }

    //isValidOperationType() tests
    //Normal flow case (success)
    @Test
    void whenIsValidOperationType_thenReturnsTrue() {
        assertThat(validationService.isValidOperationType("Prestamo")).isTrue();
        assertThat(validationService.isValidOperationType("Devolucion")).isTrue();
    }

    //Exception (invalid kardex operation type)
    @Test
    void whenIsValidOperationTypeInvalid_thenReturnsFalse() {
        assertThat(validationService.isValidOperationType("Reconexion")).isFalse();
    }

    //isValidDate() tests
    //Normal flow case (success)
    @Test
    void whenIsValidDate_thenReturnsTrue() {
        assertThat(validationService.isValidDate(LocalDate.now())).isTrue();
        assertThat(validationService.isValidDate(LocalDate.now().plusDays(1))).isTrue();
    }

    //Exception (bad request)
    @Test
    void whenIsValidDatePast_thenReturnsFalse() {
        assertThat(validationService.isValidDate(LocalDate.now().minusDays(1))).isFalse();
    }

    //isValidReturnDate() tests
    //Normal flow case (success)
    @Test
    void whenIsValidReturnDate_thenReturnsTrue() {
        LocalDate loanDate = LocalDate.now();
        LocalDate returnDate = LocalDate.now().plusDays(1);
        assertThat(validationService.isValidReturnDate(returnDate, loanDate)).isTrue();
    }

    //Exception (bad request)
    @Test
    void whenIsValidReturnDateInvalid_thenReturnsFalse() {
        LocalDate loanDate = LocalDate.now();
        LocalDate returnDate = LocalDate.now().minusDays(1);
        assertThat(validationService.isValidReturnDate(returnDate, loanDate)).isFalse();
        assertThat(validationService.isValidReturnDate(loanDate, loanDate)).isFalse();
    }

    //areToolsInStockOrAvailable() tests
    //Normal flow case (success)
    @Test
    void whenAreToolsInStockOrAvailable_NoErrors_StringBuilderIsEmpty() {
        // Given
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
                ToolDamageLevel.NO_DANADA,
                toolType);

        LoanXToolItemEntity loanToolItem = new LoanXToolItemEntity();
        loanToolItem.setToolItem(toolItem);
        List<LoanXToolItemEntity> list = List.of(loanToolItem);

        StringBuilder sb = new StringBuilder();

        when(loanXToolItemService.getAvailableStock(toolItem)).thenReturn(5);

        // When
        validationService.areToolsInStockOrAvailable(list, sb);

        // Then
        assertThat(sb.toString()).isEmpty();
    }

    //Exceptions (no tools or unavailable)
    @Test
    void whenAreToolsInStockOrAvailable_NoStock_AppendsError() {
        // Given
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
                ToolDamageLevel.NO_DANADA,
                toolType);

        LoanXToolItemEntity loanToolItem = new LoanXToolItemEntity();
        loanToolItem.setToolItem(toolItem);
        List<LoanXToolItemEntity> list = List.of(loanToolItem);

        StringBuilder sb = new StringBuilder();

        // Mock: Stock 0
        when(loanXToolItemService.getAvailableStock(toolItem)).thenReturn(0);

        // When
        validationService.areToolsInStockOrAvailable(list, sb);

        // Then
        assertThat(sb.toString()).contains("Destornillador Phillips 2*150mm");
    }

    @Test
    void whenAreToolsInStockOrAvailable_NotDisponible_AppendsError() {
        // Given
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
                ToolStatus.PRESTADA,
                ToolDamageLevel.NO_DANADA,
                toolType);

        LoanXToolItemEntity loanToolItem = new LoanXToolItemEntity();
        loanToolItem.setToolItem(toolItem);
        List<LoanXToolItemEntity> list = List.of(loanToolItem);

        StringBuilder sb = new StringBuilder();

        // Mock: Stock > 0 (aunque haya stock, la unidad específica no está disponible)
        when(loanXToolItemService.getAvailableStock(toolItem)).thenReturn(5);

        // When
        validationService.areToolsInStockOrAvailable(list, sb);

        // Then
        assertThat(sb.toString()).contains("Destornillador Phillips 2*150mm");
    }
}