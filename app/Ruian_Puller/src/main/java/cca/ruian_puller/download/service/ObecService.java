package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.ObecBoolean;
import cca.ruian_puller.download.dto.KatastralniUzemiDto;
import cca.ruian_puller.download.dto.ObecDto;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.OkresRepository;
import cca.ruian_puller.download.repository.PouRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class ObecService {

    private final ObecRepository obecRepository;
    private final OkresRepository okresRepository;
    private final PouRepository pouRepository;

    @Autowired
    public ObecService(ObecRepository obecRepository, OkresRepository okresRepository, PouRepository pouRepository) {
        this.obecRepository = obecRepository;
        this.okresRepository = okresRepository;
        this.pouRepository = pouRepository;
    }

    public void prepareAndSave(List<ObecDto> obecDtos, AppConfig appConfig) {
        // Remove all Obec with null Kod
        int initialSize = obecDtos.size();
        obecDtos.removeIf(obecDto -> obecDto.getKod() == null);
        if (initialSize != obecDtos.size()) {
            log.warn("{} removed from Obec due to null Kod", initialSize - obecDtos.size());
        }

        // Based on ObecBoolean from AppConfig, filter out ObecDto
        if (appConfig.getObecConfig() != null && !appConfig.getObecConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL))
            obecDtos.forEach(obecDto -> prepare(obecDto, appConfig.getObecConfig()));

        // Check all foreign keys
        int initialSize2 = obecDtos.size();
        obecDtos.removeIf(obecDto -> !checkFK(obecDto));
        if (initialSize2 != obecDtos.size()) {
            log.warn("{} removed from Obec due to missing foreign keys", initialSize2 - obecDtos.size());
        }

        // Split list of ObecDto into smaller lists
        for (int i = 0; i < obecDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), obecDtos.size());
            List<ObecDto> subList = obecDtos.subList(i, toIndex);
            obecRepository.saveAll(subList);
            log.info("Saved {} out of {} Obec", toIndex, obecDtos.size());
        }
    }

    private boolean checkFK(ObecDto obecDto) {
        // Get the foreign keys Kod
        Integer okresKod = obecDto.getOkres();
        Integer pouKod = obecDto.getPou();

        // Check if the foreign key Kod for Okres exists
        if (okresKod != null && !okresRepository.existsByKod(okresKod)) {
            log.warn("Obec with Kod {} does not have a valid foreign key: Okres with Kod {}", obecDto.getKod(), okresKod);
            return false;
        }

        // Check if the foreign key Kod for Pou exists
        if (pouKod != null && !pouRepository.existsByKod(pouKod)) {
            log.warn("Obec with Kod {} does not have a valid foreign key: Pou with Kod {}", obecDto.getKod(), pouKod);
            return false;
        }

        return true;
    }

    //region Prepare with ObecBoolean
    private void prepare(ObecDto obecDto, ObecBoolean obecConfig) {
        // Check if this dto is in db already
        ObecDto obecDtoFromDb = obecRepository.findByKod(obecDto.getKod());
        boolean include = obecConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (obecDtoFromDb == null) {
            setObecDtoFields(obecDto, obecConfig, include);
        } else {
            setObecDtoFieldsCombinedDB(obecDto, obecDtoFromDb, obecConfig, include);
        }
    }

    private void setObecDtoFields(ObecDto obecDto, ObecBoolean obecConfig, boolean include) {
        if (include != obecConfig.isNazev()) obecDto.setNazev(null);
        if (include != obecConfig.isNespravny()) obecDto.setNespravny(null);
        if (include != obecConfig.isStatuskod()) obecDto.setStatuskod(null);
        if (include != obecConfig.isOkres()) obecDto.setOkres(null);
        if (include != obecConfig.isPou()) obecDto.setPou(null);
        if (include != obecConfig.isPlatiod()) obecDto.setPlatiod(null);
        if (include != obecConfig.isPlatido()) obecDto.setPlatido(null);
        if (include != obecConfig.isIdtransakce()) obecDto.setIdtransakce(null);
        if (include != obecConfig.isGlobalniidnavrhuzmeny()) obecDto.setGlobalniidnavrhuzmeny(null);
        if (include != obecConfig.isMluvnickecharakteristiky()) obecDto.setMluvnickecharakteristiky(null);
        if (include != obecConfig.isVlajkatext()) obecDto.setVlajkatext(null);
        if (include != obecConfig.isVlajkaobrazek()) obecDto.setVlajkaobrazek(null);
        if (include != obecConfig.isZnaktext()) obecDto.setZnaktext(null);
        if (include != obecConfig.isZnakobrazek()) obecDto.setZnakobrazek(null);
        if (include != obecConfig.isClenenismrozsahkod()) obecDto.setClenenismrozsahkod(null);
        if (include != obecConfig.isClenenismtypkod()) obecDto.setClenenismtypkod(null);
        if (include != obecConfig.isNutslau()) obecDto.setNutslau(null);
        if (include != obecConfig.isGeometriedefbod()) obecDto.setGeometriedefbod(null);
        if (include != obecConfig.isGeometriegenhranice()) obecDto.setGeometriegenhranice(null);
        if (include != obecConfig.isGeometrieorihranice()) obecDto.setGeometrieorihranice(null);
        if (include != obecConfig.isNespravneudaje()) obecDto.setNespravneudaje(null);
        if (include != obecConfig.isDatumvzniku()) obecDto.setDatumvzniku(null);
    }

    private void setObecDtoFieldsCombinedDB(ObecDto obecDto, ObecDto obecDtoFromDb, ObecBoolean obecConfig, boolean include) {
        if (include != obecConfig.isNazev()) obecDto.setNazev(obecDtoFromDb.getNazev());
        if (include != obecConfig.isNespravny()) obecDto.setNespravny(obecDtoFromDb.getNespravny());
        if (include != obecConfig.isStatuskod()) obecDto.setStatuskod(obecDtoFromDb.getStatuskod());
        if (include != obecConfig.isOkres()) obecDto.setOkres(obecDtoFromDb.getOkres());
        if (include != obecConfig.isPou()) obecDto.setPou(obecDtoFromDb.getPou());
        if (include != obecConfig.isPlatiod()) obecDto.setPlatiod(obecDtoFromDb.getPlatiod());
        if (include != obecConfig.isPlatido()) obecDto.setPlatido(obecDtoFromDb.getPlatido());
        if (include != obecConfig.isIdtransakce()) obecDto.setIdtransakce(obecDtoFromDb.getIdtransakce());
        if (include != obecConfig.isGlobalniidnavrhuzmeny()) obecDto.setGlobalniidnavrhuzmeny(obecDtoFromDb.getGlobalniidnavrhuzmeny());
        if (include != obecConfig.isMluvnickecharakteristiky()) obecDto.setMluvnickecharakteristiky(obecDtoFromDb.getMluvnickecharakteristiky());
        if (include != obecConfig.isVlajkatext()) obecDto.setVlajkatext(obecDtoFromDb.getVlajkatext());
        if (include != obecConfig.isVlajkaobrazek()) obecDto.setVlajkaobrazek(obecDtoFromDb.getVlajkaobrazek());
        if (include != obecConfig.isZnaktext()) obecDto.setZnaktext(obecDtoFromDb.getZnaktext());
        if (include != obecConfig.isZnakobrazek()) obecDto.setZnakobrazek(obecDtoFromDb.getZnakobrazek());
        if (include != obecConfig.isClenenismrozsahkod()) obecDto.setClenenismrozsahkod(obecDtoFromDb.getClenenismrozsahkod());
        if (include != obecConfig.isClenenismtypkod()) obecDto.setClenenismtypkod(obecDtoFromDb.getClenenismtypkod());
        if (include != obecConfig.isNutslau()) obecDto.setNutslau(obecDtoFromDb.getNutslau());
        if (include != obecConfig.isGeometriedefbod()) obecDto.setGeometriedefbod(obecDtoFromDb.getGeometriedefbod());
        if (include != obecConfig.isGeometriegenhranice()) obecDto.setGeometriegenhranice(obecDtoFromDb.getGeometriegenhranice());
        if (include != obecConfig.isGeometrieorihranice()) obecDto.setGeometrieorihranice(obecDtoFromDb.getGeometrieorihranice());
        if (include != obecConfig.isNespravneudaje()) obecDto.setNespravneudaje(obecDtoFromDb.getNespravneudaje());
        if (include != obecConfig.isDatumvzniku()) obecDto.setDatumvzniku(obecDtoFromDb.getDatumvzniku());
    }
    //endregion
}
