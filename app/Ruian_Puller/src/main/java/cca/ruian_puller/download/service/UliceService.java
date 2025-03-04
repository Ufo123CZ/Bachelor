package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.UliceDto;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.UliceRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void save(UliceDto uliceDto) {
        // Get the foreign key Kod
        Integer obecKod = uliceDto.getObec();

        // Check if the foreign key Kod exists
        if (!obecRepository.existsByKod(obecKod)) {
            log.warn("Obec with Kod {} does not exist", obecKod);
            return;
        }

        // Save only if the Kod exists
        uliceRepository.save(uliceDto);
    }
}
