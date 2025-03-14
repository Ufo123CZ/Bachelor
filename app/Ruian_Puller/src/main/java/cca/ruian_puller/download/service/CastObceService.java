package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.CastObceDto;
import cca.ruian_puller.download.repository.CastObceRepository;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.StavebniObjektRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public void prepareAndSave(List<CastObceDto> castObceDtos, int commitSize) {
        // Check all foreign keys
        int initialSize = castObceDtos.size();
        castObceDtos.removeIf(castObceDto -> !checkFK(castObceDto));
        if (initialSize != castObceDtos.size()) {
            log.warn("{} removed from CastObce due to missing foreign keys", initialSize - castObceDtos.size());
        }

        // Split list of CastObceDto into smaller lists
        for (int i = 0; i < castObceDtos.size(); i += commitSize) {
            int toIndex = Math.min(i + commitSize, castObceDtos.size());
            List<CastObceDto> subList = castObceDtos.subList(i, toIndex);
            castObceRepository.saveAll(subList);
            log.info("Saved {} out of {} CastObce", toIndex, castObceDtos.size());
        }
    }

    public boolean checkFK(CastObceDto castObceDto) {
        // Get the foreign key Kod
        Integer obecKod = castObceDto.getObec();

        // Check if the foreign key Kod for Obec is valid
        if (obecKod != null && !obecRepository.existsById(obecKod)) {
            log.warn("Obec with Kod {} does not have valid foreign keys: Obec with kod {}", castObceDto.getKod(), obecKod);
            return false;
        }

        return true;
    }
}
