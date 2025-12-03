package com.proyect.toolrent.Services;

import com.proyect.toolrent.Entities.LoanXToolItemEntity;
import com.proyect.toolrent.Entities.ToolItemEntity;
import com.proyect.toolrent.Repositories.LoanXToolItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class LoanXToolItemService {
    private final LoanXToolItemRepository loanXToolItemRepository;

    @Autowired
    public LoanXToolItemService(LoanXToolItemRepository loanXToolItemRepository){
        this.loanXToolItemRepository = loanXToolItemRepository;
    }

    //List all loan tool types
    public List<LoanXToolItemEntity> getAllLoanToolItemsByLoan_Id(Long loanId){
        return loanXToolItemRepository.findByLoan_Id(loanId);
    }

    public List<LoanXToolItemEntity> getLoanToolItemsWithToolTypesByLoan_Id(Long loanId){
        return loanXToolItemRepository.findByLoanIdWithDetails(loanId);
    }

    public Set<Long> getActiveLoansToolTypeIdsByClient(String clientId){
        return loanXToolItemRepository.findActiveLoansToolTypeIdsByClient(clientId);
    }

    public List<LoanXToolItemEntity> getHistoryByToolId(Long toolId){
        return loanXToolItemRepository.findHistoryByToolId(toolId);
    }

    public List<Object[]> getMostLoanedToolsBetweenDates(LocalDate start,  LocalDate end){
        return loanXToolItemRepository.findMostLoanedToolsBetweenDates(start, end);
    }

    public LoanXToolItemEntity save(LoanXToolItemEntity loanXToolItemEntity){
        return loanXToolItemRepository.save(loanXToolItemEntity);
    }

    public Integer getAvailableStock(ToolItemEntity toolItem){
        if(toolItem != null && toolItem.getToolType() != null){
            return toolItem.getToolType().getAvailableStock();
        }
        return null;
    }

    public boolean existsByLoanId(Long loanId){
        return loanXToolItemRepository.existsByLoan_Id(loanId);
    }

}
