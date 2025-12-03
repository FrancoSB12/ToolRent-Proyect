package com.proyect.toolrent.DTO;

import com.proyect.toolrent.Entities.LoanEntity;
import com.proyect.toolrent.Entities.ToolItemEntity;
import lombok.Data;

@Data
public class DamageEvaluationRequest {
    private ToolItemEntity tool;
    private LoanEntity loan;
}
