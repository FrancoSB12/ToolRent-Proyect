package com.proyect.toolrent.Repositories;

import com.proyect.toolrent.Entities.LoanXToolItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface LoanXToolItemRepository extends JpaRepository<LoanXToolItemEntity, Long> {
    List<LoanXToolItemEntity> findByLoan_Id(Long loanId);

    boolean existsByLoan_Id(Long loanId);

    //Fetches the tool type corresponding to the tool item
    @Query("SELECT lxt FROM LoanXToolItemEntity lxt " +
            "JOIN FETCH lxt.toolItem ti " +
            "JOIN FETCH ti.toolType tt " +
            "WHERE lxt.loan.id = :loanId")
    List<LoanXToolItemEntity> findByLoanIdWithDetails(@Param("loanId") Long loanId);

    //Fetches for all IDs of tools that the client has on "Activo" loan
    @Query("SELECT DISTINCT ti.toolType.id " +
            "FROM LoanXToolItemEntity lxt " +
            "JOIN lxt.toolItem ti " +
            "JOIN lxt.loan l " +
            "WHERE l.client.run = :clientRun AND l.status = 'Activo'")
    Set<Long> findActiveLoansToolTypeIdsByClient(@Param("clientRun") String clientRun);

    //Find the lastest loan for a specific tool, sort by descending loan ID or by date (the highest is the newest)
    @Query("SELECT lxt FROM LoanXToolItemEntity lxt " +
            "WHERE lxt.toolItem.id = :toolId " +
            "ORDER BY lxt.loan.loanDate DESC")
    List<LoanXToolItemEntity> findHistoryByToolId(@Param("toolId") Long toolId);

    @Query("SELECT lxt.toolItem.toolType, COUNT(lxt) " +
            "FROM LoanXToolItemEntity lxt " +
            "WHERE lxt.loan.loanDate BETWEEN :start AND :end " +
            "GROUP BY lxt.toolItem.toolType " +
            "ORDER BY COUNT(lxt) DESC")
    List<Object[]> findMostLoanedToolsBetweenDates(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
