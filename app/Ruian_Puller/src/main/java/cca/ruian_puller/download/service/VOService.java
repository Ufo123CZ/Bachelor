package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.VODto;
import cca.ruian_puller.download.repository.MomcRepository;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.VORepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class VOService {

    private final VORepository voRepository;
    private final ObecRepository obecRepository;
    private final MomcRepository momcRepository;

    @Autowired
    public VOService(VORepository voRepository, ObecRepository obecRepository, MomcRepository momcRepository) {
        this.voRepository = voRepository;
        this.obecRepository = obecRepository;
        this.momcRepository = momcRepository;
    }

    public void prepareAndSave(List<VODto> voDtos, int commitSize) {
        // Check all foreign keys
        int initialSize = voDtos.size();
        voDtos.removeIf(voDto -> !checkFK(voDto));
        if (initialSize != voDtos.size()) {
            log.warn("{} removed from VO due to missing foreign keys", initialSize - voDtos.size());
        }

        // Split list of VODto into smaller lists
        for (int i = 0; i < voDtos.size(); i += commitSize) {
            int toIndex = Math.min(i + commitSize, voDtos.size());
            List<VODto> subList = voDtos.subList(i, toIndex);
            voRepository.saveAll(subList);
            log.info("Saved {} out of {} VO", toIndex, voDtos.size());
        }
    }

    private boolean checkFK(VODto voDto) {
        // Get the foreign keys Kod
        Integer obecKod = voDto.getObec();
        Integer momcKod = voDto.getMomc();

        // Check if the foreign key Kod for Obec exists
        if (obecKod != null && !obecRepository.existsByKod(obecKod)) {
            log.warn("VO with Kod {} does not have a valid foreign key: Obec with Kod {}", voDto.getKod(), obecKod);
            return false;
        }

        // Check if the foreign key Kod for Momc exists
        if (momcKod != null && !momcRepository.existsByKod(momcKod)) {
            log.warn("VO with Kod {} does not have a valid foreign key: Momc with Kod {}", voDto.getKod(), momcKod);
            return false;
        }

        return true;
    }
}
