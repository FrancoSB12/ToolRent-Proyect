package com.proyect.toolrent.Repositories;

import com.proyect.toolrent.Entities.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeEntity, String> {

    public List<EmployeeEntity> findByIsAdmin(boolean isAdmin);
}
