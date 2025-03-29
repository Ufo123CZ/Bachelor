package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.StatBoolean;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cca.ruian_puller.download.dto.StatDto;
import cca.ruian_puller.download.repository.StatRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class StatService {

    private final StatRepository statRepository;

    @Autowired
    public StatService(StatRepository statRepository) {
        this.statRepository = statRepository;
    }

    public void prepareAndSave(List<StatDto> statDtos, AppConfig appConfig) {
        // Remove all StatDto with null Kod
        AtomicInteger removedByNullKod = new AtomicInteger();
        List<StatDto> toDelete = new ArrayList<>();
        statDtos.forEach(statDto -> {
            if (statDto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(statDto);
                return;
            }
            // If dto is in db already, select it
            StatDto statFromDb = statRepository.findByKod(statDto.getKod());
            if (statFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(statDto, statFromDb);
            } else if (appConfig.getStatConfig() != null && !appConfig.getStatConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(statDto, statFromDb, appConfig.getStatConfig());
            }
        });
        // Remove all invalid StatDtos
        statDtos.removeAll(toDelete);
        // Log if some StatDto were removed
        if (removedByNullKod.get() > 0) log.info("Removed {} StatDto with null Kod", removedByNullKod.get());

        // Save StatDtos to db
        for (int i = 0; i < statDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), statDtos.size());
            List<StatDto> subList = statDtos.subList(i, toIndex);
            statRepository.saveAll(subList);
            log.info("Saved {} out of {} Stat", toIndex, statDtos.size());
        }
    }

    private void updateWithDbValues(StatDto statDto, StatDto statFromDb) {
        if (statDto.getNazev() == null) statDto.setNazev(statFromDb.getNazev());
        if (statDto.getNespravny() == null) statDto.setNespravny(statFromDb.getNespravny());
        if (statDto.getPlatiod() == null) statDto.setPlatiod(statFromDb.getPlatiod());
        if (statDto.getPlatido() == null) statDto.setPlatido(statFromDb.getPlatido());
        if (statDto.getIdtransakce() == null) statDto.setIdtransakce(statFromDb.getIdtransakce());
        if (statDto.getGlobalniidnavrhuzmeny() == null) statDto.setGlobalniidnavrhuzmeny(statFromDb.getGlobalniidnavrhuzmeny());
        if (statDto.getNutslau() == null) statDto.setNutslau(statFromDb.getNutslau());
        if (statDto.getGeometriedefbod() == null) statDto.setGeometriedefbod(statFromDb.getGeometriedefbod());
        if (statDto.getGeometriegenhranice() == null) statDto.setGeometriegenhranice(statFromDb.getGeometriegenhranice());
        if (statDto.getGeometrieorihranice() == null) statDto.setGeometrieorihranice(statFromDb.getGeometrieorihranice());
        if (statDto.getNespravneudaje() == null) statDto.setNespravneudaje(statFromDb.getNespravneudaje());
        if (statDto.getDatumvzniku() == null) statDto.setDatumvzniku(statFromDb.getDatumvzniku());
    }

    //region Prepare with StatBoolean
    private void prepare(StatDto statDto, StatDto statFromDb, StatBoolean statConfig) {
        // Check if this dto is in db already
        boolean include = statConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (statFromDb == null) {
            setStatDtoFields(statDto, statConfig, include);
        } else {
            setStatDtoFieldsCombinedDB(statDto, statFromDb, statConfig, include);
        }
    }

    private void setStatDtoFields(StatDto statDto, StatBoolean statConfig, boolean include) {
        if (include != statConfig.isNazev()) statDto.setNazev(null);
        if (include != statConfig.isNespravny()) statDto.setNespravny(null);
        if (include != statConfig.isPlatiod()) statDto.setPlatiod(null);
        if (include != statConfig.isPlatido()) statDto.setPlatido(null);
        if (include != statConfig.isIdtransakce()) statDto.setIdtransakce(null);
        if (include != statConfig.isGlobalniidnavrhuzmeny()) statDto.setGlobalniidnavrhuzmeny(null);
        if (include != statConfig.isNutslau()) statDto.setNutslau(null);
        if (include != statConfig.isGeometriedefbod()) statDto.setGeometriedefbod(null);
        if (include != statConfig.isGeometriegenhranice()) statDto.setGeometriegenhranice(null);
        if (include != statConfig.isGeometrieorihranice()) statDto.setGeometrieorihranice(null);
        if (include != statConfig.isNespravneudaje()) statDto.setNespravneudaje(null);
        if (include != statConfig.isDatumvzniku()) statDto.setDatumvzniku(null);
    }

    private void setStatDtoFieldsCombinedDB(StatDto statDto, StatDto statFromDb, StatBoolean statConfig, boolean include) {
        if (include != statConfig.isNazev()) statDto.setNazev(statFromDb.getNazev());
        if (include != statConfig.isNespravny()) statDto.setNespravny(statFromDb.getNespravny());
        if (include != statConfig.isPlatiod()) statDto.setPlatiod(statFromDb.getPlatiod());
        if (include != statConfig.isPlatido()) statDto.setPlatido(statFromDb.getPlatido());
        if (include != statConfig.isIdtransakce()) statDto.setIdtransakce(statFromDb.getIdtransakce());
        if (include != statConfig.isGlobalniidnavrhuzmeny()) statDto.setGlobalniidnavrhuzmeny(statFromDb.getGlobalniidnavrhuzmeny());
        if (include != statConfig.isNutslau()) statDto.setNutslau(statFromDb.getNutslau());
        if (include != statConfig.isGeometriedefbod()) statDto.setGeometriedefbod(statFromDb.getGeometriedefbod());
        if (include != statConfig.isGeometriegenhranice()) statDto.setGeometriegenhranice(statFromDb.getGeometriegenhranice());
        if (include != statConfig.isGeometrieorihranice()) statDto.setGeometrieorihranice(statFromDb.getGeometrieorihranice());
        if (include != statConfig.isNespravneudaje()) statDto.setNespravneudaje(statFromDb.getNespravneudaje());
        if (statConfig.isDatumvzniku()) statDto.setDatumvzniku(statFromDb.getDatumvzniku());
    }
    //endregion
}