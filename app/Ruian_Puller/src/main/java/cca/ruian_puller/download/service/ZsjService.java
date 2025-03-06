package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.ZsjDto;
import cca.ruian_puller.download.repository.KatastralniUzemiRepository;
import cca.ruian_puller.download.repository.ZsjRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class ZsjService {

    private final ZsjRepository zsjRepository;
    private final KatastralniUzemiRepository katastralniUzemiRepository;

    @Autowired
    public ZsjService(ZsjRepository zsjRepository, KatastralniUzemiRepository katastralniUzemiRepository) {
        this.zsjRepository = zsjRepository;
        this.katastralniUzemiRepository = katastralniUzemiRepository;
    }

    public void prepareAndSave(List<ZsjDto> zsjDtos, int commitSize) {
        // Check all foreign keys
        int initialSize = zsjDtos.size();
        zsjDtos.removeIf(zsjDto -> !checkFK(zsjDto));
        if (initialSize != zsjDtos.size()) {
            log.warn("{} removed from Zsj due to missing foreign keys", initialSize - zsjDtos.size());
        }

        // Split list of ZsjDto into smaller lists
        for (int i = 0; i < zsjDtos.size(); i += commitSize) {
            int toIndex = Math.min(i + commitSize, zsjDtos.size());
            List<ZsjDto> subList = zsjDtos.subList(i, toIndex);
            zsjRepository.saveAll(subList);
            log.info("Saved {} out of {} Zsj", toIndex, zsjDtos.size());
        }
    }

    private boolean checkFK(ZsjDto zsj) {
        // Get the foreign key Kod
        Integer katastralniUzemiKod = zsj.getKatastralniuzemi();

        // Check if the foreign key Kod exists
        if (!katastralniUzemiRepository.existsById(katastralniUzemiKod)) {
            log.warn("KatastralniUzemi with Kod {} does not exist", katastralniUzemiKod);
            return false;
        }
        return true;
    }
}
