package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.OrpDto;
import cca.ruian_puller.download.repository.OkresRepository;
import cca.ruian_puller.download.repository.OrpRepository;
import cca.ruian_puller.download.repository.VuscRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class OrpService {

    private final OrpRepository orpRepository;
    private final VuscRepository vuscRepository;
    private final OkresRepository okresRepository;

    @Autowired
    public OrpService(OrpRepository orpRepository, VuscRepository vuscRepository, OkresRepository okresRepository) {
        this.orpRepository = orpRepository;
        this.vuscRepository = vuscRepository;
        this.okresRepository = okresRepository;
    }

    public void save(OrpDto orpDto) {
        // Get the foreign keys Kod
        Integer vuscKod = orpDto.getVusc();
        Integer okresKod = orpDto.getOkres();

        // Check if the foreign keys Kod exist
        if (!vuscRepository.existsByKod(vuscKod) && !okresRepository.existsByKod(okresKod)) {
            log.warn("Vusc with Kod {} or Okres with Kod {} does not exist", vuscKod, okresKod);
            return;
        }

        // Save only if the Kod exists
        orpRepository.save(orpDto);
    }
}
