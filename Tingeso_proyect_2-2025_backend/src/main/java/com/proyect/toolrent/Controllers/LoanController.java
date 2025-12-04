package com.proyect.toolrent.Controllers;

import com.proyect.toolrent.Entities.ClientEntity;
import com.proyect.toolrent.Entities.LoanEntity;
import com.proyect.toolrent.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin("*")
public class LoanController {
    private final LoanService loanService;
    private final ValidationService validationService;
    private final ClientService clientService;
    private final SystemConfigurationService sysConfigService;
    private final EmployeeService employeeService;

    @Autowired
    public LoanController(LoanService loanService, ValidationService validationService, ClientService clientService, SystemConfigurationService sysConfigService, EmployeeService employeeService) {
        this.loanService = loanService;
        this.validationService = validationService;
        this.clientService = clientService;
        this.sysConfigService = sysConfigService;
        this.employeeService = employeeService;
    }

    //Create loan
    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @PostMapping
    public ResponseEntity<?> createLoan(@RequestBody LoanEntity loan, Authentication authentication){
        //It's verified that the loan doesn't exist
        if(loan.getId() != null && loanService.exists(loan.getId())){
            return new ResponseEntity<>("El prestamo ya existe en la base de datos", HttpStatus.CONFLICT);
        }

        String currentEmployeeRun;
        //This is to get the employee run from the keycloak
        if (authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            currentEmployeeRun = jwt.getClaimAsString("preferred_username");
        } else {
            currentEmployeeRun = authentication.getName();
        }

        //It's verified that the client exist
        if(!clientService.exists(loan.getClient().getRun())){
            return new ResponseEntity<>("Cliente no encontrado en la base de datos", HttpStatus.NOT_FOUND);
        }

        //It's verified that the employee exist
        if(!employeeService.exists(currentEmployeeRun)){
            return new ResponseEntity<>("Empleado no encontrado en la base de datos", HttpStatus.NOT_FOUND);
        }

        //The data is validated for accuracy
        if(!validationService.isValidDate(loan.getLoanDate())){
            return new ResponseEntity<>("Fecha de arriendo incorrecta", HttpStatus.BAD_REQUEST);
        }

        if(!validationService.isValidReturnDate(loan.getReturnDate(), loan.getLoanDate())){
            return new ResponseEntity<>("Fecha de devolución incorrecta", HttpStatus.BAD_REQUEST);
        }

        try {
            LoanEntity newLoan = loanService.createLoan(loan, currentEmployeeRun);
            return new ResponseEntity<>(newLoan, HttpStatus.CREATED);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    //Get loan
    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/")
    public ResponseEntity<List<LoanEntity>> getAllLoans(){
        List<LoanEntity> loans = loanService.getAllLoans();
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/{id}")
    public ResponseEntity<LoanEntity> getLoanById(@PathVariable Long id){
        return loanService.getLoanById(id)
                .map(loan -> new ResponseEntity<>(loan, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/client/{run}")
    public ResponseEntity<?> getActiveLoansByClient(@PathVariable String run){
        //It's verified that the client exist
        if(!clientService.exists(run)){
            return new ResponseEntity<>("Cliente no encontrado en la base de datos", HttpStatus.NOT_FOUND);
        }

        List<LoanEntity> loans = loanService.getActiveLoansByClient(run);
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LoanEntity>> getLoanByStatus(@PathVariable String status){
        List<LoanEntity> loans = loanService.getLoanByStatus(status);
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/validity/{validity}")
    public ResponseEntity<List<LoanEntity>> getLoanByValidity(@PathVariable String validity){
        List<LoanEntity> loans = loanService.getLoanByValidity(validity);
        return new ResponseEntity<>(loans, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @GetMapping("/most-loaned-tools")
    public ResponseEntity<List<Map<String, Object>>> getMostLoanedTools(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end){
        return new ResponseEntity<>(loanService.getMostLoanedTools(start, end), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('Admin')")
    @GetMapping("/configuration/late-return-fee")
    public ResponseEntity<Integer> getCurrentLateFee() {
        return ResponseEntity.ok(sysConfigService.getLateReturnFee());
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @PutMapping("/return/{id}")
    public ResponseEntity<?> returnLoan(@PathVariable Long id, @RequestBody LoanEntity loan){
        try {
            //It's verified that the loan exist
            Optional<LoanEntity> existingLoan = loanService.getLoanById(id);
            if (existingLoan.isEmpty()) {
                return new ResponseEntity<>("El prestamo no existe en la base de datos", HttpStatus.NOT_FOUND);
            }

            //It's verified that the client exist
            if (!clientService.exists(loan.getClient().getRun())) {
                return new ResponseEntity<>("Cliente no encontrado en la base de datos", HttpStatus.NOT_FOUND);
            }

            //It's verified that the employee exist
            if(!employeeService.exists(loan.getEmployee().getRun())){
                return new ResponseEntity<>("Empleado no encontrado en la base de datos", HttpStatus.NOT_FOUND);
            }

            if (existingLoan.get().getStatus().equals("Finalizado")) {
                return new ResponseEntity<>("El prestamo ya está finalizado", HttpStatus.CONFLICT);
            }

            LoanEntity returnedLoan = loanService.returnLoan(id, loan);
            return new ResponseEntity<>(returnedLoan, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PreAuthorize("hasRole('Admin')")
    @PutMapping("/loan/{id}")
    public ResponseEntity<?> updateLateReturnFee(@PathVariable Long id, @RequestBody LoanEntity loan){
        try {
            //Verify that the loan has an id and the id isn't null
            if (loan.getId() == null) {
                return new ResponseEntity<>("El id no puede estar vacio o ser nulo", HttpStatus.BAD_REQUEST);
            }

            //Verify that the loan exist in the database
            if (loanService.getLoanById(id).isEmpty()) {
                return new ResponseEntity<>("El prestamo no existe en la base de datos", HttpStatus.NOT_FOUND);
            }

            LoanEntity updatedLoan = loanService.updateLateReturnFee(id, loan);
            return new ResponseEntity<>(updatedLoan, HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PreAuthorize("hasRole('Admin')")
    @PutMapping("/configuration/late-fee")
    public ResponseEntity<?> updateGlobalLateFee(@RequestParam Integer amount) {
        if (!validationService.isValidNumber(amount)) {
            return new ResponseEntity<>("El monto no puede ser negativo", HttpStatus.BAD_REQUEST);
        }

        sysConfigService.updateLateReturnFee(amount);
        return new ResponseEntity<>("Tarifa actualizada para futuros préstamos", HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('Employee','Admin')")
    @PutMapping("/update-late-statuses")
    public ResponseEntity<Void> checkAndSetLateStatuses(){
        loanService.checkAndSetLateStatuses();
        return ResponseEntity.noContent().build();
    }
}