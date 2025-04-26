package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.KatastralniUzemiBoolean;
import cca.ruian_puller.download.dto.KatastralniUzemiDto;
import cca.ruian_puller.download.repository.KatastralniUzemiRepository;
import cca.ruian_puller.download.repository.ObecRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class KatastralniUzemiService {
    // Repository
    private final KatastralniUzemiRepository katastralniUzemiRepository;
    private final ObecRepository obecRepository;

    /**
     * Constructor for KatastralniUzemiService
     *
     * @param katastralniUzemiRepository Repository for KatastralniUzemi
     * @param obecRepository Repository for Obec
     */
    @Autowired
    public KatastralniUzemiService(KatastralniUzemiRepository katastralniUzemiRepository, ObecRepository obecRepository) {
        this.katastralniUzemiRepository = katastralniUzemiRepository;
        this.obecRepository = obecRepository;
    }

    /**
     * Prepares and saves KatastralniUzemiDtos to the database
     *
     * @param katastralniUzemiDtos List of KatastralniUzemiDtos to be saved
     * @param appConfig Application configuration
     */
    public void prepareAndSave(List<KatastralniUzemiDto> katastralniUzemiDtos, AppConfig appConfig) {
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<KatastralniUzemiDto> toDelete = new ArrayList<>();
        katastralniUzemiDtos.forEach(katastralniUzemiDto -> {
            iterator.getAndIncrement();
            // Remove KatastralniUzemiDto with null Kod
            if (katastralniUzemiDto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(katastralniUzemiDto);
                return;
            }
            // If dto is in db already, select it
            KatastralniUzemiDto katastralniUzemiFromDb = katastralniUzemiRepository.findByKod(katastralniUzemiDto.getKod());
            if (katastralniUzemiFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(katastralniUzemiDto, katastralniUzemiFromDb);
            } else if (appConfig.getKatastralniUzemiConfig() != null && !appConfig.getKatastralniUzemiConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(katastralniUzemiDto, katastralniUzemiFromDb, appConfig.getKatastralniUzemiConfig());
            }
            // Check if all foreign keys exist
            if (!checkFK(katastralniUzemiDto)) {
                removedByFK.getAndIncrement();
                toDelete.add(katastralniUzemiDto);
                return;
            }

            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= katastralniUzemiDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of KatastralniUzemiDtos processed");
            }
            if (iterator.get() >= katastralniUzemiDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of KatastralniUzemiDtos processed");
            }
            if (iterator.get() >= katastralniUzemiDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of KatastralniUzemiDtos processed");
            }
            if (iterator.get() >= katastralniUzemiDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of KatastralniUzemiDtos processed");
            }
        });

        // Remove all invalid KatastralniUzemiDtos
        katastralniUzemiDtos.removeAll(toDelete);

        // Log if some KatastralniUzemiDto were removed
        if (removedByNullKod.get() > 0) log.warn("{} removed from KatastralniUzemi due to null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.warn("{} removed from KatastralniUzemi due to missing foreign keys", removedByFK.get());

        // Save KatastralniUzemi to Db
        for (int i = 0; i < katastralniUzemiDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), katastralniUzemiDtos.size());
            List<KatastralniUzemiDto> subList = katastralniUzemiDtos.subList(i, toIndex);
            katastralniUzemiRepository.saveAll(subList);
            log.info("Saved {} out of {} KatastralniUzemi", toIndex, katastralniUzemiDtos.size());
        }

    }

    /**
     * Checks if the foreign keys of the KatastralniUzemiDto are valid
     *
     * @param katastralniUzemiDto KatastralniUzemiDto to be checked
     * @return true if all foreign keys are valid, false otherwise
     */
    private boolean checkFK(KatastralniUzemiDto katastralniUzemiDto) {
        // Get the foreign key Kod
        Integer obecKod = katastralniUzemiDto.getObec();

        // Check if the foreign key Kod for Obec is valid
        if (obecKod != null && !obecRepository.existsById(obecKod)) {
            log.warn("KatasralniUzemi with Kod {} does not have valid foreign keys: Obec with Kod {}", katastralniUzemiDto.getKod(), obecKod);
            return false;
        }

        return true;
    }

    /**
     * Updates the KatastralniUzemiDto with values from the database
     *
     * @param katastralniUzemiDto KatastralniUzemiDto to be updated
     * @param katastralniUzemiFromDb KatastralniUzemiDto from the database
     */
    private void updateWithDbValues(KatastralniUzemiDto katastralniUzemiDto, KatastralniUzemiDto katastralniUzemiFromDb) {
        if (katastralniUzemiDto.getNazev() == null) katastralniUzemiDto.setNazev(katastralniUzemiFromDb.getNazev());
        if (katastralniUzemiDto.getNespravny() == null) katastralniUzemiDto.setNespravny(katastralniUzemiFromDb.getNespravny());
        if (katastralniUzemiDto.getExistujedigitalnimapa() == null) katastralniUzemiDto.setExistujedigitalnimapa(katastralniUzemiFromDb.getExistujedigitalnimapa());
        if (katastralniUzemiDto.getObec() == null) katastralniUzemiDto.setObec(katastralniUzemiFromDb.getObec());
        if (katastralniUzemiDto.getPlatiod() == null) katastralniUzemiDto.setPlatiod(katastralniUzemiFromDb.getPlatiod());
        if (katastralniUzemiDto.getPlatido() == null) katastralniUzemiDto.setPlatido(katastralniUzemiFromDb.getPlatido());
        if (katastralniUzemiDto.getIdtransakce() == null) katastralniUzemiDto.setIdtransakce(katastralniUzemiFromDb.getIdtransakce());
        if (katastralniUzemiDto.getGlobalniidnavrhuzmeny() == null) katastralniUzemiDto.setGlobalniidnavrhuzmeny(katastralniUzemiFromDb.getGlobalniidnavrhuzmeny());
        if (katastralniUzemiDto.getRizeniid() == null) katastralniUzemiDto.setRizeniid(katastralniUzemiFromDb.getRizeniid());
        if (katastralniUzemiDto.getMluvnickecharakteristiky() == null) katastralniUzemiDto.setMluvnickecharakteristiky(katastralniUzemiFromDb.getMluvnickecharakteristiky());
        if (katastralniUzemiDto.getGeometriedefbod() == null) katastralniUzemiDto.setGeometriedefbod(katastralniUzemiFromDb.getGeometriedefbod());
        if (katastralniUzemiDto.getGeometriegenhranice() == null) katastralniUzemiDto.setGeometriegenhranice(katastralniUzemiFromDb.getGeometriegenhranice());
        if (katastralniUzemiDto.getNespravneudaje() == null) katastralniUzemiDto.setNespravneudaje(katastralniUzemiFromDb.getNespravneudaje());
        if (katastralniUzemiDto.getDatumvzniku() == null) katastralniUzemiDto.setDatumvzniku(katastralniUzemiFromDb.getDatumvzniku());
    }

    //region Prepare with KatastralniUzemiBoolean
    /**
     * Prepares the KatastralniUzemiDto with values from the database or from the configuration
     *
     * @param katastralniUzemiDto KatastralniUzemiDto to be prepared
     * @param katastralniUzemiFromDb KatastralniUzemiDto from the database
     * @param katastralniUzemiConfig Configuration for KatastralniUzemi
     */
    private void prepare(KatastralniUzemiDto katastralniUzemiDto, KatastralniUzemiDto katastralniUzemiFromDb, KatastralniUzemiBoolean katastralniUzemiConfig) {
        boolean include = katastralniUzemiConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (katastralniUzemiFromDb == null) {
            setKatastralniUzemiDtoFields(katastralniUzemiDto, katastralniUzemiConfig, include);
        } else {
            setKatastralniUzemiDtoFieldsCombinedDB(katastralniUzemiDto, katastralniUzemiFromDb, katastralniUzemiConfig, include);
        }
    }

    /**
     * Sets the fields of the KatastralniUzemiDto based on the configuration
     *
     * @param katastralniUzemiDto KatastralniUzemiDto to be set
     * @param katastralniUzemiConfig Configuration for KatastralniUzemi
     * @param include true if the field should be included, false otherwise
     */
    private void setKatastralniUzemiDtoFields(KatastralniUzemiDto katastralniUzemiDto, KatastralniUzemiBoolean katastralniUzemiConfig, boolean include) {
        if (include != katastralniUzemiConfig.isNazev()) katastralniUzemiDto.setNazev(null);
        if (include != katastralniUzemiConfig.isNespravny()) katastralniUzemiDto.setNespravny(null);
        if (include != katastralniUzemiConfig.isExistujedigitalnimapa()) katastralniUzemiDto.setExistujedigitalnimapa(null);
        if (include != katastralniUzemiConfig.isObec()) katastralniUzemiDto.setObec(null);
        if (include != katastralniUzemiConfig.isPlatiod()) katastralniUzemiDto.setPlatiod(null);
        if (include != katastralniUzemiConfig.isPlatido()) katastralniUzemiDto.setPlatido(null);
        if (include != katastralniUzemiConfig.isIdtransakce()) katastralniUzemiDto.setIdtransakce(null);
        if (include != katastralniUzemiConfig.isGlobalniidnavrhuzmeny()) katastralniUzemiDto.setGlobalniidnavrhuzmeny(null);
        if (include != katastralniUzemiConfig.isRizeniid()) katastralniUzemiDto.setRizeniid(null);
        if (include != katastralniUzemiConfig.isMluvnickecharakteristiky()) katastralniUzemiDto.setMluvnickecharakteristiky(null);
        if (include != katastralniUzemiConfig.isGeometriedefbod()) katastralniUzemiDto.setGeometriedefbod(null);
        if (include != katastralniUzemiConfig.isGeometriegenhranice()) katastralniUzemiDto.setGeometriegenhranice(null);
        if (include != katastralniUzemiConfig.isNespravneudaje()) katastralniUzemiDto.setNespravneudaje(null);
        if (include != katastralniUzemiConfig.isDatumvzniku()) katastralniUzemiDto.setDatumvzniku(null);
    }

    /**
     * Sets the fields of the KatastralniUzemiDto based on the configuration and the database values
     *
     * @param katastralniUzemiDto KatastralniUzemiDto to be set
     * @param katastralniUzemiFromDb KatastralniUzemiDto from the database
     * @param katastralniUzemiConfig Configuration for KatastralniUzemi
     * @param include true if the field should be included, false otherwise
     */
    private void setKatastralniUzemiDtoFieldsCombinedDB(KatastralniUzemiDto katastralniUzemiDto, KatastralniUzemiDto katastralniUzemiFromDb, KatastralniUzemiBoolean katastralniUzemiConfig, boolean include) {
        if (include != katastralniUzemiConfig.isNazev()) katastralniUzemiDto.setNazev(katastralniUzemiFromDb.getNazev());
        if (include != katastralniUzemiConfig.isNespravny()) katastralniUzemiDto.setNespravny(katastralniUzemiFromDb.getNespravny());
        if (include != katastralniUzemiConfig.isExistujedigitalnimapa()) katastralniUzemiDto.setExistujedigitalnimapa(katastralniUzemiFromDb.getExistujedigitalnimapa());
        if (include != katastralniUzemiConfig.isObec()) katastralniUzemiDto.setObec(katastralniUzemiFromDb.getObec());
        if (include != katastralniUzemiConfig.isPlatiod()) katastralniUzemiDto.setPlatiod(katastralniUzemiFromDb.getPlatiod());
        if (include != katastralniUzemiConfig.isPlatido()) katastralniUzemiDto.setPlatido(katastralniUzemiFromDb.getPlatido());
        if (include != katastralniUzemiConfig.isIdtransakce()) katastralniUzemiDto.setIdtransakce(katastralniUzemiFromDb.getIdtransakce());
        if (include != katastralniUzemiConfig.isGlobalniidnavrhuzmeny()) katastralniUzemiDto.setGlobalniidnavrhuzmeny(katastralniUzemiFromDb.getGlobalniidnavrhuzmeny());
        if (include != katastralniUzemiConfig.isRizeniid()) katastralniUzemiDto.setRizeniid(katastralniUzemiFromDb.getRizeniid());
        if (include != katastralniUzemiConfig.isMluvnickecharakteristiky()) katastralniUzemiDto.setMluvnickecharakteristiky(katastralniUzemiFromDb.getMluvnickecharakteristiky());
        if (include != katastralniUzemiConfig.isGeometriedefbod()) katastralniUzemiDto.setGeometriedefbod(katastralniUzemiFromDb.getGeometriedefbod());
        if (include != katastralniUzemiConfig.isGeometriegenhranice()) katastralniUzemiDto.setGeometriegenhranice(katastralniUzemiFromDb.getGeometriegenhranice());
        if (include != katastralniUzemiConfig.isNespravneudaje()) katastralniUzemiDto.setNespravneudaje(katastralniUzemiFromDb.getNespravneudaje());
        if (include != katastralniUzemiConfig.isDatumvzniku()) katastralniUzemiDto.setDatumvzniku(katastralniUzemiFromDb.getDatumvzniku());
    }
    //endregion
}
