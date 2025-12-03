package com.proyect.toolrent.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "loan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id" ,unique = true, nullable = false, updatable = false)
    private Long id;

    @Column(name = "loan_date", nullable = false)
    private LocalDate loanDate;

    @Column(name = "return_date", nullable = false)
    private LocalDate returnDate;

    @Column(name = "late_return_fee", nullable = false)
    private Integer lateReturnFee;

    //The initial value of the status is always "Activo"
    @Column(name = "status", nullable = false)
    private String status = "Activo";

    //The initial value of the state is always "Vigente"
    @Column(name = "validity", nullable = false)
    private String validity = "Vigente";

    @ManyToOne
    @JoinColumn(name = "client_run", referencedColumnName = "run")
    private ClientEntity client;

    @OneToMany(mappedBy = "loan")
    private List<LoanXToolItemEntity> loanTools;

    public String getValidity() {
        if ("Finalizado".equalsIgnoreCase(this.status)) {
            return this.validity;
        }

        if (this.returnDate != null && LocalDate.now().isAfter(this.returnDate)) {
            return "Atrasado";
        }

        return "Vigente";
    }
}
