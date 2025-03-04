package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.SpravniObvodDto;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.SpravniObvodRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void save(SpravniObvodDto spravniObvodDto) {
        // Get the foreign key Kod
        Integer obecKod = spravniObvodDto.getObec();

        // Check if the foreign key Kod exists
        if (!obecRepository.existsByKod(obecKod)) {
            log.warn("Obec with Kod {} does not exist", obecKod);
            return;
        }

        // Save only if the Kod exists
        spravniObvodRepository.save(spravniObvodDto);
    }
}
