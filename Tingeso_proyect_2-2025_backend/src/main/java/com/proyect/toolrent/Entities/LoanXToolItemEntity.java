package com.proyect.toolrent.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loan_x_tool_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanXToolItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, updatable = false)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "loan_id", referencedColumnName = "id")
    private LoanEntity loan;

    @ManyToOne
    @JoinColumn(name = "tool_item_id", referencedColumnName = "id")
    private ToolItemEntity toolItem;

}
