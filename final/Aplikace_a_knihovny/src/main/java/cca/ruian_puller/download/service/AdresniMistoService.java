package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.AdresniMistoBoolean;
import cca.ruian_puller.download.dto.AdresniMistoDto;
import cca.ruian_puller.download.repository.AdresniMistoRepository;
import cca.ruian_puller.download.repository.StavebniObjektRepository;
import cca.ruian_puller.download.repository.UliceRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class AdresniMistoService {
    // Repositories
    private final AdresniMistoRepository adresniMistoRepository;
    private final StavebniObjektRepository stavebniObjektRepository;
    private final UliceRepository uliceRepository;

    /**
     * Constructor for AdresniMistoService.
     *
     * @param adresniMistoRepository Repository for AdresniMisto
     * @param stavebniObjektRepository Repository for StavebniObjekt
     * @param uliceRepository Repository for Ulice
     */
    @Autowired
    public AdresniMistoService(AdresniMistoRepository adresniMistoRepository, StavebniObjektRepository stavebniObjektRepository, UliceRepository uliceRepository) {
        this.adresniMistoRepository = adresniMistoRepository;
        this.stavebniObjektRepository = stavebniObjektRepository;
        this.uliceRepository = uliceRepository;
    }

    /**
     * Prepares and saves a list of AdresniMistoDto objects to the database.
     *
     * @param adresniMistoDtos List of AdresniMistoDto objects to be saved
     * @param appConfig Application configuration
     */
    public void prepareAndSave(List<AdresniMistoDto> adresniMistoDtos, AppConfig appConfig) {
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<AdresniMistoDto> toDelete = new ArrayList<>();
        adresniMistoDtos.forEach(adresniMisto -> {
            iterator.getAndIncrement();
            // Remove all AdresniMistoDto with null Kod
            if (adresniMisto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(adresniMisto);
                return;
            }
            // If dto is already in db, select it
            AdresniMistoDto adresniMistoFromDb = adresniMistoRepository.findByKod(adresniMisto.getKod());
            if (adresniMistoFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(adresniMisto, adresniMistoFromDb);
            } else if (appConfig.getAdresniMistoConfig() != null && !appConfig.getAdresniMistoConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(adresniMisto, adresniMistoFromDb, appConfig.getAdresniMistoConfig());
            }
            // Check all foreign keys
            if (!checkFK(adresniMisto)) {
                removedByFK.getAndIncrement();
                toDelete.add(adresniMisto);
                return;
            }

            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= adresniMistoDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of AdresniMistoDtos processed");
            }
            if (iterator.get() >= adresniMistoDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of AdresniMistoDtos processed");
            }
            if (iterator.get() >= adresniMistoDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of AdresniMistoDtos processed");
            }
            if (iterator.get() >= adresniMistoDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of AdresniMistoDtos processed");
            }
        });

        // Remove all invalid AdresniMistoDtos
        adresniMistoDtos.removeAll(toDelete);

        // Log if some AdresniMistoDto were removed
        if (removedByNullKod.get() > 0) log.info("{} removed from AdresniMisto due to null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.info("{} removed from AdresniMisto due to missing foreign keys", removedByFK.get());

        // Save AdresniMistoDtos to db
        for (int i = 0; i < adresniMistoDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), adresniMistoDtos.size());
            List<AdresniMistoDto> subList = adresniMistoDtos.subList(i, toIndex);
            adresniMistoRepository.saveAll(subList);
            log.info("Saved {} out of {} AdresniMisto", toIndex, adresniMistoDtos.size());
        }
    }

    /**
     * Checks if the foreign keys of the given AdresniMistoDto are valid.
     *
     * @param adresniMisto AdresniMistoDto to check
     * @return true if all foreign keys are valid, false otherwise
     */
    private boolean checkFK(AdresniMistoDto adresniMisto) {
        // Get the foreign keys Kod
        Integer uliceKod = adresniMisto.getUlice();
        Integer stavebniObjektKod = adresniMisto.getStavebniobjekt();

        // Check if the foreign key Kod for Ulice exists
        if (uliceKod != null && !uliceRepository.existsByKod(uliceKod)) {
            log.warn("AdresniMisto with Kod {} does not have a valid foreign key: Ulice with kod {}", adresniMisto.getKod(), uliceKod);
            return false;
        }

        // Check if the foreign key Kod for StavebniObjekt exists
        if (stavebniObjektKod != null && !stavebniObjektRepository.existsByKod(stavebniObjektKod)) {
            log.warn("AdresniMisto with Kod {} does not have a valid foreign key: StavebniObjekt with kod {}", adresniMisto.getKod(), stavebniObjektKod);
            return false;
        }

        return true;
    }

    /**
     * Updates the given AdresniMistoDto with values from the database if they are null.
     *
     * @param adresniMistoDto AdresniMistoDto to update
     * @param adresniMistoFromDb AdresniMistoDto from the database
     */
    private void updateWithDbValues(AdresniMistoDto adresniMistoDto, AdresniMistoDto adresniMistoFromDb) {
        if (adresniMistoDto.getNespravny() == null) adresniMistoDto.setNespravny(adresniMistoFromDb.getNespravny());
        if (adresniMistoDto.getCislodomovni() == null) adresniMistoDto.setCislodomovni(adresniMistoFromDb.getCislodomovni());
        if (adresniMistoDto.getCisloorientacni() == null) adresniMistoDto.setCisloorientacni(adresniMistoFromDb.getCisloorientacni());
        if (adresniMistoDto.getCisloorientacnipismeno() == null) adresniMistoDto.setCisloorientacnipismeno(adresniMistoFromDb.getCisloorientacnipismeno());
        if (adresniMistoDto.getPsc() == null) adresniMistoDto.setPsc(adresniMistoFromDb.getPsc());
        if (adresniMistoDto.getStavebniobjekt() == null) adresniMistoDto.setStavebniobjekt(adresniMistoFromDb.getStavebniobjekt());
        if (adresniMistoDto.getUlice() == null) adresniMistoDto.setUlice(adresniMistoFromDb.getUlice());
        if (adresniMistoDto.getVokod() == null) adresniMistoDto.setVokod(adresniMistoFromDb.getVokod());
        if (adresniMistoDto.getPlatiod() == null) adresniMistoDto.setPlatiod(adresniMistoFromDb.getPlatiod());
        if (adresniMistoDto.getPlatido() == null) adresniMistoDto.setPlatido(adresniMistoFromDb.getPlatido());
        if (adresniMistoDto.getIdtransakce() == null) adresniMistoDto.setIdtransakce(adresniMistoFromDb.getIdtransakce());
        if (adresniMistoDto.getGlobalniidnavrhuzmeny() == null) adresniMistoDto.setGlobalniidnavrhuzmeny(adresniMistoFromDb.getGlobalniidnavrhuzmeny());
        if (adresniMistoDto.getGeometriedefbod() == null) adresniMistoDto.setGeometriedefbod(adresniMistoFromDb.getGeometriedefbod());
        if (adresniMistoDto.getNespravneudaje() == null) adresniMistoDto.setNespravneudaje(adresniMistoFromDb.getNespravneudaje());
    }

    //region Prepare with AdresniMistoBoolean
    /**
     * Prepares the AdresniMistoDto object based on the configuration and existing values in the database.
     *
     * @param adresniMistoDto AdresniMistoDto to prepare
     * @param adresniMistoFromDb Existing AdresniMistoDto from the database
     * @param adresniMistoConfig Configuration for AdresniMisto
     */
    private void prepare(AdresniMistoDto adresniMistoDto, AdresniMistoDto adresniMistoFromDb, AdresniMistoBoolean adresniMistoConfig) {
        boolean include = adresniMistoConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (adresniMistoFromDb == null) {
            setAdresniMistoDtoFields(adresniMistoDto, adresniMistoConfig, include);
        } else {
            setAdresniMistoDtoFieldsCombinedDB(adresniMistoDto, adresniMistoFromDb, adresniMistoConfig, include);
        }
    }

    /**
     * Sets the fields of the AdresniMistoDto object based on the configuration.
     *
     * @param adresniMistoDto AdresniMistoDto to set fields for
     * @param adresniMistoConfig Configuration for AdresniMisto
     * @param include Whether to include or exclude the fields
     */
    private void setAdresniMistoDtoFields(AdresniMistoDto adresniMistoDto, AdresniMistoBoolean adresniMistoConfig, boolean include) {
        if (include != adresniMistoConfig.isNespravny()) adresniMistoDto.setNespravny(null);
        if (include != adresniMistoConfig.isCislodomovni()) adresniMistoDto.setCislodomovni(null);
        if (include != adresniMistoConfig.isCisloorientacni()) adresniMistoDto.setCisloorientacni(null);
        if (include != adresniMistoConfig.isCisloorientacnipismeno()) adresniMistoDto.setCisloorientacnipismeno(null);
        if (include != adresniMistoConfig.isPsc()) adresniMistoDto.setPsc(null);
        if (include != adresniMistoConfig.isStavebniobjekt()) adresniMistoDto.setStavebniobjekt(null);
        if (include != adresniMistoConfig.isUlice()) adresniMistoDto.setUlice(null);
        if (include != adresniMistoConfig.isVokod()) adresniMistoDto.setVokod(null);
        if (include != adresniMistoConfig.isPlatiod()) adresniMistoDto.setPlatiod(null);
        if (include != adresniMistoConfig.isPlatido()) adresniMistoDto.setPlatido(null);
        if (include != adresniMistoConfig.isIdtransakce()) adresniMistoDto.setIdtransakce(null);
        if (include != adresniMistoConfig.isGlobalniidnavrhuzmeny()) adresniMistoDto.setGlobalniidnavrhuzmeny(null);
        if (include != adresniMistoConfig.isGeometriedefbod()) adresniMistoDto.setGeometriedefbod(null);
        if (include != adresniMistoConfig.isNespravneudaje()) adresniMistoDto.setNespravneudaje(null);
    }

    /**
     * Sets the fields of the AdresniMistoDto object based on the configuration and existing values in the database.
     *
     * @param adresniMistoDto AdresniMistoDto to set fields for
     * @param adresniMistoFromDb Existing AdresniMistoDto from the database
     * @param adresniMistoConfig Configuration for AdresniMisto
     * @param include Whether to include or exclude the fields
     */
    private void setAdresniMistoDtoFieldsCombinedDB(AdresniMistoDto adresniMistoDto, AdresniMistoDto adresniMistoFromDb, AdresniMistoBoolean adresniMistoConfig, boolean include) {
        if (include != adresniMistoConfig.isNespravny()) adresniMistoDto.setNespravny(adresniMistoFromDb.getNespravny());
        if (include != adresniMistoConfig.isCislodomovni()) adresniMistoDto.setCislodomovni(adresniMistoFromDb.getCislodomovni());
        if (include != adresniMistoConfig.isCisloorientacni()) adresniMistoDto.setCisloorientacni(adresniMistoFromDb.getCisloorientacni());
        if (include != adresniMistoConfig.isCisloorientacnipismeno()) adresniMistoDto.setCisloorientacnipismeno(adresniMistoFromDb.getCisloorientacnipismeno());
        if (include != adresniMistoConfig.isPsc()) adresniMistoDto.setPsc(adresniMistoFromDb.getPsc());
        if (include != adresniMistoConfig.isStavebniobjekt()) adresniMistoDto.setStavebniobjekt(adresniMistoFromDb.getStavebniobjekt());
        if (include != adresniMistoConfig.isUlice()) adresniMistoDto.setUlice(adresniMistoFromDb.getUlice());
        if (include != adresniMistoConfig.isVokod()) adresniMistoDto.setVokod(adresniMistoFromDb.getVokod());
        if (include != adresniMistoConfig.isPlatiod()) adresniMistoDto.setPlatiod(adresniMistoFromDb.getPlatiod());
        if (include != adresniMistoConfig.isPlatido()) adresniMistoDto.setPlatido(adresniMistoFromDb.getPlatido());
        if (include != adresniMistoConfig.isIdtransakce()) adresniMistoDto.setIdtransakce(adresniMistoFromDb.getIdtransakce());
        if (include != adresniMistoConfig.isGlobalniidnavrhuzmeny()) adresniMistoDto.setGlobalniidnavrhuzmeny(adresniMistoFromDb.getGlobalniidnavrhuzmeny());
        if (include != adresniMistoConfig.isGeometriedefbod()) adresniMistoDto.setGeometriedefbod(adresniMistoFromDb.getGeometriedefbod());
        if (include != adresniMistoConfig.isNespravneudaje()) adresniMistoDto.setNespravneudaje(adresniMistoFromDb.getNespravneudaje());
    }
    //endregion
}