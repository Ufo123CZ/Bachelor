package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.UliceBoolean;
import cca.ruian_puller.download.dto.UliceDto;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.UliceRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class UliceService {

    private final UliceRepository uliceRepository;
    private final ObecRepository obecRepository;

    @Autowired
    public UliceService(UliceRepository uliceRepository, ObecRepository obecRepository) {
        this.uliceRepository = uliceRepository;
        this.obecRepository = obecRepository;
    }

    public void prepareAndSave(List<UliceDto> uliceDtos, AppConfig appConfig) {
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<UliceDto> toDelete = new ArrayList<>();
        uliceDtos.forEach(uliceDto -> {
            // Remove all Ulice with null Kod
            if (uliceDto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(uliceDto);
                return;
            }
            // Check if the foreign key is valid
            if (!checkFK(uliceDto)) {
                removedByFK.getAndIncrement();
                toDelete.add(uliceDto);
                return;
            }
            // If dto is in db already, select it
            UliceDto uliceFromDb = uliceRepository.findByKod(uliceDto.getKod());
            if (uliceFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(uliceDto, uliceFromDb);
            } else if (appConfig.getUliceConfig() != null && !appConfig.getUliceConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(uliceDto, uliceFromDb, appConfig.getUliceConfig());
            }
            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= uliceDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of UliceDtos processed");
            }
            if (iterator.get() >= uliceDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of UliceDtos processed");
            }
            if (iterator.get() >= uliceDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of UliceDtos processed");
            }
            if (iterator.get() >= uliceDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of UliceDtos processed");
            }
        });

        // Remove all invalid UliceDtos
        uliceDtos.removeAll(toDelete);

        // Log if some UliceDto were removed
        if (removedByNullKod.get() > 0) log.warn("Removed {} Ulice with null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.warn("Removed {} Ulice with invalid foreign keys", removedByFK.get());

        // Save UliceDtos to db
        for (int i = 0; i < uliceDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), uliceDtos.size());
            List<UliceDto> subList = uliceDtos.subList(i, toIndex);
            uliceRepository.saveAll(subList);
            log.info("Saved {} out of {} Ulice", toIndex, uliceDtos.size());
        }
    }

    private boolean checkFK(UliceDto uliceDto) {
        // Get the foreign key Kod
        Integer obecKod = uliceDto.getObec();

        // Check if the foreign key Kod for Obec is valid
        if (obecKod != null && !obecRepository.existsByKod(obecKod)) {
            log.warn("Ulice with Kod {} does not have valid foreign keys: Obec with Kod {}", uliceDto.getKod(), obecKod);
            return false;
        }

        return true;
    }

    private void updateWithDbValues(UliceDto uliceDto, UliceDto uliceFromDb) {
        if (uliceDto.getNazev() == null) uliceDto.setNazev(uliceFromDb.getNazev());
        if (uliceDto.getNespravny() == null) uliceDto.setNespravny(uliceFromDb.getNespravny());
        if (uliceDto.getObec() == null) uliceDto.setObec(uliceFromDb.getObec());
        if (uliceDto.getPlatiod() == null) uliceDto.setPlatiod(uliceFromDb.getPlatiod());
        if (uliceDto.getPlatido() == null) uliceDto.setPlatido(uliceFromDb.getPlatido());
        if (uliceDto.getIdtransakce() == null) uliceDto.setIdtransakce(uliceFromDb.getIdtransakce());
        if (uliceDto.getGlobalniidnavrhuzmeny() == null) uliceDto.setGlobalniidnavrhuzmeny(uliceFromDb.getGlobalniidnavrhuzmeny());
        if (uliceDto.getGeometriedefbod() == null) uliceDto.setGeometriedefbod(uliceFromDb.getGeometriedefbod());
        if (uliceDto.getGeometriedefcara() == null) uliceDto.setGeometriedefcara(uliceFromDb.getGeometriedefcara());
        if (uliceDto.getNespravneudaje() == null) uliceDto.setNespravneudaje(uliceFromDb.getNespravneudaje());
    }


    //region Prepare with UliceBoolean
    private void prepare(UliceDto uliceDto, UliceDto uliceFromDb, UliceBoolean uliceConfig) {
        boolean include = uliceConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (uliceFromDb == null) {
            setUliceDtoFields(uliceDto, uliceConfig, include);
        } else {
            setUliceDtoFieldsCombinedDB(uliceDto, uliceFromDb, uliceConfig, include);
        }
    }

    private void setUliceDtoFields(UliceDto uliceDto, UliceBoolean uliceConfig, boolean include) {
        if (include != uliceConfig.isNazev()) uliceDto.setNazev(null);
        if (include != uliceConfig.isNespravny()) uliceDto.setNespravny(null);
        if (include != uliceConfig.isObec()) uliceDto.setObec(null);
        if (include != uliceConfig.isPlatiod()) uliceDto.setPlatiod(null);
        if (include != uliceConfig.isPlatido()) uliceDto.setPlatido(null);
        if (include != uliceConfig.isIdtransakce()) uliceDto.setIdtransakce(null);
        if (include != uliceConfig.isGlobalniidnavrhuzmeny()) uliceDto.setGlobalniidnavrhuzmeny(null);
        if (include != uliceConfig.isGeometriedefbod()) uliceDto.setGeometriedefbod(null);
        if (include != uliceConfig.isGeometriedefcara()) uliceDto.setGeometriedefcara(null);
        if (include != uliceConfig.isNespravneudaje()) uliceDto.setNespravneudaje(null);
    }

    private void setUliceDtoFieldsCombinedDB(UliceDto uliceDto, UliceDto uliceFromDb, UliceBoolean uliceConfig, boolean include) {
        if (include != uliceConfig.isNazev()) uliceDto.setNazev(uliceFromDb.getNazev());
        if (include != uliceConfig.isNespravny()) uliceDto.setNespravny(uliceFromDb.getNespravny());
        if (include != uliceConfig.isObec()) uliceDto.setObec(uliceFromDb.getObec());
        if (include != uliceConfig.isPlatiod()) uliceDto.setPlatiod(uliceFromDb.getPlatiod());
        if (include != uliceConfig.isPlatido()) uliceDto.setPlatido(uliceFromDb.getPlatido());
        if (include != uliceConfig.isIdtransakce()) uliceDto.setIdtransakce(uliceFromDb.getIdtransakce());
        if (include != uliceConfig.isGlobalniidnavrhuzmeny()) uliceDto.setGlobalniidnavrhuzmeny(uliceFromDb.getGlobalniidnavrhuzmeny());
        if (include != uliceConfig.isGeometriedefbod()) uliceDto.setGeometriedefbod(uliceFromDb.getGeometriedefbod());
        if (include != uliceConfig.isGeometriedefcara()) uliceDto.setGeometriedefcara(uliceFromDb.getGeometriedefcara());
        if (include != uliceConfig.isNespravneudaje()) uliceDto.setNespravneudaje(uliceFromDb.getNespravneudaje());
    }
    //endregion
}
