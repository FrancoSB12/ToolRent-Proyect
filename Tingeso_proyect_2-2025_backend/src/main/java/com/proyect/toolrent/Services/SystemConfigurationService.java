package com.proyect.toolrent.Services;

import com.proyect.toolrent.Entities.SystemConfigurationEntity;
import com.proyect.toolrent.Repositories.SystemConfigurationRepositpry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemConfigurationService {
    private final SystemConfigurationRepositpry sysConfigRepository;

    @Autowired
    public SystemConfigurationService(SystemConfigurationRepositpry sysConfigRepository) {
        this.sysConfigRepository = sysConfigRepository;
    }

    private static final Integer DEFAULT_FEE = 2000;    //Default value in case the db is empty

    public Integer getLateReturnFee() {
        return sysConfigRepository.findById(1L)
                .map(SystemConfigurationEntity::getCurrentLateReturnFee)
                .orElse(DEFAULT_FEE);
    }

    @Transactional
    public void updateLateReturnFee(Integer newFee) {
        SystemConfigurationEntity config = sysConfigRepository.findById(1L)
                .orElse(new SystemConfigurationEntity());

        config.setId(1L); //Ensure that it is the ID of the fee
        config.setCurrentLateReturnFee(newFee);
        sysConfigRepository.save(config);
    }
}
