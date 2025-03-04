package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.VODto;
import cca.ruian_puller.download.repository.MomcRepository;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.VORepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void save(VODto voDto) {
        // Get the foreign keys Kod
        Integer obecKod = voDto.getObec();
        Integer momcKod = voDto.getMomc();

        // Check if the foreign keys exist
        boolean somethingIsMissing = false;
        if (!obecRepository.existsByKod(obecKod)) {
            log.warn("Obec with Kod {} does not exist", obecKod);
            somethingIsMissing = true;
        }
        if (!momcRepository.existsByKod(momcKod)) {
            log.warn("Momc with Kod {} does not exist", momcKod);
            somethingIsMissing = true;
        }
        if (somethingIsMissing) return;

        // Save only if the Kod exists
        voRepository.save(voDto);
    }
}
