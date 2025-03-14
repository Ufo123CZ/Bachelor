package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.UliceDto;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.UliceRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class UliceService {

    private final UliceRepository uliceRepository;
    private final ObecRepository obecRepository;

    @Autowired
    public UliceService(UliceRepository uliceRepository, ObecRepository obecRepository) {
        this.uliceRepository = uliceRepository;
        this.obecRepository = obecRepository;
    }

    public void prepareAndSave(List<UliceDto> uliceDtos, int commitSize) {
        // Check all foreign keys
        int initialSize = uliceDtos.size();
        uliceDtos.removeIf(uliceDto -> !checkFK(uliceDto));
        if (initialSize != uliceDtos.size()) {
            log.warn("{} removed from Ulice due to missing foreign keys", initialSize - uliceDtos.size());
        }

        // Split list of UliceDto into smaller lists
        for (int i = 0; i < uliceDtos.size(); i += commitSize) {
            int toIndex = Math.min(i + commitSize, uliceDtos.size());
            List<UliceDto> subList = uliceDtos.subList(i, toIndex);
            uliceRepository.saveAll(subList);
            log.info("Saved {} out of {} Ulice", toIndex, uliceDtos.size());
        }
    }

    private boolean checkFK(UliceDto uliceDto) {
        // Get the foreign key Kod
        Integer obecKod = uliceDto.getObec();

        // Check if the foreign key Kod for Obec is valid
        if (obecKod != null && !obecRepository.existsByKod(obecKod)) {
            log.warn("Ulice with Kod {} does not have valid foreign keys: Obec with Kod {}", uliceDto.getKod(), obecKod);
            return false;
        }

        return true;
    }
}
