package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.ParcelaDto;
import cca.ruian_puller.download.repository.KatastralniUzemiRepository;
import cca.ruian_puller.download.repository.ParcelaRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class ParcelaService {

    private final ParcelaRepository parcelaRepository;
    private final KatastralniUzemiRepository katastralniUzemiRepository;

    @Autowired
    public ParcelaService(ParcelaRepository parcelaRepository, KatastralniUzemiRepository katastralniUzemiRepository) {
        this.parcelaRepository = parcelaRepository;
        this.katastralniUzemiRepository = katastralniUzemiRepository;
    }

    public void prepareAndSave(List<ParcelaDto> parcelaDtos, int commitSize) {
        // Check all foreign keys
        int initialSize = parcelaDtos.size();
        parcelaDtos.removeIf(parcelaDto -> !checkFK(parcelaDto));
        if (initialSize != parcelaDtos.size()) {
            log.warn("{} removed from Parcela due to missing foreign keys", initialSize - parcelaDtos.size());
        }

        // Split list of ParcelaDto into smaller lists
        for (int i = 0; i < parcelaDtos.size(); i += commitSize) {
            int toIndex = Math.min(i + commitSize, parcelaDtos.size());
            List<ParcelaDto> subList = parcelaDtos.subList(i, toIndex);
            parcelaRepository.saveAll(subList);
            log.info("Saved {} out of {} Parcela", toIndex, parcelaDtos.size());
        }
    }

    private boolean checkFK(ParcelaDto parcelaDto) {
        // Get the foreign key Kod
        Integer katastralniUzemiKod = parcelaDto.getKatastralniuzemi();

        // Check if the foreign key Kod for KatastralniUzemi exists
        if (katastralniUzemiKod != null && !katastralniUzemiRepository.existsByKod(katastralniUzemiKod)) {
            log.warn("Parcela with Id {} does not have valid foreign keys: KatastralniUzemi with Kod {}", parcelaDto.getId(), katastralniUzemiKod);
            return false;
        }

        return true;
    }
}
