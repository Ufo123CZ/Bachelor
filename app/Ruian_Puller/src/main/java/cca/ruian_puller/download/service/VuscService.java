package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.VuscDto;
import cca.ruian_puller.download.repository.RegionSoudrznostiRepository;
import cca.ruian_puller.download.repository.VuscRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class VuscService {

    private final VuscRepository vuscRepository;
    private final RegionSoudrznostiRepository regionSoudrznostiRepository;

    @Autowired
    public VuscService(VuscRepository vuscRepository, RegionSoudrznostiRepository regionSoudrznostiRepository) {
        this.vuscRepository = vuscRepository;
        this.regionSoudrznostiRepository = regionSoudrznostiRepository;
    }

    public void save(VuscDto vuscDto) {
        // Get the foreign keys Kod
        Integer regionSoudrznostiKod = vuscDto.getRegionsoudrznosti();

        // Check if the foreign keys exist
        if (!regionSoudrznostiRepository.existsByKod(regionSoudrznostiKod)) {
            log.warn("RegionSoudrznosti with Kod {} does not exist", regionSoudrznostiKod);
            return;
        }

        // Save only if the Kod exists
        vuscRepository.save(vuscDto);
    }
}
