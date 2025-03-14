package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.OkresDto;
import cca.ruian_puller.download.dto.VuscDto;
import cca.ruian_puller.download.repository.OkresRepository;
import cca.ruian_puller.download.repository.VuscRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class OkresService {

    private final OkresRepository okresRepository;
    private final VuscRepository vuscRepository;

    @Autowired
    public OkresService(OkresRepository okresRepository, VuscRepository vuscRepository) {
        this.okresRepository = okresRepository;
        this.vuscRepository = vuscRepository;
    }

    public void prepareAndSave(List<OkresDto> okresDtos, int commitSize) {
        // Check all foreign keys
        int initialSize = okresDtos.size();
        okresDtos.removeIf(okresDto -> !checkFK(okresDto));
        if (initialSize != okresDtos.size()) {
            log.warn("{} removed from Okres due to missing foreign keys", initialSize - okresDtos.size());
        }

        // Split list of OkresDto into smaller lists
        for (int i = 0; i < okresDtos.size(); i += commitSize) {
            int toIndex = Math.min(i + commitSize, okresDtos.size());
            List<OkresDto> subList = okresDtos.subList(i, toIndex);
            okresRepository.saveAll(subList);
            log.info("Saved {} out of {} Okres", toIndex, okresDtos.size());
        }
    }

    private boolean checkFK(OkresDto okresDto) {
        // Get the foreign key Kod
        Integer vuscKod = okresDto.getVusc();

        // Check if the foreign key Kod for Vusc exists
        if (vuscKod != null && !vuscRepository.existsByKod(vuscKod)) {
            log.warn("Okres with Kod {} does not have valid foreign keys: Vusc with Kod {}", okresDto.getKod(), vuscKod);
            return false;
        }

        return true;
    }
}
