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

import java.util.List;

@Service
@Log4j2
public class AdresniMistoService {

    private final AdresniMistoRepository adresniMistoRepository;
    private final StavebniObjektRepository stavebniObjektRepository;
    private final UliceRepository uliceRepository;

    @Autowired
    public AdresniMistoService(AdresniMistoRepository adresniMistoRepository, StavebniObjektRepository stavebniObjektRepository, UliceRepository uliceRepository) {
        this.adresniMistoRepository = adresniMistoRepository;
        this.stavebniObjektRepository = stavebniObjektRepository;
        this.uliceRepository = uliceRepository;
    }

    public void prepareAndSave(List<AdresniMistoDto> adresniMistoDtos, AppConfig appConfig) {
        // Remove all AdresniMistoDto with null Kod
        int initialSize = adresniMistoDtos.size();
        adresniMistoDtos.removeIf(adresniMisto -> adresniMisto.getKod() == null);
        if (initialSize != adresniMistoDtos.size())
            log.warn("{} removed from AdresniMisto due to null Kod", initialSize - adresniMistoDtos.size());

        // Based on AdresniMistoBoolean from AppConfig, filter out AdresniMistoDto
        if (appConfig.getAdresniMistoConfig() != null && !appConfig.getAdresniMistoConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL))
            adresniMistoDtos.forEach(adresniMisto -> prepare(adresniMisto, appConfig.getAdresniMistoConfig()));

        // Check all foreign keys
        int initialSize2 = adresniMistoDtos.size();
        adresniMistoDtos.removeIf(adresniMisto -> !checkFK(adresniMisto));
        if (initialSize2 != adresniMistoDtos.size()) {
            log.info("{} removed from AdresniMisto due to missing foreign keys", initialSize2 - adresniMistoDtos.size());
        }

        // Split list of AdresniMistoDto into smaller lists
        for (int i = 0; i < adresniMistoDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), adresniMistoDtos.size());
            List<AdresniMistoDto> subList = adresniMistoDtos.subList(i, toIndex);
            adresniMistoRepository.saveAll(subList);
            log.info("Saved {} out of {} AdresniMisto", toIndex, adresniMistoDtos.size());
        }
    }

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

    //region Prepare with AdresniMistoBoolean
    private void prepare(AdresniMistoDto adresniMistoDto, AdresniMistoBoolean adresniMistoConfig) {
        // Check if this dto is in db already
        AdresniMistoDto adresniMistoFromDb = adresniMistoRepository.findByKod(adresniMistoDto.getKod());
        boolean include = adresniMistoConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (adresniMistoFromDb == null) {
            setAdresniMistoDtoFields(adresniMistoDto, adresniMistoConfig, include);
        } else {
            setAdresniMistoDtoFieldsCombinedDB(adresniMistoDto, adresniMistoFromDb, adresniMistoConfig, include);
        }
    }

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