package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.OkresDto;
import cca.ruian_puller.download.dto.VuscDto;
import cca.ruian_puller.download.repository.OkresRepository;
import cca.ruian_puller.download.repository.VuscRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class OkresService {

    private final OkresRepository okresRepository;
    private final VuscRepository vuscRepository;

    @Autowired
    public OkresService(OkresRepository okresRepository, VuscRepository vuscRepository) {
        this.okresRepository = okresRepository;
        this.vuscRepository = vuscRepository;
    }

    public void save(OkresDto okresDto) {
        // Get the foreign key Kod
        Integer vuscKod = okresDto.getVusc();

        // Check if the foreign key Kod exists
        if (!vuscRepository.existsByKod(vuscKod)) {
            log.warn("Vusc with Kod {} does not exist", vuscKod);
            return;
        }

        // Save only if the Kod exists
        okresRepository.save(okresDto);
    }
}
