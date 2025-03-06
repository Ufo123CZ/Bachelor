package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.RegionSoudrznostiDto;
import cca.ruian_puller.download.repository.StatRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cca.ruian_puller.download.repository.RegionSoudrznostiRepository;

import java.util.List;

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

    public void prepareAndSave(List<RegionSoudrznostiDto> regionSoudrznostiDtos, int comitSize) {
        // Check all foreign keys
        int initialSize = regionSoudrznostiDtos.size();
        regionSoudrznostiDtos.removeIf(regionSoudrznostiDto -> !checkFK(regionSoudrznostiDto));
        if (initialSize != regionSoudrznostiDtos.size()) {
            log.warn("{} removed from RegionSoudrznosti due to missing foreign keys", initialSize - regionSoudrznostiDtos.size());
        }

        // Split list of RegionSoudrznostiDto into smaller lists
        for (int i = 0; i < regionSoudrznostiDtos.size(); i += comitSize) {
            int toIndex = Math.min(i + comitSize, regionSoudrznostiDtos.size());
            List<RegionSoudrznostiDto> subList = regionSoudrznostiDtos.subList(i, toIndex);
            regionSoudrznostiRepository.saveAll(subList);
            log.info("Saved {} out of {} RegionSoudrznosti", toIndex, regionSoudrznostiDtos.size());
        }
    }

    private boolean checkFK(RegionSoudrznostiDto regionSoudrznostiDto) {
        // Get the foreign key Kod
        Integer statKod = regionSoudrznostiDto.getStat();

        // Check if the foreign key Kod exists
        if (!statRepository.existsByKod(statKod)) {
            log.warn("Stat with Kod {} does not exist", statKod);
            return false;
        }

        // Save only if the Kod exists
        return true;
    }
}
