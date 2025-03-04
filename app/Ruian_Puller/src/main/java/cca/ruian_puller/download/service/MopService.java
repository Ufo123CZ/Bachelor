package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.MopDto;
import cca.ruian_puller.download.repository.MopRepository;
import cca.ruian_puller.download.repository.ObecRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class MopService {

    private final MopRepository mopRepository;
    private final ObecRepository obecRepository;

    @Autowired
    public MopService(MopRepository mopRepository, ObecRepository obecRepository) {
        this.mopRepository = mopRepository;
        this.obecRepository = obecRepository;
    }

    public void save(MopDto mopDto) {
        // Get the foreign key Kod
        Integer obecKod = mopDto.getObec();

        // Check if the foreign key Kod exists
        if (!obecRepository.existsByKod(obecKod)) {
            log.warn("Obec with Kod {} does not exist", obecKod);
            return;
        }

        // Save only if the Kod exists
        mopRepository.save(mopDto);
    }
}
