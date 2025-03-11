package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.SpravniObvodDto;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.SpravniObvodRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class SpravniObvodService {

    private final SpravniObvodRepository spravniObvodRepository;
    private final ObecRepository obecRepository;

    @Autowired
    public SpravniObvodService(SpravniObvodRepository spravniObvodRepository, ObecRepository obecRepository) {
        this.spravniObvodRepository = spravniObvodRepository;
        this.obecRepository = obecRepository;
    }

    public void prepareAndSave(List<SpravniObvodDto> spravniObvodDtos, int commitSize) {
        // Check all foreign keys
        int initialSize = spravniObvodDtos.size();
        spravniObvodDtos.removeIf(spravniObvodDto -> !checkFK(spravniObvodDto));
        if (initialSize != spravniObvodDtos.size()) {
            log.warn("{} removed from SpravniObvod due to missing foreign keys", initialSize - spravniObvodDtos.size());
        }

        // Split list of SpravniObvodDto into smaller lists
        for (int i = 0; i < spravniObvodDtos.size(); i += commitSize) {
            int toIndex = Math.min(i + commitSize, spravniObvodDtos.size());
            List<SpravniObvodDto> subList = spravniObvodDtos.subList(i, toIndex);
            spravniObvodRepository.saveAll(subList);
            log.info("Saved {} out of {} SpravniObvod", toIndex, spravniObvodDtos.size());
        }
    }

    private boolean checkFK(SpravniObvodDto spravniObvodDto) {
        // Get the foreign key Kod
        Integer obecKod = spravniObvodDto.getObec();

        // Check if the foreign key Kod exists
        if (!obecRepository.existsByKod(obecKod)) {
            log.warn("SpravniObvod with Kod {} does not have valid foreign keys: Obec with Kod {}", spravniObvodDto.getKod(), obecKod);
            return false;
        }
        return true;
    }
}
