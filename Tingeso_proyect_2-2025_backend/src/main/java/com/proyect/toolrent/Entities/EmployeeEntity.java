package com.proyect.toolrent.Entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEntity {

    @Id
    @Column(name = "run", length = 12, nullable = false, unique = true, updatable = false)    //The lenght is 10 because the hyphen also counts
    private String run;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "cellphone", nullable = false)
    private String cellphone;

    @JsonProperty("isAdmin")
    @Column(name = "isAdmin", nullable = false)
    private boolean isAdmin;

}
