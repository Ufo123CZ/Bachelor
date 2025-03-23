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

import java.util.List;

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
        // Remove all Ulice with null Kod
        int initialSize = uliceDtos.size();
        uliceDtos.removeIf(uliceDto -> uliceDto.getKod() == null);
        if (initialSize != uliceDtos.size()) {
            log.warn("{} removed from Ulice due to null Kod", initialSize - uliceDtos.size());
        }

        // Based on UliceBoolean from AppConfig, filter out UliceDto
        if (!appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL))
            uliceDtos.forEach(uliceDto -> prepare(uliceDto, appConfig.getUliceConfig()));


        // Check all foreign keys
        int initialSize2 = uliceDtos.size();
        uliceDtos.removeIf(uliceDto -> !checkFK(uliceDto));
        if (initialSize2 != uliceDtos.size()) {
            log.warn("{} removed from Ulice due to missing foreign keys", initialSize2 - uliceDtos.size());
        }

        // Split list of UliceDto into smaller lists
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

    //region Prepare with UliceBoolean
    private void prepare(UliceDto uliceDto, UliceBoolean uliceConfig) {
        // Check if this dto is in db already
        UliceDto uliceDtoFromDb = uliceRepository.findByKod(uliceDto.getKod());
        boolean include = uliceConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (uliceDtoFromDb == null) {
            setUliceDtoFields(uliceDto, uliceConfig, include);
        } else {
            setUliceDtoFieldsCombinedDB(uliceDto, uliceDtoFromDb, uliceConfig, include);
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

    private void setUliceDtoFieldsCombinedDB(UliceDto uliceDto, UliceDto uliceDtoFromDb, UliceBoolean uliceConfig, boolean include) {
        if (uliceDtoFromDb.getNazev() != null && include == uliceConfig.isNazev())
            uliceDto.setNazev(uliceDtoFromDb.getNazev());
        if (uliceDtoFromDb.getNespravny() != null && include == uliceConfig.isNespravny())
            uliceDto.setNespravny(uliceDtoFromDb.getNespravny());
        if (uliceDtoFromDb.getObec() != null && include == uliceConfig.isObec())
            uliceDto.setObec(uliceDtoFromDb.getObec());
        if (uliceDtoFromDb.getPlatiod() != null && include == uliceConfig.isPlatiod())
            uliceDto.setPlatiod(uliceDtoFromDb.getPlatiod());
        if (uliceDtoFromDb.getPlatido() != null && include == uliceConfig.isPlatido())
            uliceDto.setPlatido(uliceDtoFromDb.getPlatido());
        if (uliceDtoFromDb.getIdtransakce() != null && include == uliceConfig.isIdtransakce())
            uliceDto.setIdtransakce(uliceDtoFromDb.getIdtransakce());
        if (uliceDtoFromDb.getGlobalniidnavrhuzmeny() != null && include == uliceConfig.isGlobalniidnavrhuzmeny())
            uliceDto.setGlobalniidnavrhuzmeny(uliceDtoFromDb.getGlobalniidnavrhuzmeny());
        if (uliceDtoFromDb.getGeometriedefbod() != null && include == uliceConfig.isGeometriedefbod())
            uliceDto.setGeometriedefbod(uliceDtoFromDb.getGeometriedefbod());
        if (uliceDtoFromDb.getGeometriedefcara() != null && include == uliceConfig.isGeometriedefcara())
            uliceDto.setGeometriedefcara(uliceDtoFromDb.getGeometriedefcara());
        if (uliceDtoFromDb.getNespravneudaje() != null && include == uliceConfig.isNespravneudaje())
            uliceDto.setNespravneudaje(uliceDtoFromDb.getNespravneudaje());
    }
    //endregion
}
