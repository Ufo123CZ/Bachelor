package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.PouBoolean;
import cca.ruian_puller.download.dto.PouDto;
import cca.ruian_puller.download.repository.OrpRepository;
import cca.ruian_puller.download.repository.PouRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<PouDto> toDelete = new ArrayList<>();
        pouDtos.forEach(pouDto -> {
            // Remove all Pou with null Kod
            if (pouDto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(pouDto);
                return;
            }
            // Check if the foreign key is valid
            if (!checkFK(pouDto)) {
                removedByFK.getAndIncrement();
                toDelete.add(pouDto);
                return;
            }
            // If dto is in db already, select it
            PouDto pouFromDb = pouRepository.findByKod(pouDto.getKod());
            if (pouFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(pouDto, pouFromDb);
            } else if (appConfig.getPouConfig() != null && !appConfig.getPouConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(pouDto, pouFromDb, appConfig.getPouConfig());
            }
            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= pouDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of PouDtos processed");
            }
            if (iterator.get() >= pouDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of PouDtos processed");
            }
            if (iterator.get() >= pouDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of PouDtos processed");
            }
            if (iterator.get() >= pouDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of PouDtos processed");
            }
        });
        // Remove all invalid PouDtos
        pouDtos.removeAll(toDelete);
        // Log if some PouDto were removed
        if (removedByNullKod.get() > 0) log.warn("Removed {} Pou with null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.warn("Removed {} Pou with invalid foreign keys", removedByFK.get());

        // Save PouDtos to db
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

    private void updateWithDbValues(PouDto pouDto, PouDto pouFromDb) {
        if (pouDto.getNazev() == null) pouDto.setNazev(pouFromDb.getNazev());
        if (pouDto.getNespravny() == null) pouDto.setNespravny(pouFromDb.getNespravny());
        if (pouDto.getSpravniobeckod() == null) pouDto.setSpravniobeckod(pouFromDb.getSpravniobeckod());
        if (pouDto.getOrp() == null) pouDto.setOrp(pouFromDb.getOrp());
        if (pouDto.getPlatiod() == null) pouDto.setPlatiod(pouFromDb.getPlatiod());
        if (pouDto.getPlatido() == null) pouDto.setPlatido(pouFromDb.getPlatido());
        if (pouDto.getIdtransakce() == null) pouDto.setIdtransakce(pouFromDb.getIdtransakce());
        if (pouDto.getGlobalniidnavrhuzmeny() == null) pouDto.setGlobalniidnavrhuzmeny(pouFromDb.getGlobalniidnavrhuzmeny());
        if (pouDto.getGeometriedefbod() == null) pouDto.setGeometriedefbod(pouFromDb.getGeometriedefbod());
        if (pouDto.getGeometriegenhranice() == null) pouDto.setGeometriegenhranice(pouFromDb.getGeometriegenhranice());
        if (pouDto.getGeometrieorihranice() == null) pouDto.setGeometrieorihranice(pouFromDb.getGeometrieorihranice());
        if (pouDto.getNespravneudaje() == null) pouDto.setNespravneudaje(pouFromDb.getNespravneudaje());
        if (pouDto.getDatumvzniku() == null) pouDto.setDatumvzniku(pouFromDb.getDatumvzniku());
    }

    //region Prepare with PouBoolean
    private void prepare(PouDto pouDto, PouDto pouFromDb, PouBoolean pouConfig) {
        boolean include = pouConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (pouFromDb == null) {
            setPouDtoFields(pouDto, pouConfig, include);
        } else {
            setPouDtoFieldsCombinedDB(pouDto, pouFromDb, pouConfig, include);
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
        if (include != pouConfig.isNazev()) pouDto.setNazev(pouDtoFromDb.getNazev());
        if (include != pouConfig.isNespravny()) pouDto.setNespravny(pouDtoFromDb.getNespravny());
        if (include != pouConfig.isSpravniobeckod()) pouDto.setSpravniobeckod(pouDtoFromDb.getSpravniobeckod());
        if (include != pouConfig.isOrp()) pouDto.setOrp(pouDtoFromDb.getOrp());
        if (include != pouConfig.isPlatiod()) pouDto.setPlatiod(pouDtoFromDb.getPlatiod());
        if (include != pouConfig.isPlatido()) pouDto.setPlatido(pouDtoFromDb.getPlatido());
        if (include != pouConfig.isIdtransakce()) pouDto.setIdtransakce(pouDtoFromDb.getIdtransakce());
        if (include != pouConfig.isGlobalniidnavrhuzmeny()) pouDto.setGlobalniidnavrhuzmeny(pouDtoFromDb.getGlobalniidnavrhuzmeny());
        if (include != pouConfig.isGeometriedefbod()) pouDto.setGeometriedefbod(pouDtoFromDb.getGeometriedefbod());
        if (include != pouConfig.isGeometriegenhranice()) pouDto.setGeometriegenhranice(pouDtoFromDb.getGeometriegenhranice());
        if (include != pouConfig.isGeometrieorihranice()) pouDto.setGeometrieorihranice(pouDtoFromDb.getGeometrieorihranice());
        if (include != pouConfig.isNespravneudaje()) pouDto.setNespravneudaje(pouDtoFromDb.getNespravneudaje());
        if (include != pouConfig.isDatumvzniku()) pouDto.setDatumvzniku(pouDtoFromDb.getDatumvzniku());
    }
    //endregion
}
