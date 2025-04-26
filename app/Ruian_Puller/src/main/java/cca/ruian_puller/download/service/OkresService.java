package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.OkresBoolean;
import cca.ruian_puller.download.dto.OkresDto;
import cca.ruian_puller.download.repository.OkresRepository;
import cca.ruian_puller.download.repository.VuscRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    public void prepareAndSave(List<OkresDto> okresDtos, AppConfig appConfig) {
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<OkresDto> toDelete = new ArrayList<>();
        okresDtos.forEach(okresDto -> {
            iterator.getAndIncrement();
            // Remove all Okres with null Kod
            if (okresDto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(okresDto);
                return;
            }
            // If dto is in db already, select it
            OkresDto okresDtoFromDb = okresRepository.findByKod(okresDto.getKod());
            if (okresDtoFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(okresDto, okresDtoFromDb);
            } else if (appConfig.getOkresConfig() != null && !appConfig.getOkresConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(okresDto, okresDtoFromDb, appConfig.getOkresConfig());
            }
            // Check if the foreign key is valid
            if (!checkFK(okresDto)) {
                removedByFK.getAndIncrement();
                toDelete.add(okresDto);
                return;
            }

            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= okresDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of OkresDtos processed");
            }
            if (iterator.get() >= okresDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of OkresDtos processed");
            }
            if (iterator.get() >= okresDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of OkresDtos processed");
            }
            if (iterator.get() >= okresDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of OkresDtos processed");
            }
        });

        // Remove all invalid OkresDtos
        okresDtos.removeAll(toDelete);

        // Log if some ObecDto were removed
        if (removedByNullKod.get() > 0) log.warn("Removed {} Obec with null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.warn("Removed {} Obec with invalid foreign keys", removedByFK.get());

        // Save OkresDtos to db
        for (int i = 0; i < okresDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), okresDtos.size());
            List<OkresDto> subList = okresDtos.subList(i, toIndex);
            okresRepository.saveAll(subList);
            log.info("Saved {} out of {} Okres", toIndex, okresDtos.size());
        }
    }

    private boolean checkFK(OkresDto okresDto) {
        // Get the foreign key Kod
        Integer vuscKod = okresDto.getVusc();

        // Check if the foreign key Kod for Vusc exists
        if (vuscKod != null && !vuscRepository.existsByKod(vuscKod)) {
            log.warn("Okres with Kod {} does not have valid foreign keys: Vusc with Kod {}", okresDto.getKod(), vuscKod);
            return false;
        }

        return true;
    }

    private void updateWithDbValues(OkresDto okresDto, OkresDto okresFromDb) {
        if (okresDto.getNazev() == null) okresDto.setNazev(okresFromDb.getNazev());
        if (okresDto.getNespravny() == null) okresDto.setNespravny(okresFromDb.getNespravny());
        if (okresDto.getKraj() == null) okresDto.setKraj(okresFromDb.getKraj());
        if (okresDto.getVusc() == null) okresDto.setVusc(okresFromDb.getVusc());
        if (okresDto.getPlatiod() == null) okresDto.setPlatiod(okresFromDb.getPlatiod());
        if (okresDto.getPlatido() == null) okresDto.setPlatido(okresFromDb.getPlatido());
        if (okresDto.getIdtransakce() == null) okresDto.setIdtransakce(okresFromDb.getIdtransakce());
        if (okresDto.getGlobalniidnavrhuzmeny() == null) okresDto.setGlobalniidnavrhuzmeny(okresFromDb.getGlobalniidnavrhuzmeny());
        if (okresDto.getNutslau() == null) okresDto.setNutslau(okresFromDb.getNutslau());
        if (okresDto.getGeometriedefbod() == null) okresDto.setGeometriedefbod(okresFromDb.getGeometriedefbod());
        if (okresDto.getGeometriegenhranice() == null) okresDto.setGeometriegenhranice(okresFromDb.getGeometriegenhranice());
        if (okresDto.getGeometrieorihranice() == null) okresDto.setGeometrieorihranice(okresFromDb.getGeometrieorihranice());
        if (okresDto.getNespravneudaje() == null) okresDto.setNespravneudaje(okresFromDb.getNespravneudaje());
        if (okresDto.getDatumvzniku() == null) okresDto.setDatumvzniku(okresFromDb.getDatumvzniku());
    }

    //region Prepare with OkresBoolean
    private void prepare(OkresDto okresDto, OkresDto okresFromDb, OkresBoolean okresConfig) {
        boolean include = okresConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (okresFromDb == null) {
            setOkresDtoFields(okresDto, okresConfig, include);
        } else {
            setOkresDtoFieldsCombinedDB(okresDto, okresFromDb, okresConfig, include);
        }
    }

    private void setOkresDtoFields(OkresDto okresDto, OkresBoolean okresConfig, boolean include) {
        if (include != okresConfig.isNazev()) okresDto.setNazev(null);
        if (include != okresConfig.isNespravny()) okresDto.setNespravny(null);
        if (include != okresConfig.isKraj()) okresDto.setKraj(null);
        if (include != okresConfig.isVusc()) okresDto.setVusc(null);
        if (include != okresConfig.isPlatiod()) okresDto.setPlatiod(null);
        if (include != okresConfig.isPlatido()) okresDto.setPlatido(null);
        if (include != okresConfig.isIdtransakce()) okresDto.setIdtransakce(null);
        if (include != okresConfig.isGlobalniidnavrhuzmeny()) okresDto.setGlobalniidnavrhuzmeny(null);
        if (include != okresConfig.isNutslau()) okresDto.setNutslau(null);
        if (include != okresConfig.isGeometriedefbod()) okresDto.setGeometriedefbod(null);
        if (include != okresConfig.isGeometriegenhranice()) okresDto.setGeometriegenhranice(null);
        if (include != okresConfig.isGeometrieorihranice()) okresDto.setGeometrieorihranice(null);
        if (include != okresConfig.isNespravneudaje()) okresDto.setNespravneudaje(null);
        if (include != okresConfig.isDatumvzniku()) okresDto.setDatumvzniku(null);
    }

    private void setOkresDtoFieldsCombinedDB(OkresDto okresDto, OkresDto okresFromDb, OkresBoolean okresConfig, boolean include) {
        if (include != okresConfig.isNazev()) okresDto.setNazev(okresFromDb.getNazev());
        if (include != okresConfig.isNespravny()) okresDto.setNespravny(okresFromDb.getNespravny());
        if (include != okresConfig.isKraj()) okresDto.setKraj(okresFromDb.getKraj());
        if (include != okresConfig.isVusc()) okresDto.setVusc(okresFromDb.getVusc());
        if (include != okresConfig.isPlatiod()) okresDto.setPlatiod(okresFromDb.getPlatiod());
        if (include != okresConfig.isPlatido()) okresDto.setPlatido(okresFromDb.getPlatido());
        if (include != okresConfig.isIdtransakce()) okresDto.setIdtransakce(okresFromDb.getIdtransakce());
        if (include != okresConfig.isGlobalniidnavrhuzmeny()) okresDto.setGlobalniidnavrhuzmeny(okresFromDb.getGlobalniidnavrhuzmeny());
        if (include != okresConfig.isNutslau()) okresDto.setNutslau(okresFromDb.getNutslau());
        if (include != okresConfig.isGeometriedefbod()) okresDto.setGeometriedefbod(okresFromDb.getGeometriedefbod());
        if (include != okresConfig.isGeometriegenhranice()) okresDto.setGeometriegenhranice(okresFromDb.getGeometriegenhranice());
        if (include != okresConfig.isGeometrieorihranice()) okresDto.setGeometrieorihranice(okresFromDb.getGeometrieorihranice());
        if (include != okresConfig.isNespravneudaje()) okresDto.setNespravneudaje(okresFromDb.getNespravneudaje());
        if (include != okresConfig.isDatumvzniku()) okresDto.setDatumvzniku(okresFromDb.getDatumvzniku());
    }
    //endregion
}
