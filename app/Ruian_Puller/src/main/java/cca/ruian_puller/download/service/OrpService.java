package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.OrpDto;
import cca.ruian_puller.download.repository.OkresRepository;
import cca.ruian_puller.download.repository.OrpRepository;
import cca.ruian_puller.download.repository.VuscRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public void prepareAndSave(List<OrpDto> orpDtos, int commitSize) {
        // Check all foreign keys
        int initialSize = orpDtos.size();
        orpDtos.removeIf(orpDto -> !checkBF(orpDto));
        if (initialSize != orpDtos.size()) {
            log.warn("{} removed from Orp due to missing foreign keys", initialSize - orpDtos.size());
        }

        // Split list of OrpDto into smaller lists
        for (int i = 0; i < orpDtos.size(); i += commitSize) {
            int toIndex = Math.min(i + commitSize, orpDtos.size());
            List<OrpDto> subList = orpDtos.subList(i, toIndex);
            orpRepository.saveAll(subList);
            log.info("Saved {} out of {} Orp", toIndex, orpDtos.size());
        }
    }

    private boolean checkBF(OrpDto orpDto) {
        // Get the foreign keys Kod
        Integer vuscKod = orpDto.getVusc();
        Integer okresKod = orpDto.getOkres();

        // Check if the foreign keys Kod exist
        if (!vuscRepository.existsByKod(vuscKod) && !okresRepository.existsByKod(okresKod)) {
            log.warn("Orp with Kod {} does not have valid foreign keys: Vusc with Kod {}, Okres with Kod {}", orpDto.getKod(), vuscKod, okresKod);
            return false;
        }
        return true;
    }
}
