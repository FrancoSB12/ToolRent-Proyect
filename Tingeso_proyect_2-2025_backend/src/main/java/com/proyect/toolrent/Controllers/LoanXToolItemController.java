package com.proyect.toolrent.Controllers;

import com.proyect.toolrent.Entities.LoanXToolItemEntity;
import com.proyect.toolrent.Services.LoanXToolItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-x-tool-items")
@CrossOrigin("*")
public class LoanXToolItemController {
    private final LoanXToolItemService loanXToolItemService;

    @Autowired
    public LoanXToolItemController(LoanXToolItemService loanXToolItemService) {
        this.loanXToolItemService = loanXToolItemService;
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<?> getAllLoanToolItems(@PathVariable Long loanId){
        //First, it's verified that the loan exist
        if(!loanXToolItemService.existsByLoanId(loanId)){
            return new ResponseEntity<>("El prestamo no existe en la base de datos", HttpStatus.NOT_FOUND);
        }

        List<LoanXToolItemEntity> loanXToolItems = loanXToolItemService.getAllLoanToolItemsByLoan_Id(loanId);

        return new ResponseEntity<>(loanXToolItems, HttpStatus.OK);
    }
}
