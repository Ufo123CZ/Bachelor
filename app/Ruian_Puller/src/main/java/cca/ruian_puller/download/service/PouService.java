package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.PouDto;
import cca.ruian_puller.download.repository.OrpRepository;
import cca.ruian_puller.download.repository.PouRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public void prepareAndSave(List<PouDto> pouDtos, int commitSize) {
        // Check all foreign keys
        int initialSize = pouDtos.size();
        pouDtos.removeIf(pouDto -> !checkFK(pouDto));
        if (initialSize != pouDtos.size()) {
            log.warn("{} removed from Pou due to missing foreign keys", initialSize - pouDtos.size());
        }

        // Split list of PouDto into smaller lists
        for (int i = 0; i < pouDtos.size(); i += commitSize) {
            int toIndex = Math.min(i + commitSize, pouDtos.size());
            List<PouDto> subList = pouDtos.subList(i, toIndex);
            pouRepository.saveAll(subList);
            log.info("Saved {} out of {} Pou", toIndex, pouDtos.size());
        }
    }


    private boolean checkFK(PouDto pouDto) {
        // Get the foreign key Kod
        Integer orpKod = pouDto.getOrp();

        // Check if the foreign key Kod exists
        if (!orpRepository.existsByKod(orpKod)) {
            log.warn("Pou with Kod {} does not have valid foreign keys: Orp with Kod {}", pouDto.getKod(), orpKod);
            return false;
        }
        return true;
    }
}
