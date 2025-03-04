package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.CastObceDto;
import cca.ruian_puller.download.repository.CastObceRepository;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.StavebniObjektRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class CastObceService {

    private final CastObceRepository castObceRepository;
    private final ObecRepository obecRepository;

    @Autowired
    public CastObceService(CastObceRepository castObceRepository, ObecRepository obecRepository) {
        this.castObceRepository = castObceRepository;
        this.obecRepository = obecRepository;
    }

    public void save(CastObceDto castObceDto) {
        // Get the foreign key Kod
        Integer obecKod = castObceDto.getObec();

        // Check if the foreign key Kod exists
        if (!obecRepository.existsById(obecKod)) {
            log.warn("Obec with Kod {} does not exist", obecKod);
            return;
        }

        // Save only if the Kod exists
        castObceRepository.save(castObceDto);
    }
}
