package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.ZsjBoolean;
import cca.ruian_puller.download.dto.ZsjDto;
import cca.ruian_puller.download.repository.KatastralniUzemiRepository;
import cca.ruian_puller.download.repository.ZsjRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class ZsjService {
    // Repositories
    private final ZsjRepository zsjRepository;
    private final KatastralniUzemiRepository katastralniUzemiRepository;

    /**
     * Constructor for ZsjService.
     *
     * @param zsjRepository the repository for Zsj
     * @param katastralniUzemiRepository the repository for KatastralniUzemi
     */
    @Autowired
    public ZsjService(ZsjRepository zsjRepository, KatastralniUzemiRepository katastralniUzemiRepository) {
        this.zsjRepository = zsjRepository;
        this.katastralniUzemiRepository = katastralniUzemiRepository;
    }

    /**
     * Prepares and saves ZsjDtos to the database.
     *
     * @param zsjDtos   the list of ZsjDtos to be saved
     * @param appConfig the application configuration
     */
    public void prepareAndSave(List<ZsjDto> zsjDtos, AppConfig appConfig) {
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<ZsjDto> toDelete = new ArrayList<>();
        zsjDtos.forEach(zsjDto -> {
            iterator.getAndIncrement();
            // Remove all Zsj with null Kod
            if (zsjDto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(zsjDto);
                return;
            }
            // If dto is in db already, select it
            ZsjDto zsjFromDb = zsjRepository.findById(zsjDto.getKod()).orElse(null);
            if (zsjFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(zsjDto, zsjFromDb);
            } else if (appConfig.getZsjConfig() != null && !appConfig.getZsjConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(zsjDto, zsjFromDb, appConfig.getZsjConfig());
            }
            // Check if the foreign key is valid
            if (!checkFK(zsjDto)) {
                removedByFK.getAndIncrement();
                toDelete.add(zsjDto);
                return;
            }

            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= zsjDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of ZsjDtos processed");
            }
            if (iterator.get() >= zsjDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of ZsjDtos processed");
            }
            if (iterator.get() >= zsjDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of ZsjDtos processed");
            }
            if (iterator.get() >= zsjDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of ZsjDtos processed");
            }
        });

        // Remove all unwanted ZsjDtos
        zsjDtos.removeAll(toDelete);

        // Log if some ZsjDto were removed
        if (removedByNullKod.get() > 0) log.warn("Removed {} Zsj with null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.warn("Removed {} Zsj with invalid foreign keys", removedByFK.get());

        // Save ZsjDtos to db
        for (int i = 0; i < zsjDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), zsjDtos.size());
            List<ZsjDto> subList = zsjDtos.subList(i, toIndex);
            zsjRepository.saveAll(subList);
            log.info("Saved {} out of {} Zsj", toIndex, zsjDtos.size());
        }
    }

    /**
     * Checks if the foreign key for KatastralniUzemi is valid.
     *
     * @param zsj the ZsjDto to check
     * @return true if the foreign key is valid, false otherwise
     */
    private boolean checkFK(ZsjDto zsj) {
        // Get the foreign key Kod
        Integer katastralniUzemiKod = zsj.getKatastralniuzemi();

        // Check if the foreign key Kod for KatastralniUzemi exists
        if (katastralniUzemiKod != null && !katastralniUzemiRepository.existsById(katastralniUzemiKod)) {
            log.warn("Zsj with Kod {} does not have valid foreign keys: KatastralniUzemi with Kod {}", zsj.getKod(), katastralniUzemiKod);
            return false;
        }

        return true;
    }

    /**
     * Updates the ZsjDto with values from the database if they are null in the DTO.
     *
     * @param zsjDto      the ZsjDto to be updated
     * @param zsjFromDb   the ZsjDto from the database
     */
    private void updateWithDbValues(ZsjDto zsjDto, ZsjDto zsjFromDb) {
        if (zsjDto.getNazev() == null) zsjDto.setNazev(zsjFromDb.getNazev());
        if (zsjDto.getNespravny() == null) zsjDto.setNespravny(zsjFromDb.getNespravny());
        if (zsjDto.getKatastralniuzemi() == null) zsjDto.setKatastralniuzemi(zsjFromDb.getKatastralniuzemi());
        if (zsjDto.getPlatiod() == null) zsjDto.setPlatiod(zsjFromDb.getPlatiod());
        if (zsjDto.getPlatido() == null) zsjDto.setPlatido(zsjFromDb.getPlatido());
        if (zsjDto.getIdtransakce() == null) zsjDto.setIdtransakce(zsjFromDb.getIdtransakce());
        if (zsjDto.getGlobalniidnavrhuzmeny() == null) zsjDto.setGlobalniidnavrhuzmeny(zsjFromDb.getGlobalniidnavrhuzmeny());
        if (zsjDto.getMluvnickecharakteristiky() == null) zsjDto.setMluvnickecharakteristiky(zsjFromDb.getMluvnickecharakteristiky());
        if (zsjDto.getVymera() == null) zsjDto.setVymera(zsjFromDb.getVymera());
        if (zsjDto.getCharakterzsjkod() == null) zsjDto.setCharakterzsjkod(zsjFromDb.getCharakterzsjkod());
        if (zsjDto.getGeometriedefbod() == null) zsjDto.setGeometriedefbod(zsjFromDb.getGeometriedefbod());
        if (zsjDto.getGeometrieorihranice() == null) zsjDto.setGeometrieorihranice(zsjFromDb.getGeometrieorihranice());
        if (zsjDto.getNespravneudaje() == null) zsjDto.setNespravneudaje(zsjFromDb.getNespravneudaje());
        if (zsjDto.getDatumvzniku() == null) zsjDto.setDatumvzniku(zsjFromDb.getDatumvzniku());
    }

    //region Prepare with ZsjBoolean
    /**
     * Prepares the ZsjDto by setting its fields based on the configuration and the database values.
     *
     * @param zsjDto      the ZsjDto to be prepared
     * @param zsjFromDb   the ZsjDto from the database
     * @param zsjConfig   the configuration for Zsj
     */
    private void prepare(ZsjDto zsjDto, ZsjDto zsjFromDb, ZsjBoolean zsjConfig) {
        boolean include = zsjConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL);
        if (zsjFromDb == null) {
            setZsjDtoFields(zsjDto, zsjConfig, include);
        } else {
            setZsjDtoFieldsCombinedDB(zsjDto, zsjFromDb, zsjConfig, include);
        }
    }

    /**
     * Sets the fields of the ZsjDto based on the configuration.
     *
     * @param zsjDto      the ZsjDto to be set
     * @param zsjConfig   the configuration for Zsj
     * @param include     whether to include or exclude the fields
     */
    private void setZsjDtoFields(ZsjDto zsjDto, ZsjBoolean zsjConfig, boolean include) {
        if (include != zsjConfig.isNazev()) zsjDto.setNazev(null);
        if (include != zsjConfig.isNespravny()) zsjDto.setNespravny(null);
        if (include != zsjConfig.isKatastralniuzemi()) zsjDto.setKatastralniuzemi(null);
        if (include != zsjConfig.isPlatiod()) zsjDto.setPlatiod(null);
        if (include != zsjConfig.isPlatido()) zsjDto.setPlatido(null);
        if (include != zsjConfig.isIdtransakce()) zsjDto.setIdtransakce(null);
        if (include != zsjConfig.isGlobalniidnavrhuzmeny()) zsjDto.setGlobalniidnavrhuzmeny(null);
        if (include != zsjConfig.isMluvnickecharakteristiky()) zsjDto.setMluvnickecharakteristiky(null);
        if (include != zsjConfig.isVymera()) zsjDto.setVymera(null);
        if (include != zsjConfig.isCharakterzsjkod()) zsjDto.setCharakterzsjkod(null);
        if (include != zsjConfig.isGeometriedefbod()) zsjDto.setGeometriedefbod(null);
        if (include != zsjConfig.isGeometrieorihranice()) zsjDto.setGeometrieorihranice(null);
        if (include != zsjConfig.isNespravneudaje()) zsjDto.setNespravneudaje(null);
        if (include != zsjConfig.isDatumvzniku()) zsjDto.setDatumvzniku(null);
    }

    /**
     * Sets the fields of the ZsjDto based on the configuration and the database values.
     *
     * @param zsjDto      the ZsjDto to be set
     * @param zsjFromDb   the ZsjDto from the database
     * @param zsjConfig   the configuration for Zsj
     * @param include     whether to include or exclude the fields
     */
    private void setZsjDtoFieldsCombinedDB(ZsjDto zsjDto, ZsjDto zsjFromDb, ZsjBoolean zsjConfig, boolean include) {
        if (include != zsjConfig.isNazev()) zsjDto.setNazev(zsjFromDb.getNazev());
        if (include != zsjConfig.isNespravny()) zsjDto.setNespravny(zsjFromDb.getNespravny());
        if (include != zsjConfig.isKatastralniuzemi()) zsjDto.setKatastralniuzemi(zsjFromDb.getKatastralniuzemi());
        if (include != zsjConfig.isPlatiod()) zsjDto.setPlatiod(zsjFromDb.getPlatiod());
        if (include != zsjConfig.isPlatido()) zsjDto.setPlatido(zsjFromDb.getPlatido());
        if (include != zsjConfig.isIdtransakce()) zsjDto.setIdtransakce(zsjFromDb.getIdtransakce());
        if (include != zsjConfig.isGlobalniidnavrhuzmeny()) zsjDto.setGlobalniidnavrhuzmeny(zsjFromDb.getGlobalniidnavrhuzmeny());
        if (include != zsjConfig.isMluvnickecharakteristiky()) zsjDto.setMluvnickecharakteristiky(zsjFromDb.getMluvnickecharakteristiky());
        if (include != zsjConfig.isVymera()) zsjDto.setVymera(zsjFromDb.getVymera());
        if (include != zsjConfig.isCharakterzsjkod()) zsjDto.setCharakterzsjkod(zsjFromDb.getCharakterzsjkod());
        if (include != zsjConfig.isGeometriedefbod()) zsjDto.setGeometriedefbod(zsjFromDb.getGeometriedefbod());
        if (include != zsjConfig.isGeometrieorihranice()) zsjDto.setGeometrieorihranice(zsjFromDb.getGeometrieorihranice());
        if (include != zsjConfig.isNespravneudaje()) zsjDto.setNespravneudaje(zsjFromDb.getNespravneudaje());
        if (include != zsjConfig.isDatumvzniku()) zsjDto.setDatumvzniku(zsjFromDb.getDatumvzniku());
    }
}
