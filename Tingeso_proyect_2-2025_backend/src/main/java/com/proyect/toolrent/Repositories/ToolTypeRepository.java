package com.proyect.toolrent.Repositories;

import com.proyect.toolrent.Entities.ToolTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ToolTypeRepository extends JpaRepository<ToolTypeEntity, Long> {

    Optional<ToolTypeEntity> findByName(String name);
}
