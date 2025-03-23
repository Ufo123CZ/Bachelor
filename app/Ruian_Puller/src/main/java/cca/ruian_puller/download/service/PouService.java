package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.PouBoolean;
import cca.ruian_puller.download.dto.OrpDto;
import cca.ruian_puller.download.dto.PouDto;
import cca.ruian_puller.download.repository.OrpRepository;
import cca.ruian_puller.download.repository.PouRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class PouService {

    private final PouRepository pouRepository;
    private final OrpRepository orpRepository;

    @Autowired
    public PouService(PouRepository pouRepository, OrpRepository orpRepository) {
        this.pouRepository = pouRepository;
        this.orpRepository = orpRepository;
    }

    public void prepareAndSave(List<PouDto> pouDtos, AppConfig appConfig) {
        // Remove all Pou with null Kod
        int initialSize = pouDtos.size();
        pouDtos.removeIf(pouDto -> pouDto.getKod() == null);
        if (initialSize != pouDtos.size()) {
            log.warn("{} removed from Pou due to null Kod", initialSize - pouDtos.size());
        }

        // Based on PouBoolean from AppConfig, filter out PouDto
        if (!appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL))
            pouDtos.forEach(pouDto -> prepare(pouDto, appConfig.getPouConfig()));

        // Check all foreign keys
        int initialSize2 = pouDtos.size();
        pouDtos.removeIf(pouDto -> !checkFK(pouDto));
        if (initialSize2 != pouDtos.size()) {
            log.warn("{} removed from Pou due to missing foreign keys", initialSize2 - pouDtos.size());
        }

        // Split list of PouDto into smaller lists
        for (int i = 0; i < pouDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), pouDtos.size());
            List<PouDto> subList = pouDtos.subList(i, toIndex);
            pouRepository.saveAll(subList);
            log.info("Saved {} out of {} Pou", toIndex, pouDtos.size());
        }
    }


    private boolean checkFK(PouDto pouDto) {
        // Get the foreign key Kod
        Integer orpKod = pouDto.getOrp();

        // Check if the foreign key Kod for Orp exists
        if (orpKod != null && !orpRepository.existsByKod(orpKod)) {
            log.warn("Pou with Kod {} does not have valid foreign keys: Orp with Kod {}", pouDto.getKod(), orpKod);
            return false;
        }

        return true;
    }

    //region Prepare with PouBoolean
    private void prepare(PouDto pouDto, PouBoolean pouConfig) {
        // Check if this dto is in db already
        PouDto pouDtoFromDb = pouRepository.findByKod(pouDto.getKod());
        boolean include = pouConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (pouDtoFromDb == null) {
            setPouDtoFields(pouDto, pouConfig, include);
        } else {
            setPouDtoFieldsCombinedDB(pouDto, pouDtoFromDb, pouConfig, include);
        }
    }

    private void setPouDtoFields(PouDto pouDto, PouBoolean pouConfig, boolean include) {
        if (include != pouConfig.isNazev()) pouDto.setNazev(null);
        if (include != pouConfig.isNespravny()) pouDto.setNespravny(null);
        if (include != pouConfig.isSpravniobeckod()) pouDto.setSpravniobeckod(null);
        if (include != pouConfig.isOrp()) pouDto.setOrp(null);
        if (include != pouConfig.isPlatiod()) pouDto.setPlatiod(null);
        if (include != pouConfig.isPlatido()) pouDto.setPlatido(null);
        if (include != pouConfig.isIdtransakce()) pouDto.setIdtransakce(null);
        if (include != pouConfig.isGlobalniidnavrhuzmeny()) pouDto.setGlobalniidnavrhuzmeny(null);
        if (include != pouConfig.isGeometriedefbod()) pouDto.setGeometriedefbod(null);
        if (include != pouConfig.isGeometriegenhranice()) pouDto.setGeometriegenhranice(null);
        if (include != pouConfig.isGeometrieorihranice()) pouDto.setGeometrieorihranice(null);
        if (include != pouConfig.isNespravneudaje()) pouDto.setNespravneudaje(null);
        if (include != pouConfig.isDatumvzniku()) pouDto.setDatumvzniku(null);
    }

    private void setPouDtoFieldsCombinedDB(PouDto pouDto, PouDto pouDtoFromDb, PouBoolean pouConfig, boolean include) {
        if (pouDtoFromDb.getNazev() != null && (include == pouConfig.isNazev()))
            pouDto.setNazev(pouDtoFromDb.getNazev());
        if (pouDtoFromDb.getNespravny() != null && (include == pouConfig.isNespravny()))
            pouDto.setNespravny(pouDtoFromDb.getNespravny());
        if (pouDtoFromDb.getSpravniobeckod() != null && (include == pouConfig.isSpravniobeckod()))
            pouDto.setSpravniobeckod(pouDtoFromDb.getSpravniobeckod());
        if (pouDtoFromDb.getOrp() != null && (include == pouConfig.isOrp()))
            pouDto.setOrp(pouDtoFromDb.getOrp());
        if (pouDtoFromDb.getPlatiod() != null && (include == pouConfig.isPlatiod()))
            pouDto.setPlatiod(pouDtoFromDb.getPlatiod());
        if (pouDtoFromDb.getPlatido() != null && (include == pouConfig.isPlatido()))
            pouDto.setPlatido(pouDtoFromDb.getPlatido());
        if (pouDtoFromDb.getIdtransakce() != null && (include == pouConfig.isIdtransakce()))
            pouDto.setIdtransakce(pouDtoFromDb.getIdtransakce());
        if (pouDtoFromDb.getGlobalniidnavrhuzmeny() != null && (include == pouConfig.isGlobalniidnavrhuzmeny()))
            pouDto.setGlobalniidnavrhuzmeny(pouDtoFromDb.getGlobalniidnavrhuzmeny());
        if (pouDtoFromDb.getGeometriedefbod() != null && (include == pouConfig.isGeometriedefbod()))
            pouDto.setGeometriedefbod(pouDtoFromDb.getGeometriedefbod());
        if (pouDtoFromDb.getGeometriegenhranice() != null && (include == pouConfig.isGeometriegenhranice()))
            pouDto.setGeometriegenhranice(pouDtoFromDb.getGeometriegenhranice());
        if (pouDtoFromDb.getGeometrieorihranice() != null && (include == pouConfig.isGeometrieorihranice()))
            pouDto.setGeometrieorihranice(pouDtoFromDb.getGeometrieorihranice());
        if (pouDtoFromDb.getNespravneudaje() != null && (include == pouConfig.isNespravneudaje()))
            pouDto.setNespravneudaje(pouDtoFromDb.getNespravneudaje());
        if (pouDtoFromDb.getDatumvzniku() != null && (include == pouConfig.isDatumvzniku()))
            pouDto.setDatumvzniku(pouDtoFromDb.getDatumvzniku());
    }
    //endregion
}
