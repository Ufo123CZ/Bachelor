package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.OrpBoolean;
import cca.ruian_puller.download.dto.OrpDto;
import cca.ruian_puller.download.repository.OkresRepository;
import cca.ruian_puller.download.repository.OrpRepository;
import cca.ruian_puller.download.repository.VuscRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class OrpService {

    private final OrpRepository orpRepository;
    private final VuscRepository vuscRepository;
    private final OkresRepository okresRepository;

    @Autowired
    public OrpService(OrpRepository orpRepository, VuscRepository vuscRepository, OkresRepository okresRepository) {
        this.orpRepository = orpRepository;
        this.vuscRepository = vuscRepository;
        this.okresRepository = okresRepository;
    }

    public void prepareAndSave(List<OrpDto> orpDtos, AppConfig appConfig) {
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<OrpDto> toDelete = new ArrayList<>();
        orpDtos.forEach(orpDto -> {
            // Remove all Orp with null Kod
            if (orpDto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(orpDto);
                return;
            }
            // Check if the foreign key is valid
            if (!checkBF(orpDto)) {
                removedByFK.getAndIncrement();
                toDelete.add(orpDto);
                return;
            }
            // If dto is in db already, select it
            OrpDto orpFromDb = orpRepository.findByKod(orpDto.getKod());
            if (orpFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(orpDto, orpFromDb);
            } else if (appConfig.getOrpConfig() != null && !appConfig.getOrpConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(orpDto, orpFromDb, appConfig.getOrpConfig());
            }
            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= orpDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of OrpDtos processed");
            }
            if (iterator.get() >= orpDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of OrpDtos processed");
            }
            if (iterator.get() >= orpDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of OrpDtos processed");
            }
            if (iterator.get() >= orpDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of OrpDtos processed");
            }
        });

        // Remove all invalid OrpDtos
        orpDtos.removeAll(toDelete);

        // Log if some OrpDto were removed
        if (removedByNullKod.get() > 0) log.warn("Removed {} Orp with null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.warn("Removed {} Orp with invalid foreign keys", removedByFK.get());

        // Save OrpDtos to db
        for (int i = 0; i < orpDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), orpDtos.size());
            List<OrpDto> subList = orpDtos.subList(i, toIndex);
            orpRepository.saveAll(subList);
            log.info("Saved {} out of {} Orp", toIndex, orpDtos.size());
        }
    }

    private boolean checkBF(OrpDto orpDto) {
        // Get the foreign keys Kod
        Integer vuscKod = orpDto.getVusc();
        Integer okresKod = orpDto.getOkres();

        // Check if the foreign key Kod for Vusc exists
        if (vuscKod != null && !vuscRepository.existsByKod(vuscKod)) {
            log.warn("Orp with Kod {} does not have a valid foreign key: Vusc with Kod {}", orpDto.getKod(), vuscKod);
            return false;
        }

        // Check if the foreign key Kod for Okres exists
        if (okresKod != null && !okresRepository.existsByKod(okresKod)) {
            log.warn("Orp with Kod {} does not have a valid foreign key: Okres with Kod {}", orpDto.getKod(), okresKod);
            return false;
        }

        return true;
    }

    private void updateWithDbValues(OrpDto orpDto, OrpDto orpFromDb) {
        if (orpDto.getNazev() == null) orpDto.setNazev(orpFromDb.getNazev());
        if (orpDto.getNespravny() == null) orpDto.setNespravny(orpFromDb.getNespravny());
        if (orpDto.getSpravniobeckod() == null) orpDto.setSpravniobeckod(orpFromDb.getSpravniobeckod());
        if (orpDto.getVusc() == null) orpDto.setVusc(orpFromDb.getVusc());
        if (orpDto.getOkres() == null) orpDto.setOkres(orpFromDb.getOkres());
        if (orpDto.getPlatiod() == null) orpDto.setPlatiod(orpFromDb.getPlatiod());
        if (orpDto.getPlatido() == null) orpDto.setPlatido(orpFromDb.getPlatido());
        if (orpDto.getIdtransakce() == null) orpDto.setIdtransakce(orpFromDb.getIdtransakce());
        if (orpDto.getGlobalniidnavrhuzmeny() == null) orpDto.setGlobalniidnavrhuzmeny(orpFromDb.getGlobalniidnavrhuzmeny());
        if (orpDto.getGeometriedefbod() == null) orpDto.setGeometriedefbod(orpFromDb.getGeometriedefbod());
        if (orpDto.getGeometriegenhranice() == null) orpDto.setGeometriegenhranice(orpFromDb.getGeometriegenhranice());
        if (orpDto.getGeometrieorihranice() == null) orpDto.setGeometrieorihranice(orpFromDb.getGeometrieorihranice());
        if (orpDto.getNespravneudaje() == null) orpDto.setNespravneudaje(orpFromDb.getNespravneudaje());
        if (orpDto.getDatumvzniku() == null) orpDto.setDatumvzniku(orpFromDb.getDatumvzniku());
    }

    //region Prepare with OrpBoolean
    private void prepare(OrpDto orpDto, OrpDto orpFromDb, OrpBoolean orpConfig) {
        boolean include = orpConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (orpFromDb == null) {
            setOrpDtoFields(orpDto, orpConfig, include);
        } else {
            setOrpDtoFieldsCombinedDB(orpDto, orpFromDb, orpConfig, include);
        }
    }

    private void setOrpDtoFields(OrpDto orpDto, OrpBoolean orpConfig, boolean include) {
        if (include != orpConfig.isNazev()) orpDto.setNazev(null);
        if (include != orpConfig.isNespravny()) orpDto.setNespravny(null);
        if (include != orpConfig.isSpravniobeckod()) orpDto.setSpravniobeckod(null);
        if (include != orpConfig.isVusc()) orpDto.setVusc(null);
        if (include != orpConfig.isOkres()) orpDto.setOkres(null);
        if (include != orpConfig.isPlatiod()) orpDto.setPlatiod(null);
        if (include != orpConfig.isPlatido()) orpDto.setPlatido(null);
        if (include != orpConfig.isIdtransakce()) orpDto.setIdtransakce(null);
        if (include != orpConfig.isGlobalniidnavrhuzmeny()) orpDto.setGlobalniidnavrhuzmeny(null);
        if (include != orpConfig.isGeometriedefbod()) orpDto.setGeometriedefbod(null);
        if (include != orpConfig.isGeometriegenhranice()) orpDto.setGeometriegenhranice(null);
        if (include != orpConfig.isGeometrieorihranice()) orpDto.setGeometrieorihranice(null);
        if (include != orpConfig.isNespravneudaje()) orpDto.setNespravneudaje(null);
        if (include != orpConfig.isDatumvzniku()) orpDto.setDatumvzniku(null);
    }

    private void setOrpDtoFieldsCombinedDB(OrpDto orpDto, OrpDto orpFromDb, OrpBoolean orpConfig, boolean include) {
        if (include != orpConfig.isNazev()) orpDto.setNazev(orpFromDb.getNazev());
        if (include != orpConfig.isNespravny()) orpDto.setNespravny(orpFromDb.getNespravny());
        if (include != orpConfig.isSpravniobeckod()) orpDto.setSpravniobeckod(orpFromDb.getSpravniobeckod());
        if (include != orpConfig.isVusc()) orpDto.setVusc(orpFromDb.getVusc());
        if (include != orpConfig.isOkres()) orpDto.setOkres(orpFromDb.getOkres());
        if (include != orpConfig.isPlatiod()) orpDto.setPlatiod(orpFromDb.getPlatiod());
        if (include != orpConfig.isPlatido()) orpDto.setPlatido(orpFromDb.getPlatido());
        if (include != orpConfig.isIdtransakce()) orpDto.setIdtransakce(orpFromDb.getIdtransakce());
        if (include != orpConfig.isGlobalniidnavrhuzmeny()) orpDto.setGlobalniidnavrhuzmeny(orpFromDb.getGlobalniidnavrhuzmeny());
        if (include != orpConfig.isGeometriedefbod()) orpDto.setGeometriedefbod(orpFromDb.getGeometriedefbod());
        if (include != orpConfig.isGeometriegenhranice()) orpDto.setGeometriegenhranice(orpFromDb.getGeometriegenhranice());
        if (include != orpConfig.isGeometrieorihranice()) orpDto.setGeometrieorihranice(orpFromDb.getGeometrieorihranice());
        if (include != orpConfig.isNespravneudaje()) orpDto.setNespravneudaje(orpFromDb.getNespravneudaje());
        if (include != orpConfig.isDatumvzniku()) orpDto.setDatumvzniku(orpFromDb.getDatumvzniku());
    }
    //endregion
}
