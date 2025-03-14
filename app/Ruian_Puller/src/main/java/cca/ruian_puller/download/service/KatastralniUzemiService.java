package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.KatastralniUzemiDto;
import cca.ruian_puller.download.repository.KatastralniUzemiRepository;
import cca.ruian_puller.download.repository.ObecRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class KatastralniUzemiService {

    private final KatastralniUzemiRepository katastralniUzemiRepository;
    private final ObecRepository obecRepository;

    @Autowired
    public KatastralniUzemiService(KatastralniUzemiRepository katastralniUzemiRepository, ObecRepository obecRepository) {
        this.katastralniUzemiRepository = katastralniUzemiRepository;
        this.obecRepository = obecRepository;
    }

    public void prepareAndSave(List<KatastralniUzemiDto> katastralniUzemiDtos, int commitSize) {
        // Check all foreign keys
        int initialSize = katastralniUzemiDtos.size();
        katastralniUzemiDtos.removeIf(katastralniUzemiDto -> !checkFK(katastralniUzemiDto));
        if (initialSize != katastralniUzemiDtos.size()) {
            log.warn("{} removed from KatastralniUzemi due to missing foreign keys", initialSize - katastralniUzemiDtos.size());
        }

        // Split list of KatastralniUzemiDto into smaller lists
        for (int i = 0; i < katastralniUzemiDtos.size(); i += commitSize) {
            int toIndex = Math.min(i + commitSize, katastralniUzemiDtos.size());
            List<KatastralniUzemiDto> subList = katastralniUzemiDtos.subList(i, toIndex);
            katastralniUzemiRepository.saveAll(subList);
            log.info("Saved {} out of {} KatastralniUzemi", toIndex, katastralniUzemiDtos.size());
        }

    }

    public boolean checkFK(KatastralniUzemiDto katastralniUzemiDto) {
        // Get the foreign key Kod
        Integer obecKod = katastralniUzemiDto.getObec();

        // Check if the foreign key Kod for Obec is valid
        if (obecKod != null && !obecRepository.existsById(obecKod)) {
            log.warn("KatasralniUzemi with Kod {} does not have valid foreign keys: Obec with Kod {}", katastralniUzemiDto.getKod(), obecKod);
            return false;
        }

        return true;
    }
}
