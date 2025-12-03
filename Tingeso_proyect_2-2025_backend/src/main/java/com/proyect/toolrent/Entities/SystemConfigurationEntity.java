package com.proyect.toolrent.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class SystemConfigurationEntity {
    //This entity manages the late return fee for the loan, so when the admin wants to update, loans that have already been made won't be affected

    @Id
    private Long id = 1L;

    @Column(name = "current_late_return_fee", nullable = false)
    private Integer currentLateReturnFee;
}
