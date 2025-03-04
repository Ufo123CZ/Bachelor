package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.PouDto;
import cca.ruian_puller.download.repository.OrpRepository;
import cca.ruian_puller.download.repository.PouRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class PouService {

    private final PouRepository pouRepository;
    private final OrpRepository orpRepository;

    @Autowired
    public PouService(PouRepository pouRepository, OrpRepository orpRepository) {
        this.pouRepository = pouRepository;
        this.orpRepository = orpRepository;
    }

    public void save(PouDto pouDto) {
        // Get the foreign key Kod
        Integer orpKod = pouDto.getOrp();

        // Check if the foreign key Kod exists
        if (!orpRepository.existsByKod(orpKod)) {
            log.warn("Orp with Kod {} does not exist", orpKod);
            return;
        }

        // Save only if the Kod exists
        pouRepository.save(pouDto);
    }
}
