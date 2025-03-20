package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.RegionSoudrznostiBoolean;
import cca.ruian_puller.download.dto.RegionSoudrznostiDto;
import cca.ruian_puller.download.repository.StatRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cca.ruian_puller.download.repository.RegionSoudrznostiRepository;

import java.util.List;

@Service
@Log4j2
public class RegionSoudrznostiService {

    private final RegionSoudrznostiRepository regionSoudrznostiRepository;
    private final StatRepository statRepository;

    @Autowired
    public RegionSoudrznostiService(RegionSoudrznostiRepository regionSoudrznostiRepository, StatRepository statRepository) {
        this.regionSoudrznostiRepository = regionSoudrznostiRepository;
        this.statRepository = statRepository;
    }

    public void prepareAndSave(List<RegionSoudrznostiDto> regionSoudrznostiDtos, AppConfig appConfig) {
        // Remove all RegionSoudrznostiDto with null Kod
        int initialSize = regionSoudrznostiDtos.size();
        int removedNullKod = 0;
        regionSoudrznostiDtos.removeIf(regionSoudrznostiDto -> regionSoudrznostiDto.getKod() == null);
        removedNullKod += initialSize - regionSoudrznostiDtos.size();

        // Based on RegionSoudrznostiBoolean from AppConfig, filter out RegionSoudrznostiDto
        if (!appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL))
            regionSoudrznostiDtos.forEach(regionSoudrznostiDto -> prepare(regionSoudrznostiDto, appConfig.getRegionSoudrznostiConfig()));

        // Check all foreign keys
        regionSoudrznostiDtos.removeIf(regionSoudrznostiDto -> !checkFK(regionSoudrznostiDto));
        if (initialSize != regionSoudrznostiDtos.size())
            log.warn("{} removed from RegionSoudrznosti due to missing foreign keys", initialSize - regionSoudrznostiDtos.size());
        if (removedNullKod != 0)
            log.warn("{} removed from RegionSoudrznosti due to null Kod", removedNullKod);

        // Split list of RegionSoudrznostiDto into smaller lists
        for (int i = 0; i < regionSoudrznostiDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), regionSoudrznostiDtos.size());
            List<RegionSoudrznostiDto> subList = regionSoudrznostiDtos.subList(i, toIndex);
            regionSoudrznostiRepository.saveAll(subList);
            log.info("Saved {} out of {} RegionSoudrznosti", toIndex, regionSoudrznostiDtos.size());
        }
    }

    // Check if all foreign keys exist
    private boolean checkFK(RegionSoudrznostiDto regionSoudrznostiDto) {
        // Get the foreign key Kod
        Integer statKod = regionSoudrznostiDto.getStat();

        // Check if the foreign key Kod for Stat exists
        if (statKod != null && !statRepository.existsByKod(statKod)) {
            log.warn("RegionSoudrznosti with Kod {} does not have valid foreign keys: Stat with Kod {}", regionSoudrznostiDto.getKod(), statKod);
            return false;
        }

        return true;
    }
    //endregion

    //region Prepare with RegionSoudrznostiBoolean
    private void prepare(RegionSoudrznostiDto regionSoudrznostiDto, RegionSoudrznostiBoolean regionSoudrznostiConfig) {
        // Check if this dto is in db already
        RegionSoudrznostiDto regionSoudrznostiFromDb = regionSoudrznostiRepository.findByKod(regionSoudrznostiDto.getKod());
        includeOrExclude(regionSoudrznostiFromDb, regionSoudrznostiDto, regionSoudrznostiConfig);
    }

    private void includeOrExclude(RegionSoudrznostiDto regionSoudrznostiFromDb, RegionSoudrznostiDto regionSoudrznostiDto, RegionSoudrznostiBoolean regionSoudrznostiConfig) {
        if (regionSoudrznostiFromDb == null) {
            if (regionSoudrznostiConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE)) {
                setRegionSoudrznostiDtoFields(regionSoudrznostiDto, regionSoudrznostiConfig, true);
            } else if (regionSoudrznostiConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_EXCLUDE)) {
                setRegionSoudrznostiDtoFields(regionSoudrznostiDto, regionSoudrznostiConfig, false);
            }
        } else {
            if (regionSoudrznostiConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE)) {
                setRegionSoudrznostiDtoFieldsCombinedDB(regionSoudrznostiDto, regionSoudrznostiFromDb, regionSoudrznostiConfig, true);
            } else if (regionSoudrznostiConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_EXCLUDE)) {
                setRegionSoudrznostiDtoFieldsCombinedDB(regionSoudrznostiDto, regionSoudrznostiFromDb, regionSoudrznostiConfig, false);
            }
        }
    }

    private void setRegionSoudrznostiDtoFields(RegionSoudrznostiDto regionSoudrznostiDto, RegionSoudrznostiBoolean regionSoudrznostiConfig, boolean include) {
        if (include != regionSoudrznostiConfig.isNazev())
            regionSoudrznostiDto.setNazev(null);
        if (include != regionSoudrznostiConfig.isNespravny())
            regionSoudrznostiDto.setNespravny(null);
        if (include != regionSoudrznostiConfig.isStat())
            regionSoudrznostiDto.setStat(null);
        if (include != regionSoudrznostiConfig.isPlatiod())
            regionSoudrznostiDto.setPlatiod(null);
        if (include != regionSoudrznostiConfig.isPlatido())
            regionSoudrznostiDto.setPlatido(null);
        if (include != regionSoudrznostiConfig.isIdtransakce())
            regionSoudrznostiDto.setIdtransakce(null);
        if (include != regionSoudrznostiConfig.isGlobalniidnavrhuzmeny())
            regionSoudrznostiDto.setGlobalniidnavrhuzmeny(null);
        if (include != regionSoudrznostiConfig.isNutslau())
            regionSoudrznostiDto.setNutslau(null);
        if (include != regionSoudrznostiConfig.isGeometriedefbod())
            regionSoudrznostiDto.setGeometriedefbod(null);
        if (include != regionSoudrznostiConfig.isGeometriegenhranice())
            regionSoudrznostiDto.setGeometriegenhranice(null);
        if (include != regionSoudrznostiConfig.isGeometrieorihranice())
            regionSoudrznostiDto.setGeometrieorihranice(null);
        if (include != regionSoudrznostiConfig.isNespravneudaje())
            regionSoudrznostiDto.setNespravneudaje(null);
        if (include != regionSoudrznostiConfig.isDatumvzniku())
            regionSoudrznostiDto.setDatumvzniku(null);
    }

    private void setRegionSoudrznostiDtoFieldsCombinedDB(RegionSoudrznostiDto regionSoudrznostiDto, RegionSoudrznostiDto regionSoudrznostiFromDb, RegionSoudrznostiBoolean regionSoudrznostiConfig, boolean include) {
        if (regionSoudrznostiFromDb.getNazev() != null && (include == regionSoudrznostiConfig.isNazev()))
            regionSoudrznostiDto.setNazev(regionSoudrznostiFromDb.getNazev());
        if (regionSoudrznostiFromDb.getNespravny() != null && (include == regionSoudrznostiConfig.isNespravny()))
            regionSoudrznostiDto.setNespravny(regionSoudrznostiFromDb.getNespravny());
        if (regionSoudrznostiFromDb.getStat() != null && (include == regionSoudrznostiConfig.isStat()))
            regionSoudrznostiDto.setStat(regionSoudrznostiFromDb.getStat());
        if (regionSoudrznostiFromDb.getPlatiod() != null && (include == regionSoudrznostiConfig.isPlatiod()))
            regionSoudrznostiDto.setPlatiod(regionSoudrznostiFromDb.getPlatiod());
        if (regionSoudrznostiFromDb.getPlatido() != null && (include == regionSoudrznostiConfig.isPlatido()))
            regionSoudrznostiDto.setPlatido(regionSoudrznostiFromDb.getPlatido());
        if (regionSoudrznostiFromDb.getIdtransakce() != null && (include == regionSoudrznostiConfig.isIdtransakce()))
            regionSoudrznostiDto.setIdtransakce(regionSoudrznostiFromDb.getIdtransakce());
        if (regionSoudrznostiFromDb.getGlobalniidnavrhuzmeny() != null && (include == regionSoudrznostiConfig.isGlobalniidnavrhuzmeny()))
            regionSoudrznostiDto.setGlobalniidnavrhuzmeny(regionSoudrznostiFromDb.getGlobalniidnavrhuzmeny());
        if (regionSoudrznostiFromDb.getNutslau() != null && (include == regionSoudrznostiConfig.isNutslau()))
            regionSoudrznostiDto.setNutslau(regionSoudrznostiFromDb.getNutslau());
        if (regionSoudrznostiFromDb.getGeometriedefbod() != null && (include == regionSoudrznostiConfig.isGeometriedefbod()))
            regionSoudrznostiDto.setGeometriedefbod(regionSoudrznostiFromDb.getGeometriedefbod());
        if (regionSoudrznostiFromDb.getGeometriegenhranice() != null && (include == regionSoudrznostiConfig.isGeometriegenhranice()))
            regionSoudrznostiDto.setGeometriegenhranice(regionSoudrznostiFromDb.getGeometriegenhranice());
        if (regionSoudrznostiFromDb.getGeometrieorihranice() != null && (include == regionSoudrznostiConfig.isGeometrieorihranice()))
            regionSoudrznostiDto.setGeometrieorihranice(regionSoudrznostiFromDb.getGeometrieorihranice());
        if (regionSoudrznostiFromDb.getNespravneudaje() != null && (include == regionSoudrznostiConfig.isNespravneudaje()))
            regionSoudrznostiDto.setNespravneudaje(regionSoudrznostiFromDb.getNespravneudaje());
        if (regionSoudrznostiFromDb.getDatumvzniku() != null && (include == regionSoudrznostiConfig.isDatumvzniku()))
            regionSoudrznostiDto.setDatumvzniku(regionSoudrznostiFromDb.getDatumvzniku());
    }
    //endregion
}
