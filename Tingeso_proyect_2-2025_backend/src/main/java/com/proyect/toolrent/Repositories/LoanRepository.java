package com.proyect.toolrent.Repositories;

import com.proyect.toolrent.Entities.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<LoanEntity, Long> {

    //Search for a loan by id and return it with all the data including toolItem and toolType
    @Query("SELECT l FROM LoanEntity l " +
            "JOIN FETCH l.client c " +
            "JOIN FETCH l.employee e " +
            "LEFT JOIN FETCH l.loanTools lt " +
            "LEFT JOIN FETCH lt.toolItem ti " +
            "LEFT JOIN FETCH ti.toolType tt " +
            "WHERE l.id = :id")
    Optional<LoanEntity> findByIdWithDetails(@Param("id") Long id);

    //Search for active loans
    @Query("SELECT DISTINCT l FROM LoanEntity l " +
            "LEFT JOIN FETCH l.loanTools lt " +
            "LEFT JOIN FETCH lt.toolItem ti " +
            "LEFT JOIN FETCH ti.toolType tt " +
            "WHERE l.client.run = :run " +
            "AND l.status = 'Activo' " +
            "ORDER BY l.loanDate ASC")
    List<LoanEntity> findActiveLoansByClient(@Param("run") String clientRun);

    //Search for overdue loans ("Activo" status and past due)
    @Query("SELECT l FROM LoanEntity l " +
            "WHERE l.client.run = :clientRun " +
            "AND l.status = 'Activo' " +
            "AND l.returnDate < :today")
    List<LoanEntity> findOverdueLoans(@Param("clientRun") String clientRun,
                                      @Param("today") LocalDate today);

    @Query("SELECT l FROM LoanEntity l JOIN FETCH l.client c JOIN FETCH l.employee e LEFT JOIN FETCH l.loanTools lt")
    List<LoanEntity> findAllWithDetails();

    @Query("SELECT l FROM LoanEntity l JOIN FETCH l.client c JOIN FETCH l.employee e LEFT JOIN FETCH l.loanTools lt WHERE l.status = :status")
    List<LoanEntity> findByStatusWithDetails(@Param("status") String status);

    List<LoanEntity> findByValidity(String validity);

}
