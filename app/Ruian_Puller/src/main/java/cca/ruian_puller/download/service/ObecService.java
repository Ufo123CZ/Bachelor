package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.ObecDto;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.OkresRepository;
import cca.ruian_puller.download.repository.PouRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class ObecService {

    private final ObecRepository obecRepository;
    private final OkresRepository okresRepository;
    private final PouRepository pouRepository;

    @Autowired
    public ObecService(ObecRepository obecRepository, OkresRepository okresRepository, PouRepository pouRepository) {
        this.obecRepository = obecRepository;
        this.okresRepository = okresRepository;
        this.pouRepository = pouRepository;
    }

    public void prepareAndSave(List<ObecDto> obecDtos, int commitSize) {
        // Check all foreign keys
        int initialSize = obecDtos.size();
        obecDtos.removeIf(obecDto -> !checkFK(obecDto));
        if (initialSize != obecDtos.size()) {
            log.warn("{} removed from Obec due to missing foreign keys", initialSize - obecDtos.size());
        }

        // Split list of ObecDto into smaller lists
        for (int i = 0; i < obecDtos.size(); i += commitSize) {
            int toIndex = Math.min(i + commitSize, obecDtos.size());
            List<ObecDto> subList = obecDtos.subList(i, toIndex);
            obecRepository.saveAll(subList);
            log.info("Saved {} out of {} Obec", toIndex, obecDtos.size());
        }
    }

    private boolean checkFK(ObecDto obecDto) {
        // Get the foreign keys Kod
        Integer okresKod = obecDto.getOkres();
        Integer pouKod = obecDto.getPou();

        // Check if the foreign key Kod exists
        if (!okresRepository.existsByKod(okresKod) && !pouRepository.existsByKod(pouKod)) {
            log.warn("Okres with Kod {} or Pou with Kod {} does not exist", okresKod, pouKod);
            return false;
        }
        return true;
    }
}
