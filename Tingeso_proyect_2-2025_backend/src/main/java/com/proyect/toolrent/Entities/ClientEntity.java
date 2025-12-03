package com.proyect.toolrent.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "client")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientEntity {

    @Id
    @Column(name = "run", length = 12, nullable = false, unique = true, updatable = false)    //The lenght is 10 because the hyphen also counts
    private String run;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "cellphone", nullable = false, length = 15)
    private String cellphone;

    //The initial value of the status is always "Activo"
    @Column(name = "status", nullable = false)
    private String status = "Activo";

    //The initial value of the debt is always 0
    @Column(name = "debt", nullable = false)
    private Integer debt = 0;

    //The initial value of the active loans is always 0
    @Column(name = "active_loans", nullable = false)
    private Integer activeLoans = 0;
}
