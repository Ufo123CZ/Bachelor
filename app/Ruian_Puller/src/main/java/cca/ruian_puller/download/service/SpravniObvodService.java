package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.SpravniObvodBoolean;
import cca.ruian_puller.download.dto.SpravniObvodDto;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.SpravniObvodRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class SpravniObvodService {

    private final SpravniObvodRepository spravniObvodRepository;
    private final ObecRepository obecRepository;

    @Autowired
    public SpravniObvodService(SpravniObvodRepository spravniObvodRepository, ObecRepository obecRepository) {
        this.spravniObvodRepository = spravniObvodRepository;
        this.obecRepository = obecRepository;
    }

    public void prepareAndSave(List<SpravniObvodDto> spravniObvodDtos, AppConfig appConfig) {
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<SpravniObvodDto> toDelete = new ArrayList<>();
        spravniObvodDtos.forEach(spravniObvodDto -> {
            iterator.getAndIncrement();
            // Remove all SpravniObvod with null Kod
            if (spravniObvodDto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(spravniObvodDto);
                return;
            }
            // Check if the foreign key is valid
            if (!checkFK(spravniObvodDto)) {
                removedByFK.getAndIncrement();
                toDelete.add(spravniObvodDto);
                return;
            }
            // If dto is in db already, select it
            SpravniObvodDto spravniObvodFromDb = spravniObvodRepository.findByKod(spravniObvodDto.getKod());
            if (spravniObvodFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(spravniObvodDto, spravniObvodFromDb);
            } else if (appConfig.getSpravniObvodConfig() != null && !appConfig.getSpravniObvodConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(spravniObvodDto, spravniObvodFromDb, appConfig.getSpravniObvodConfig());
            }
            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= spravniObvodDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of SpravniObvodDtos processed");
            }
            if (iterator.get() >= spravniObvodDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of SpravniObvodDtos processed");
            }
            if (iterator.get() >= spravniObvodDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of SpravniObvodDtos processed");
            }
            if (iterator.get() >= spravniObvodDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of SpravniObvodDtos processed");
            }
        });

        // Remove all invalid SpravniObvodDtos
        spravniObvodDtos.removeAll(toDelete);

        // Log if some SpravniObvodDto were removed
        if (removedByNullKod.get() > 0) log.warn("Removed {} SpravniObvod with null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.warn("Removed {} SpravniObvod with invalid foreign keys", removedByFK.get());

        // Save SpravniObvodDtos to db
        for (int i = 0; i < spravniObvodDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), spravniObvodDtos.size());
            List<SpravniObvodDto> subList = spravniObvodDtos.subList(i, toIndex);
            spravniObvodRepository.saveAll(subList);
            log.info("Saved {} out of {} SpravniObvod", toIndex, spravniObvodDtos.size());
        }
    }

    private boolean checkFK(SpravniObvodDto spravniObvodDto) {
        // Get the foreign key Kod
        Integer obecKod = spravniObvodDto.getObec();

        // Check if the foreign key Kod for Obec is valid
        if (obecKod != null && !obecRepository.existsByKod(obecKod)) {
            log.warn("SpravniObvod with Kod {} does not have valid foreign keys: Obec with Kod {}", spravniObvodDto.getKod(), obecKod);
            return false;
        }

        return true;
    }

    private void updateWithDbValues(SpravniObvodDto spravniObvodDto, SpravniObvodDto spravniObvodFromDb) {
        if (spravniObvodDto.getNazev() == null) spravniObvodDto.setNazev(spravniObvodFromDb.getNazev());
        if (spravniObvodDto.getNespravny() == null) spravniObvodDto.setNespravny(spravniObvodFromDb.getNespravny());
        if (spravniObvodDto.getSpravnimomckod() == null) spravniObvodDto.setSpravnimomckod(spravniObvodFromDb.getSpravnimomckod());
        if (spravniObvodDto.getObec() == null) spravniObvodDto.setObec(spravniObvodFromDb.getObec());
        if (spravniObvodDto.getPlatiod() == null) spravniObvodDto.setPlatiod(spravniObvodFromDb.getPlatiod());
        if (spravniObvodDto.getPlatido() == null) spravniObvodDto.setPlatido(spravniObvodFromDb.getPlatido());
        if (spravniObvodDto.getIdtransakce() == null) spravniObvodDto.setIdtransakce(spravniObvodFromDb.getIdtransakce());
        if (spravniObvodDto.getGlobalniidnavrhuzmeny() == null) spravniObvodDto.setGlobalniidnavrhuzmeny(spravniObvodFromDb.getGlobalniidnavrhuzmeny());
        if (spravniObvodDto.getGeometriedefbod() == null) spravniObvodDto.setGeometriedefbod(spravniObvodFromDb.getGeometriedefbod());
        if (spravniObvodDto.getGeometrieorihranice() == null) spravniObvodDto.setGeometrieorihranice(spravniObvodFromDb.getGeometrieorihranice());
        if (spravniObvodDto.getNespravneudaje() == null) spravniObvodDto.setNespravneudaje(spravniObvodFromDb.getNespravneudaje());
        if (spravniObvodDto.getDatumvzniku() == null) spravniObvodDto.setDatumvzniku(spravniObvodFromDb.getDatumvzniku());
    }

    //region Prepare with SpravniObvodBoolean
    private void prepare(SpravniObvodDto spravniObvodDto, SpravniObvodDto spravniObvodFromDb, SpravniObvodBoolean spravniObvodConfig) {
        boolean include = spravniObvodConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (spravniObvodFromDb == null) {
            setSpravniObvodDtoFields(spravniObvodDto, spravniObvodConfig, include);
        } else {
            setSpravniObvodDtoFieldsCombinedDB(spravniObvodDto, spravniObvodFromDb, spravniObvodConfig, include);
        }
    }

    private void setSpravniObvodDtoFields(SpravniObvodDto spravniObvodDto, SpravniObvodBoolean spravniObvodConfig, boolean include) {
        if (include != spravniObvodConfig.isNazev()) spravniObvodDto.setNazev(null);
        if (include != spravniObvodConfig.isNespravny()) spravniObvodDto.setNespravny(null);
        if (include != spravniObvodConfig.isSpravnimomckod()) spravniObvodDto.setSpravnimomckod(null);
        if (include != spravniObvodConfig.isObec()) spravniObvodDto.setObec(null);
        if (include != spravniObvodConfig.isPlatiod()) spravniObvodDto.setPlatiod(null);
        if (include != spravniObvodConfig.isPlatido()) spravniObvodDto.setPlatido(null);
        if (include != spravniObvodConfig.isIdtransakce()) spravniObvodDto.setIdtransakce(null);
        if (include != spravniObvodConfig.isGlobalniidnavrhuzmeny()) spravniObvodDto.setGlobalniidnavrhuzmeny(null);
        if (include != spravniObvodConfig.isGeometriedefbod()) spravniObvodDto.setGeometriedefbod(null);
        if (include != spravniObvodConfig.isGeometrieorihranice()) spravniObvodDto.setGeometrieorihranice(null);
        if (include != spravniObvodConfig.isNespravneudaje()) spravniObvodDto.setNespravneudaje(null);
        if (include != spravniObvodConfig.isDatumvzniku()) spravniObvodDto.setDatumvzniku(null);
    }

    private void setSpravniObvodDtoFieldsCombinedDB(SpravniObvodDto spravniObvodDto, SpravniObvodDto spravniObvodFromDb, SpravniObvodBoolean spravniObvodConfig, boolean include) {
        if (include != spravniObvodConfig.isNazev()) spravniObvodDto.setNazev(spravniObvodFromDb.getNazev());
        if (include != spravniObvodConfig.isNespravny()) spravniObvodDto.setNespravny(spravniObvodFromDb.getNespravny());
        if (include != spravniObvodConfig.isSpravnimomckod()) spravniObvodDto.setSpravnimomckod(spravniObvodFromDb.getSpravnimomckod());
        if (include != spravniObvodConfig.isObec()) spravniObvodDto.setObec(spravniObvodFromDb.getObec());
        if (include != spravniObvodConfig.isPlatiod()) spravniObvodDto.setPlatiod(spravniObvodFromDb.getPlatiod());
        if (include != spravniObvodConfig.isPlatido()) spravniObvodDto.setPlatido(spravniObvodFromDb.getPlatido());
        if (include != spravniObvodConfig.isIdtransakce()) spravniObvodDto.setIdtransakce(spravniObvodFromDb.getIdtransakce());
        if (include != spravniObvodConfig.isGlobalniidnavrhuzmeny()) spravniObvodDto.setGlobalniidnavrhuzmeny(spravniObvodFromDb.getGlobalniidnavrhuzmeny());
        if (include != spravniObvodConfig.isGeometriedefbod()) spravniObvodDto.setGeometriedefbod(spravniObvodFromDb.getGeometriedefbod());
        if (include != spravniObvodConfig.isGeometrieorihranice()) spravniObvodDto.setGeometrieorihranice(spravniObvodFromDb.getGeometrieorihranice());
        if (include != spravniObvodConfig.isNespravneudaje()) spravniObvodDto.setNespravneudaje(spravniObvodFromDb.getNespravneudaje());
        if (include != spravniObvodConfig.isDatumvzniku()) spravniObvodDto.setDatumvzniku(spravniObvodFromDb.getDatumvzniku());
    }
    //endregion
}
