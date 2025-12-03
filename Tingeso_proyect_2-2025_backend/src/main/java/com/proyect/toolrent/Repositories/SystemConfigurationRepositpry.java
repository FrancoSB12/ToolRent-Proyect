package com.proyect.toolrent.Repositories;

import com.proyect.toolrent.Entities.SystemConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemConfigurationRepositpry extends JpaRepository<SystemConfigurationEntity, Long> {
}
