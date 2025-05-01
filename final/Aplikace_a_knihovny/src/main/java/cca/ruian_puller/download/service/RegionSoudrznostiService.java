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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class RegionSoudrznostiService {
    // Repositories
    private final RegionSoudrznostiRepository regionSoudrznostiRepository;
    private final StatRepository statRepository;

    /**
     * Constructor for RegionSoudrznostiService.
     *
     * @param regionSoudrznostiRepository the repository for RegionSoudrznosti
     * @param statRepository             the repository for Stat
     */
    @Autowired
    public RegionSoudrznostiService(RegionSoudrznostiRepository regionSoudrznostiRepository, StatRepository statRepository) {
        this.regionSoudrznostiRepository = regionSoudrznostiRepository;
        this.statRepository = statRepository;
    }

    /**
     * Prepares and saves a list of RegionSoudrznostiDto objects to the database.
     *
     * @param regionSoudrznostiDtos the list of RegionSoudrznostiDto objects to be saved
     * @param appConfig             the application configuration
     */
    public void prepareAndSave(List<RegionSoudrznostiDto> regionSoudrznostiDtos, AppConfig appConfig) {
        // Remove all RegionSoudrznostiDto with null Kod
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<RegionSoudrznostiDto> toDelete = new ArrayList<>();
        regionSoudrznostiDtos.forEach(regionSoudrznostiDto -> {
            iterator.getAndIncrement();
            if (regionSoudrznostiDto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(regionSoudrznostiDto);
                return;
            }
            // If dto is in db already, select it
            RegionSoudrznostiDto regionSoudrznostiFromDb = regionSoudrznostiRepository.findByKod(regionSoudrznostiDto.getKod());
            if (regionSoudrznostiFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(regionSoudrznostiDto, regionSoudrznostiFromDb);
            } else if (appConfig.getRegionSoudrznostiConfig() != null && !appConfig.getRegionSoudrznostiConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(regionSoudrznostiDto, regionSoudrznostiFromDb, appConfig.getRegionSoudrznostiConfig());
            }
            // Check if all foreign keys exist
            if (!checkFK(regionSoudrznostiDto)) {
                removedByFK.getAndIncrement();
                toDelete.add(regionSoudrznostiDto);
                return;
            }
            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= regionSoudrznostiDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of RegionSoudrznostiDtos processed");
            }
            if (iterator.get() >= regionSoudrznostiDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of RegionSoudrznostiDtos processed");
            }
            if (iterator.get() >= regionSoudrznostiDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of RegionSoudrznostiDtos processed");
            }
            if (iterator.get() >= regionSoudrznostiDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of RegionSoudrznostiDtos processed");
            }
        });

        // Remove all invalid RegionSoudrznostiDtos
        regionSoudrznostiDtos.removeAll(toDelete);

        // Log if some RegionSoudrznostiDto were removed
        if (removedByNullKod.get() > 0) log.warn("{} removed from RegionSoudrznosti due to null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.warn("{} removed from RegionSoudrznosti due to missing foreign keys", removedByFK.get());

        // Save RegionSoudrznostiDtos to db
        for (int i = 0; i < regionSoudrznostiDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), regionSoudrznostiDtos.size());
            List<RegionSoudrznostiDto> subList = regionSoudrznostiDtos.subList(i, toIndex);
            regionSoudrznostiRepository.saveAll(subList);
            log.info("Saved {} out of {} RegionSoudrznosti", toIndex, regionSoudrznostiDtos.size());
        }
    }

    // Check if all foreign keys exist
    /**
     * Checks if the foreign keys of the given RegionSoudrznostiDto object are valid.
     *
     * @param regionSoudrznostiDto the RegionSoudrznostiDto object to check
     * @return true if all foreign keys are valid, false otherwise
     */
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

    /**
     * Updates the RegionSoudrznostiDto object with values from the database if they are null.
     *
     * @param regionSoudrznostiDto       the RegionSoudrznostiDto object to update
     * @param regionSoudrznostiFromDb   the RegionSoudrznostiDto object from the database
     */
    private void updateWithDbValues(RegionSoudrznostiDto regionSoudrznostiDto, RegionSoudrznostiDto regionSoudrznostiFromDb) {
        if (regionSoudrznostiDto.getNazev() == null) regionSoudrznostiDto.setNazev(regionSoudrznostiFromDb.getNazev());
        if (regionSoudrznostiDto.getNespravny() == null) regionSoudrznostiDto.setNespravny(regionSoudrznostiFromDb.getNespravny());
        if (regionSoudrznostiDto.getStat() == null) regionSoudrznostiDto.setStat(regionSoudrznostiFromDb.getStat());
        if (regionSoudrznostiDto.getPlatiod() == null) regionSoudrznostiDto.setPlatiod(regionSoudrznostiFromDb.getPlatiod());
        if (regionSoudrznostiDto.getPlatido() == null) regionSoudrznostiDto.setPlatido(regionSoudrznostiFromDb.getPlatido());
        if (regionSoudrznostiDto.getIdtransakce() == null) regionSoudrznostiDto.setIdtransakce(regionSoudrznostiFromDb.getIdtransakce());
        if (regionSoudrznostiDto.getGlobalniidnavrhuzmeny() == null) regionSoudrznostiDto.setGlobalniidnavrhuzmeny(regionSoudrznostiFromDb.getGlobalniidnavrhuzmeny());
        if (regionSoudrznostiDto.getNutslau() == null) regionSoudrznostiDto.setNutslau(regionSoudrznostiFromDb.getNutslau());
        if (regionSoudrznostiDto.getGeometriedefbod() == null) regionSoudrznostiDto.setGeometriedefbod(regionSoudrznostiFromDb.getGeometriedefbod());
        if (regionSoudrznostiDto.getGeometriegenhranice() == null) regionSoudrznostiDto.setGeometriegenhranice(regionSoudrznostiFromDb.getGeometriegenhranice());
        if (regionSoudrznostiDto.getGeometrieorihranice() == null) regionSoudrznostiDto.setGeometrieorihranice(regionSoudrznostiFromDb.getGeometrieorihranice());
        if (regionSoudrznostiDto.getNespravneudaje() == null) regionSoudrznostiDto.setNespravneudaje(regionSoudrznostiFromDb.getNespravneudaje());
        if (regionSoudrznostiDto.getDatumvzniku() == null) regionSoudrznostiDto.setDatumvzniku(regionSoudrznostiFromDb.getDatumvzniku());
    }

    //region Prepare with RegionSoudrznostiBoolean
    /**
     * Prepares the RegionSoudrznostiDto object based on the given RegionSoudrznostiBoolean configuration.
     *
     * @param regionSoudrznostiDto          the RegionSoudrznostiDto object to prepare
     * @param regionSoudrznostiFromDb      the RegionSoudrznostiDto object from the database
     * @param regionSoudrznostiConfig      the RegionSoudrznostiBoolean configuration
     */
    private void prepare(RegionSoudrznostiDto regionSoudrznostiDto, RegionSoudrznostiDto regionSoudrznostiFromDb, RegionSoudrznostiBoolean regionSoudrznostiConfig) {
        boolean include = regionSoudrznostiConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (regionSoudrznostiFromDb == null) {
            setRegionSoudrznostiDtoFields(regionSoudrznostiDto, regionSoudrznostiConfig, include);
        } else {
            setRegionSoudrznostiDtoFieldsCombinedDB(regionSoudrznostiDto, regionSoudrznostiFromDb, regionSoudrznostiConfig, include);
        }
    }

    /**
     * Sets the fields of the RegionSoudrznostiDto object based on the given RegionSoudrznostiBoolean configuration.
     *
     * @param regionSoudrznostiDto       the RegionSoudrznostiDto object to set fields for
     * @param regionSoudrznostiConfig   the RegionSoudrznostiBoolean configuration
     * @param include                   whether to include or exclude the fields
     */
    private void setRegionSoudrznostiDtoFields(RegionSoudrznostiDto regionSoudrznostiDto, RegionSoudrznostiBoolean regionSoudrznostiConfig, boolean include) {
        if (include != regionSoudrznostiConfig.isNazev()) regionSoudrznostiDto.setNazev(null);
        if (include != regionSoudrznostiConfig.isNespravny()) regionSoudrznostiDto.setNespravny(null);
        if (include != regionSoudrznostiConfig.isStat()) regionSoudrznostiDto.setStat(null);
        if (include != regionSoudrznostiConfig.isPlatiod()) regionSoudrznostiDto.setPlatiod(null);
        if (include != regionSoudrznostiConfig.isPlatido()) regionSoudrznostiDto.setPlatido(null);
        if (include != regionSoudrznostiConfig.isIdtransakce()) regionSoudrznostiDto.setIdtransakce(null);
        if (include != regionSoudrznostiConfig.isGlobalniidnavrhuzmeny()) regionSoudrznostiDto.setGlobalniidnavrhuzmeny(null);
        if (include != regionSoudrznostiConfig.isNutslau()) regionSoudrznostiDto.setNutslau(null);
        if (include != regionSoudrznostiConfig.isGeometriedefbod()) regionSoudrznostiDto.setGeometriedefbod(null);
        if (include != regionSoudrznostiConfig.isGeometriegenhranice()) regionSoudrznostiDto.setGeometriegenhranice(null);
        if (include != regionSoudrznostiConfig.isGeometrieorihranice()) regionSoudrznostiDto.setGeometrieorihranice(null);
        if (include != regionSoudrznostiConfig.isNespravneudaje()) regionSoudrznostiDto.setNespravneudaje(null);
        if (include != regionSoudrznostiConfig.isDatumvzniku()) regionSoudrznostiDto.setDatumvzniku(null);
    }

    /**
     * Sets the fields of the RegionSoudrznostiDto object based on the given RegionSoudrznostiBoolean configuration and existing values in the database.
     *
     * @param regionSoudrznostiDto          the RegionSoudrznostiDto object to set fields for
     * @param regionSoudrznostiFromDb      the RegionSoudrznostiDto object from the database
     * @param regionSoudrznostiConfig      the RegionSoudrznostiBoolean configuration
     * @param include                      whether to include or exclude the fields
     */
    private void setRegionSoudrznostiDtoFieldsCombinedDB(RegionSoudrznostiDto regionSoudrznostiDto, RegionSoudrznostiDto regionSoudrznostiFromDb, RegionSoudrznostiBoolean regionSoudrznostiConfig, boolean include) {
        if (include != regionSoudrznostiConfig.isNazev()) regionSoudrznostiDto.setNazev(regionSoudrznostiFromDb.getNazev());
        if (include != regionSoudrznostiConfig.isNespravny()) regionSoudrznostiDto.setNespravny(regionSoudrznostiFromDb.getNespravny());
        if (include != regionSoudrznostiConfig.isStat()) regionSoudrznostiDto.setStat(regionSoudrznostiFromDb.getStat());
        if (include != regionSoudrznostiConfig.isPlatiod()) regionSoudrznostiDto.setPlatiod(regionSoudrznostiFromDb.getPlatiod());
        if (include != regionSoudrznostiConfig.isPlatido()) regionSoudrznostiDto.setPlatido(regionSoudrznostiFromDb.getPlatido());
        if (include != regionSoudrznostiConfig.isIdtransakce()) regionSoudrznostiDto.setIdtransakce(regionSoudrznostiFromDb.getIdtransakce());
        if (include != regionSoudrznostiConfig.isGlobalniidnavrhuzmeny()) regionSoudrznostiDto.setGlobalniidnavrhuzmeny(regionSoudrznostiFromDb.getGlobalniidnavrhuzmeny());
        if (include != regionSoudrznostiConfig.isNutslau()) regionSoudrznostiDto.setNutslau(regionSoudrznostiFromDb.getNutslau());
        if (include != regionSoudrznostiConfig.isGeometriedefbod()) regionSoudrznostiDto.setGeometriedefbod(regionSoudrznostiFromDb.getGeometriedefbod());
        if (include != regionSoudrznostiConfig.isGeometriegenhranice()) regionSoudrznostiDto.setGeometriegenhranice(regionSoudrznostiFromDb.getGeometriegenhranice());
        if (include != regionSoudrznostiConfig.isGeometrieorihranice()) regionSoudrznostiDto.setGeometrieorihranice(regionSoudrznostiFromDb.getGeometrieorihranice());
        if (include != regionSoudrznostiConfig.isNespravneudaje()) regionSoudrznostiDto.setNespravneudaje(regionSoudrznostiFromDb.getNespravneudaje());
        if (include != regionSoudrznostiConfig.isDatumvzniku()) regionSoudrznostiDto.setDatumvzniku(regionSoudrznostiFromDb.getDatumvzniku());
    }
    //endregion
}
