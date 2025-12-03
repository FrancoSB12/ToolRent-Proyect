package com.proyect.toolrent.Repositories;

import com.proyect.toolrent.Entities.ToolItemEntity;
import com.proyect.toolrent.Enums.ToolDamageLevel;
import com.proyect.toolrent.Enums.ToolStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToolItemRepository extends JpaRepository<ToolItemEntity, Long> {

    Optional<ToolItemEntity> findBySerialNumber(String serialNumber);

    boolean existsBySerialNumber(String serialNumber);

    Optional<ToolItemEntity> findFirstByToolType_IdAndStatusAndDamageLevelIn(Long toolTypeId, ToolStatus status, List<ToolDamageLevel> damageLevels);
}
