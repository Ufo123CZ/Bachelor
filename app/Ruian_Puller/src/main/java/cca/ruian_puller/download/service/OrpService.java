package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.OrpDto;
import cca.ruian_puller.download.repository.OrpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrpService {
    @Autowired
    private OrpRepository orpRepository;

    public void save(OrpDto orpDto) {
        orpRepository.save(orpDto);
    }
}
