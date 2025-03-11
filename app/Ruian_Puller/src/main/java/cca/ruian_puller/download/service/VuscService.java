package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.VuscDto;
import cca.ruian_puller.download.repository.RegionSoudrznostiRepository;
import cca.ruian_puller.download.repository.VuscRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class VuscService {

    private final VuscRepository vuscRepository;
    private final RegionSoudrznostiRepository regionSoudrznostiRepository;

    @Autowired
    public VuscService(VuscRepository vuscRepository, RegionSoudrznostiRepository regionSoudrznostiRepository) {
        this.vuscRepository = vuscRepository;
        this.regionSoudrznostiRepository = regionSoudrznostiRepository;
    }

    public void prepareAndSave(List<VuscDto> vuscDtos, int commitSize) {
        // Check all foreign keys
        int initialSize = vuscDtos.size();
        vuscDtos.removeIf(vuscDto -> !checkFK(vuscDto));
        if (initialSize != vuscDtos.size()) {
            log.warn("{} removed from Vusc due to missing foreign keys", initialSize - vuscDtos.size());
        }

        // Split list of VuscDto into smaller lists
        for (int i = 0; i < vuscDtos.size(); i += commitSize) {
            int toIndex = Math.min(i + commitSize, vuscDtos.size());
            List<VuscDto> subList = vuscDtos.subList(i, toIndex);
            vuscRepository.saveAll(subList);
            log.info("Saved {} out of {} Vusc", toIndex, vuscDtos.size());
        }
    }

    private boolean checkFK(VuscDto vuscDto) {
        // Get the foreign keys Kod
        Integer regionSoudrznostiKod = vuscDto.getRegionsoudrznosti();

        // Check if the foreign keys exist
        if (!regionSoudrznostiRepository.existsByKod(regionSoudrznostiKod)) {
            log.warn("Vusc with Kod {} does not have valid foreign keys: RegionSoudrznosti with Kod {}", vuscDto.getKod(), regionSoudrznostiKod);
            return false;
        }
        return true;
    }
}
