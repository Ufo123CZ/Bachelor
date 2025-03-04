package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.RegionSoudrznostiDto;
import cca.ruian_puller.download.repository.StatRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cca.ruian_puller.download.repository.RegionSoudrznostiRepository;

@Service
@Log4j2
public class RegionSoudrznostiService {

    private final RegionSoudrznostiRepository regionSoudrznostiRepository;
    private final StatRepository statRepository;

    @Autowired
    public RegionSoudrznostiService(RegionSoudrznostiRepository regionSoudrznostiRepository, StatRepository statRepository) {
        this.regionSoudrznostiRepository = regionSoudrznostiRepository;
        this.statRepository = statRepository;
    }

    public void save(RegionSoudrznostiDto regionSoudrznostiDto) {
        // Get the foreign key Kod
        Integer statKod = regionSoudrznostiDto.getStat();

        // Check if the foreign key Kod exists
        if (!statRepository.existsByKod(statKod)) {
            log.warn("Stat with Kod {} does not exist", statKod);
            return;
        }

        // Save only if the Kod exists
        regionSoudrznostiRepository.save(regionSoudrznostiDto);
    }
}
