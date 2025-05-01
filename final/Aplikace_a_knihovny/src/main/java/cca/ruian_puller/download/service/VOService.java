package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.VOBoolean;
import cca.ruian_puller.download.dto.VODto;
import cca.ruian_puller.download.repository.MomcRepository;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.VORepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class VOService {
    // Repositories
    private final VORepository voRepository;
    private final ObecRepository obecRepository;
    private final MomcRepository momcRepository;

    /**
     * Constructor for VOService.
     *
     * @param voRepository  the repository for VO
     * @param obecRepository the repository for Obec
     * @param momcRepository the repository for Momc
     */
    @Autowired
    public VOService(VORepository voRepository, ObecRepository obecRepository, MomcRepository momcRepository) {
        this.voRepository = voRepository;
        this.obecRepository = obecRepository;
        this.momcRepository = momcRepository;
    }

    /**
     * Prepares and saves VODtos to the database.
     *
     * @param voDtos   the list of VODtos to be saved
     * @param appConfig the application configuration
     */
    public void prepareAndSave(List<VODto> voDtos, AppConfig appConfig) {
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<VODto> toDelete = new java.util.ArrayList<>();
        voDtos.forEach(voDto -> {
            iterator.getAndIncrement();
            // Remove all VO with null Kod
            if (voDto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(voDto);
                return;
            }
            // If dto is in db already, select it
            VODto voFromDb = voRepository.findByKod(voDto.getKod());
            if (voFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(voDto, voFromDb);
            } else if (appConfig.getVoConfig() != null && !appConfig.getVoConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(voDto, voFromDb, appConfig.getVoConfig());
            }
            // Check if the foreign key is valid
            if (!checkFK(voDto)) {
                removedByFK.getAndIncrement();
                toDelete.add(voDto);
                return;
            }

            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= voDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of VoDtos processed");
            }
            if (iterator.get() >= voDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of VoDtos processed");
            }
            if (iterator.get() >= voDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of VoDtos processed");
            }
            if (iterator.get() >= voDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of VDtos processed");
            }
        });

        // Remove all invalid VODtos
        voDtos.removeAll(toDelete);

        // Log if some VODto were removed
        if (removedByNullKod.get() > 0) log.warn("Removed {} VO with null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.warn("Removed {} VO with invalid foreign keys", removedByFK.get());

        // Save VODtos to db
        for (int i = 0; i < voDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), voDtos.size());
            List<VODto> subList = voDtos.subList(i, toIndex);
            voRepository.saveAll(subList);
            log.info("Saved {} out of {} VO", toIndex, voDtos.size());
        }
    }

    /**
     * Checks if the foreign keys in the VODto are valid.
     *
     * @param voDto the VODto to check
     * @return true if all foreign keys are valid, false otherwise
     */
    private boolean checkFK(VODto voDto) {
        // Get the foreign keys Kod
        Integer obecKod = voDto.getObec();
        Integer momcKod = voDto.getMomc();

        // Check if the foreign key Kod for Obec exists
        if (obecKod != null && !obecRepository.existsByKod(obecKod)) {
            log.warn("VO with Kod {} does not have a valid foreign key: Obec with Kod {}", voDto.getKod(), obecKod);
            return false;
        }

        // Check if the foreign key Kod for Momc exists
        if (momcKod != null && !momcRepository.existsByKod(momcKod)) {
            log.warn("VO with Kod {} does not have a valid foreign key: Momc with Kod {}", voDto.getKod(), momcKod);
            return false;
        }

        return true;
    }

    /**
     * Updates the VODto with values from the database if they are null.
     *
     * @param voDto    the VODto to update
     * @param voFromDb the VODto from the database
     */
    private void updateWithDbValues(VODto voDto, VODto voFromDb) {
        if (voDto.getPlatiod() == null) voDto.setPlatiod(voFromDb.getPlatiod());
        if (voDto.getPlatido() == null) voDto.setPlatido(voFromDb.getPlatido());
        if (voDto.getIdtransakce() == null) voDto.setIdtransakce(voFromDb.getIdtransakce());
        if (voDto.getGlobalniidnavrhuzmeny() == null) voDto.setGlobalniidnavrhuzmeny(voFromDb.getGlobalniidnavrhuzmeny());
        if (voDto.getGeometriedefbod() == null) voDto.setGeometriedefbod(voFromDb.getGeometriedefbod());
        if (voDto.getGeometriegenhranice() == null) voDto.setGeometriegenhranice(voFromDb.getGeometriegenhranice());
        if (voDto.getGeometrieorihranice() == null) voDto.setGeometrieorihranice(voFromDb.getGeometrieorihranice());
        if (voDto.getNespravneudaje() == null) voDto.setNespravneudaje(voFromDb.getNespravneudaje());
        if (voDto.getCislo() == null) voDto.setCislo(voFromDb.getCislo());
        if (voDto.getNespravny() == null) voDto.setNespravny(voFromDb.getNespravny());
        if (voDto.getObec() == null) voDto.setObec(voFromDb.getObec());
        if (voDto.getMomc() == null) voDto.setMomc(voFromDb.getMomc());
        if (voDto.getPoznamka() == null) voDto.setPoznamka(voFromDb.getPoznamka());
    }

    //region Prepare with VOBoolean
    /**
     * Prepares the VODto with values from the VOBoolean configuration.
     *
     * @param voDto      the VODto to prepare
     * @param voFromDb   the VODto from the database
     * @param voConfig   the VOBoolean configuration
     */
    private void prepare(VODto voDto, VODto voFromDb, VOBoolean voConfig) {
        boolean include = voConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (voFromDb == null) {
            setVODtoFields(voDto, voConfig, include);
        } else {
            setVODtoFieldsCombinedDB(voDto, voFromDb, voConfig, include);
        }
    }

    /**
     * Sets the fields of the VODto based on the VOBoolean configuration.
     *
     * @param voDto    the VODto to set fields for
     * @param voConfig the VOBoolean configuration
     * @param include  whether to include or exclude the fields
     */
    private void setVODtoFields(VODto voDto, VOBoolean voConfig, boolean include) {
        if (include != voConfig.isPlatiod()) voDto.setPlatiod(null);
        if (include != voConfig.isPlatido()) voDto.setPlatido(null);
        if (include != voConfig.isIdtransakce()) voDto.setIdtransakce(null);
        if (include != voConfig.isGlobalniidnavrhuzmeny()) voDto.setGlobalniidnavrhuzmeny(null);
        if (include != voConfig.isGeometriedefbod()) voDto.setGeometriedefbod(null);
        if (include != voConfig.isGeometriegenhranice()) voDto.setGeometriegenhranice(null);
        if (include != voConfig.isGeometrieorihranice()) voDto.setGeometrieorihranice(null);
        if (include != voConfig.isNespravneudaje()) voDto.setNespravneudaje(null);
        if (include != voConfig.isCislo()) voDto.setCislo(null);
        if (include != voConfig.isNespravny()) voDto.setNespravny(null);
        if (include != voConfig.isObec()) voDto.setObec(null);
        if (include != voConfig.isMomc()) voDto.setMomc(null);
        if (include != voConfig.isPoznamka()) voDto.setPoznamka(null);
    }

    /**
     * Sets the fields of the VODto based on the VOBoolean configuration and the values from the database.
     *
     * @param voDto      the VODto to set fields for
     * @param voFromDb   the VODto from the database
     * @param voConfig   the VOBoolean configuration
     * @param include    whether to include or exclude the fields
     */
    private void setVODtoFieldsCombinedDB(VODto voDto, VODto voFromDb, VOBoolean voConfig, boolean include) {
        if (include != voConfig.isPlatiod()) voDto.setPlatiod(voFromDb.getPlatiod());
        if (include != voConfig.isPlatido()) voDto.setPlatido(voFromDb.getPlatido());
        if (include != voConfig.isIdtransakce()) voDto.setIdtransakce(voFromDb.getIdtransakce());
        if (include != voConfig.isGlobalniidnavrhuzmeny()) voDto.setGlobalniidnavrhuzmeny(voFromDb.getGlobalniidnavrhuzmeny());
        if (include != voConfig.isGeometriedefbod()) voDto.setGeometriedefbod(voFromDb.getGeometriedefbod());
        if (include != voConfig.isGeometriegenhranice()) voDto.setGeometriegenhranice(voFromDb.getGeometriegenhranice());
        if (include != voConfig.isGeometrieorihranice()) voDto.setGeometrieorihranice(voFromDb.getGeometrieorihranice());
        if (include != voConfig.isNespravneudaje()) voDto.setNespravneudaje(voFromDb.getNespravneudaje());
        if (include != voConfig.isCislo()) voDto.setCislo(voFromDb.getCislo());
        if (include != voConfig.isNespravny()) voDto.setNespravny(voFromDb.getNespravny());
        if (include != voConfig.isObec()) voDto.setObec(voFromDb.getObec());
        if (include != voConfig.isMomc()) voDto.setMomc(voFromDb.getMomc());
        if (include != voConfig.isPoznamka()) voDto.setPoznamka(voFromDb.getPoznamka());
    }
    //endregion
}
