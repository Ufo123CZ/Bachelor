package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.VuscBoolean;
import cca.ruian_puller.download.dto.VuscDto;
import cca.ruian_puller.download.repository.RegionSoudrznostiRepository;
import cca.ruian_puller.download.repository.VuscRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class VuscService {
    // Repositories
    private final VuscRepository vuscRepository;
    private final RegionSoudrznostiRepository regionSoudrznostiRepository;

    /**
     * Constructor for VuscService.
     *
     * @param vuscRepository               the repository for Vusc
     * @param regionSoudrznostiRepository   the repository for RegionSoudrznosti
     */
    @Autowired
    public VuscService(VuscRepository vuscRepository, RegionSoudrznostiRepository regionSoudrznostiRepository) {
        this.vuscRepository = vuscRepository;
        this.regionSoudrznostiRepository = regionSoudrznostiRepository;
    }

    /**
     * Prepares and saves VuscDtos to the database.
     *
     * @param vuscDtos   the list of VuscDtos to be saved
     * @param appConfig  the application configuration
     */
    public void prepareAndSave(List<VuscDto> vuscDtos, AppConfig appConfig) {
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<VuscDto> toDelete = new ArrayList<>();
        vuscDtos.forEach(vuscDto -> {
            iterator.getAndIncrement();
            // Remove all Vusc with null Kod
            if (vuscDto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(vuscDto);
                return;
            }
            // If dto is in db already, select it
            VuscDto vuscFromDb = vuscRepository.findByKod(vuscDto.getKod());
            if (vuscFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(vuscDto, vuscFromDb);
            } else if (appConfig.getVuscConfig() != null && !appConfig.getVuscConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(vuscDto, vuscFromDb, appConfig.getVuscConfig());
            }
            // Check if the foreign key is valid
            if (!checkFK(vuscDto)) {
                removedByFK.getAndIncrement();
                toDelete.add(vuscDto);
                return;
            }

            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= vuscDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of VuscDtos processed");
            }
            if (iterator.get() >= vuscDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of VuscDtos processed");
            }
            if (iterator.get() >= vuscDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of VuscDtos processed");
            }
            if (iterator.get() >= vuscDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of VuscDtos processed");
            }
        });

        // Remove all invalid VuscDtos
        vuscDtos.removeAll(toDelete);

        // Log if some VuscDto were removed
        if (removedByNullKod.get() > 0) log.warn("Removed {} Vusc with null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.warn("Removed {} Vusc with invalid foreign keys", removedByFK.get());

        // Save VuscDtos to db
        for (int i = 0; i < vuscDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), vuscDtos.size());
            List<VuscDto> subList = vuscDtos.subList(i, toIndex);
            vuscRepository.saveAll(subList);
            log.info("Saved {} out of {} Vusc", toIndex, vuscDtos.size());
        }
    }

    /**
     * Checks if the foreign keys of the VuscDto are valid.
     *
     * @param vuscDto the VuscDto to be checked
     * @return true if the foreign keys are valid, false otherwise
     */
    private boolean checkFK(VuscDto vuscDto) {
        // Get the foreign keys Kod
        Integer regionSoudrznostiKod = vuscDto.getRegionsoudrznosti();

        // Check if the foreign key Kod for RegionSoudrznosti exists
        if (regionSoudrznostiKod != null && !regionSoudrznostiRepository.existsByKod(regionSoudrznostiKod)) {
            log.warn("Vusc with Kod {} does not have valid foreign keys: RegionSoudrznosti with Kod {}", vuscDto.getKod(), regionSoudrznostiKod);
            return false;
        }

        return true;
    }

    /**
     * Updates the VuscDto with values from the database if they are null.
     *
     * @param vuscDto      the VuscDto to be updated
     * @param vuscFromDb   the VuscDto from the database
     */
    private void updateWithDbValues(VuscDto vuscDto, VuscDto vuscFromDb) {
        if (vuscDto.getNazev() == null) vuscDto.setNazev(vuscFromDb.getNazev());
        if (vuscDto.getNespravny() == null) vuscDto.setNespravny(vuscFromDb.getNespravny());
        if (vuscDto.getRegionsoudrznosti() == null) vuscDto.setRegionsoudrznosti(vuscFromDb.getRegionsoudrznosti());
        if (vuscDto.getPlatiod() == null) vuscDto.setPlatiod(vuscFromDb.getPlatiod());
        if (vuscDto.getPlatido() == null) vuscDto.setPlatido(vuscFromDb.getPlatido());
        if (vuscDto.getIdtransakce() == null) vuscDto.setIdtransakce(vuscFromDb.getIdtransakce());
        if (vuscDto.getGlobalniidnavrhuzmeny() == null) vuscDto.setGlobalniidnavrhuzmeny(vuscFromDb.getGlobalniidnavrhuzmeny());
        if (vuscDto.getNutslau() == null) vuscDto.setNutslau(vuscFromDb.getNutslau());
        if (vuscDto.getGeometriedefbod() == null) vuscDto.setGeometriedefbod(vuscFromDb.getGeometriedefbod());
        if (vuscDto.getGeometriegenhranice() == null) vuscDto.setGeometriegenhranice(vuscFromDb.getGeometriegenhranice());
        if (vuscDto.getGeometrieorihranice() == null) vuscDto.setGeometrieorihranice(vuscFromDb.getGeometrieorihranice());
        if (vuscDto.getNespravneudaje() == null) vuscDto.setNespravneudaje(vuscFromDb.getNespravneudaje());
        if (vuscDto.getDatumvzniku() == null) vuscDto.setDatumvzniku(vuscFromDb.getDatumvzniku());
    }

    //region Prepare with VuscBoolean
    /**
     * Prepares the VuscDto with values from the database if they are null.
     *
     * @param vuscDto      the VuscDto to be prepared
     * @param vuscFromDb   the VuscDto from the database
     * @param vuscConfig   the configuration for Vusc
     */
    private void prepare(VuscDto vuscDto, VuscDto vuscFromDb, VuscBoolean vuscConfig) {
        boolean include = vuscConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL);
        if (vuscFromDb == null) {
            setVuscDtoFields(vuscDto, vuscConfig, include);
        } else {
            setVuscDtoFieldsCombinedDB(vuscDto, vuscFromDb, vuscConfig, include);
        }
    }

    /**
     * Sets the fields of the VuscDto based on the configuration.
     *
     * @param vuscDto      the VuscDto to be set
     * @param vuscConfig   the configuration for Vusc
     * @param include      whether to include or exclude the fields
     */
    private void setVuscDtoFields(VuscDto vuscDto, VuscBoolean vuscConfig, boolean include) {
        if (include != vuscConfig.isNazev()) vuscDto.setNazev(null);
        if (include != vuscConfig.isNespravny()) vuscDto.setNespravny(null);
        if (include != vuscConfig.isRegionsoudrznosti()) vuscDto.setRegionsoudrznosti(null);
        if (include != vuscConfig.isPlatiod()) vuscDto.setPlatiod(null);
        if (include != vuscConfig.isPlatido()) vuscDto.setPlatido(null);
        if (include != vuscConfig.isIdtransakce()) vuscDto.setIdtransakce(null);
        if (include != vuscConfig.isGlobalniidnavrhuzmeny()) vuscDto.setGlobalniidnavrhuzmeny(null);
        if (include != vuscConfig.isNutslau()) vuscDto.setNutslau(null);
        if (include != vuscConfig.isGeometriedefbod()) vuscDto.setGeometriedefbod(null);
        if (include != vuscConfig.isGeometriegenhranice()) vuscDto.setGeometriegenhranice(null);
        if (include != vuscConfig.isGeometrieorihranice()) vuscDto.setGeometrieorihranice(null);
        if (include != vuscConfig.isNespravneudaje()) vuscDto.setNespravneudaje(null);
        if (include != vuscConfig.isDatumvzniku()) vuscDto.setDatumvzniku(null);
    }

    /**
     * Sets the fields of the VuscDto based on the configuration and the values from the database.
     *
     * @param vuscDto      the VuscDto to be set
     * @param vuscFromDb   the VuscDto from the database
     * @param vuscConfig   the configuration for Vusc
     * @param include      whether to include or exclude the fields
     */
    private void setVuscDtoFieldsCombinedDB(VuscDto vuscDto, VuscDto vuscFromDb, VuscBoolean vuscConfig, boolean include) {
        if (include != vuscConfig.isNazev()) vuscDto.setNazev(vuscFromDb.getNazev());
        if (include != vuscConfig.isNespravny()) vuscDto.setNespravny(vuscFromDb.getNespravny());
        if (include != vuscConfig.isRegionsoudrznosti()) vuscDto.setRegionsoudrznosti(vuscFromDb.getRegionsoudrznosti());
        if (include != vuscConfig.isPlatiod()) vuscDto.setPlatiod(vuscFromDb.getPlatiod());
        if (include != vuscConfig.isPlatido()) vuscDto.setPlatido(vuscFromDb.getPlatido());
        if (include != vuscConfig.isIdtransakce()) vuscDto.setIdtransakce(vuscFromDb.getIdtransakce());
        if (include != vuscConfig.isGlobalniidnavrhuzmeny()) vuscDto.setGlobalniidnavrhuzmeny(vuscFromDb.getGlobalniidnavrhuzmeny());
        if (include != vuscConfig.isNutslau()) vuscDto.setNutslau(vuscFromDb.getNutslau());
        if (include != vuscConfig.isGeometriedefbod()) vuscDto.setGeometriedefbod(vuscFromDb.getGeometriedefbod());
        if (include != vuscConfig.isGeometriegenhranice()) vuscDto.setGeometriegenhranice(vuscFromDb.getGeometriegenhranice());
        if (include != vuscConfig.isGeometrieorihranice()) vuscDto.setGeometrieorihranice(vuscFromDb.getGeometrieorihranice());
        if (include != vuscConfig.isNespravneudaje()) vuscDto.setNespravneudaje(vuscFromDb.getNespravneudaje());
        if (include != vuscConfig.isDatumvzniku()) vuscDto.setDatumvzniku(vuscFromDb.getDatumvzniku());
    }
    //endregion
}
