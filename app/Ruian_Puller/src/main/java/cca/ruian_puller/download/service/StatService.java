package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.StatBoolean;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cca.ruian_puller.download.dto.StatDto;
import cca.ruian_puller.download.repository.StatRepository;

import java.util.List;

@Service
@Log4j2
public class StatService {

    private final StatRepository statRepository;

    @Autowired
    public StatService(StatRepository statRepository) {
        this.statRepository = statRepository;
    }

    public void prepareAndSave(List<StatDto> statDtos, AppConfig appConfig) {
        int initialSize = statDtos.size();
        int removedNullKod = 0;
        statDtos.removeIf(statDto -> statDto.getKod() == null);
        removedNullKod += initialSize - statDtos.size();

        // Based on StatBoolean from AppConfig, filter out StatDto
        if (!appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL))
            statDtos.forEach(statDto -> prepare(statDto, appConfig.getStatConfig()));

        // Split list of StatDto into smaller lists
        for (int i = 0; i < statDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), statDtos.size());
            List<StatDto> subList = statDtos.subList(i, toIndex);
            statRepository.saveAll(subList);
            log.info("Saved {} out of {} Stat", toIndex, statDtos.size());
        }
        if (removedNullKod != 0)
            log.warn("{} removed from Stat due to null Kod", removedNullKod);
    }

    //region Prepare with StatBoolean
    private void prepare(StatDto statDto, StatBoolean statConfig) {
        // Check if this dto is in db already
        StatDto statFromDb = statRepository.findByKod(statDto.getKod());
        includeOrExclude(statFromDb, statDto, statConfig);
    }

    private void includeOrExclude(StatDto statFromDb, StatDto statDto, StatBoolean statConfig) {
        if (statFromDb == null) {
            if (statConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE)) {
                setStatDtoFields(statDto, statConfig, true);
            } else if (statConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_EXCLUDE)) {
                setStatDtoFields(statDto, statConfig, false);
            }
        } else {
            if (statConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE)) {
                setStatDtoFieldsCombinedDB(statDto, statFromDb, statConfig, true);
            } else if (statConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_EXCLUDE)) {
                setStatDtoFieldsCombinedDB(statDto, statFromDb, statConfig, false);
            }
        }
    }

    private void setStatDtoFields(StatDto statDto, StatBoolean statConfig, boolean include) {
        if (include != statConfig.isNazev())
            statDto.setNazev(null);
        if (include != statConfig.isNespravny())
            statDto.setNespravny(null);
        if (include != statConfig.isPlatiod())
            statDto.setPlatiod(null);
        if (include != statConfig.isPlatido())
            statDto.setPlatido(null);
        if (include != statConfig.isIdtransakce())
            statDto.setIdtransakce(null);
        if (include != statConfig.isGlobalniidnavrhuzmeny())
            statDto.setGlobalniidnavrhuzmeny(null);
        if (include != statConfig.isNutslau())
            statDto.setNutslau(null);
        if (include != statConfig.isGeometriedefbod())
            statDto.setGeometriedefbod(null);
        if (include != statConfig.isGeometriegenhranice())
            statDto.setGeometriegenhranice(null);
        if (include != statConfig.isGeometrieorihranice())
            statDto.setGeometrieorihranice(null);
        if (include != statConfig.isNespravneudaje())
            statDto.setNespravneudaje(null);
        if (include != statConfig.isDatumvzniku())
            statDto.setDatumvzniku(null);
    }

    private void setStatDtoFieldsCombinedDB(StatDto statDto, StatDto statFromDb, StatBoolean statConfig, boolean include) {
        if (statFromDb.getNazev() != null && (include == statConfig.isNazev()))
            statDto.setNazev(statFromDb.getNazev());
        if (statFromDb.getNespravny() != null && (include == statConfig.isNespravny()))
            statDto.setNespravny(statFromDb.getNespravny());
        if (statFromDb.getPlatiod() != null && (include == statConfig.isPlatiod()))
            statDto.setPlatiod(statFromDb.getPlatiod());
        if (statFromDb.getPlatido() != null && (include == statConfig.isPlatido()))
            statDto.setPlatido(statFromDb.getPlatido());
        if (statFromDb.getIdtransakce() != null && (include == statConfig.isIdtransakce()))
            statDto.setIdtransakce(statFromDb.getIdtransakce());
        if (statFromDb.getGlobalniidnavrhuzmeny() != null && (include == statConfig.isGlobalniidnavrhuzmeny()))
            statDto.setGlobalniidnavrhuzmeny(statFromDb.getGlobalniidnavrhuzmeny());
        if (statFromDb.getNutslau() != null && (include == statConfig.isNutslau()))
            statDto.setNutslau(statFromDb.getNutslau());
        if (statFromDb.getGeometriedefbod() != null && (include == statConfig.isGeometriedefbod()))
            statDto.setGeometriedefbod(statFromDb.getGeometriedefbod());
        if (statFromDb.getGeometriegenhranice() != null && (include == statConfig.isGeometriegenhranice()))
            statDto.setGeometriegenhranice(statFromDb.getGeometriegenhranice());
        if (statFromDb.getGeometrieorihranice() != null && (include == statConfig.isGeometrieorihranice()))
            statDto.setGeometrieorihranice(statFromDb.getGeometrieorihranice());
        if (statFromDb.getNespravneudaje() != null && (include == statConfig.isNespravneudaje()))
            statDto.setNespravneudaje(statFromDb.getNespravneudaje());
        if (statFromDb.getDatumvzniku() != null && (include == statConfig.isDatumvzniku()))
            statDto.setDatumvzniku(statFromDb.getDatumvzniku());
    }
    //endregion
}