package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.MomcBoolean;
import cca.ruian_puller.download.dto.MomcDto;
import cca.ruian_puller.download.repository.MomcRepository;
import cca.ruian_puller.download.repository.MopRepository;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.SpravniObvodRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class MomcService {

    private final MomcRepository momcRepository;
    private final MopRepository mopRepository;
    private final ObecRepository obecRepository;
    private final SpravniObvodRepository spravniObvodRepository;

    @Autowired
    public MomcService(MomcRepository momcRepository, MopRepository mopRepository, ObecRepository obecRepository, SpravniObvodRepository spravniObvodRepository) {
        this.momcRepository = momcRepository;
        this.mopRepository = mopRepository;
        this.obecRepository = obecRepository;
        this.spravniObvodRepository = spravniObvodRepository;
    }

    public void prepareAndSave(List<MomcDto> momcDtos, AppConfig appConfig) {
        // Remove all Momc with null Kod
        int initialSize = momcDtos.size();
        momcDtos.removeIf(momcDto -> momcDto.getKod() == null);
        if (initialSize != momcDtos.size()) {
            log.warn("{} removed from Momc due to null Kod", initialSize - momcDtos.size());
        }

        // Based on MomcBoolean from AppConfig, filter out MomcDto
        if (appConfig.getMomcConfig() != null && !appConfig.getMomcConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL))
            momcDtos.forEach(momcDto -> prepare(momcDto, appConfig.getMomcConfig()));

        // Check all foreign keys
        int initialSize2 = momcDtos.size();
        momcDtos.removeIf(momcDto -> !checkFK(momcDto));
        if (initialSize2 != momcDtos.size()) {
            log.warn("{} removed from Momc due to missing foreign keys", initialSize2 - momcDtos.size());
        }

        // Split list of MomcDto into smaller lists
        for (int i = 0; i < momcDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), momcDtos.size());
            List<MomcDto> subList = momcDtos.subList(i, toIndex);
            momcRepository.saveAll(subList);
            log.info("Saved {} out of {} Momc", toIndex, momcDtos.size());
        }
    }

    private boolean checkFK(MomcDto momcDto) {
        // Get the foreign keys Kod
        Integer mopKod = momcDto.getMop();
        Integer obecKod = momcDto.getObec();
        Integer spravniObvodKod = momcDto.getSpravniobvod();

        // Check if the foreign key Kod for Mop exists
        if (mopKod != null && !mopRepository.existsByKod(mopKod)) {
            log.warn("Momc with Kod {} does not have a valid foreign key: Mop with Kod {}", momcDto.getKod(), mopKod);
            return false;
        }

        // Check if the foreign key Kod for Obec exists
        if (obecKod != null && !obecRepository.existsByKod(obecKod)) {
            log.warn("Momc with Kod {} does not have a valid foreign key: Obec with Kod {}", momcDto.getKod(), obecKod);
            return false;
        }

        // Check if the foreign key Kod for SpravniObvod exists
        if (spravniObvodKod != null && !spravniObvodRepository.existsByKod(spravniObvodKod)) {
            log.warn("Momc with Kod {} does not have a valid foreign key: SpravniObvod with Kod {}", momcDto.getKod(), spravniObvodKod);
            return false;
        }

        return true;
    }

    //region Prepare with MomcBoolean
    private void prepare(MomcDto momcDto, MomcBoolean momcConfig) {
        // Check if this dto is in db already
        MomcDto momcFromDb = momcRepository.findByKod(momcDto.getKod());
        boolean include = momcConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (momcFromDb == null) {
            setMomcDtoFields(momcDto, momcConfig, include);
        } else {
            setMomcDtoFieldsCombinedDB(momcDto, momcFromDb, momcConfig, include);
        }
    }

    private void setMomcDtoFields(MomcDto momcDto, MomcBoolean momcConfig, boolean include) {
        if (include != momcConfig.isNazev()) momcDto.setNazev(null);
        if (include != momcConfig.isNespravny()) momcDto.setNespravny(null);
        if (include != momcConfig.isMop()) momcDto.setMop(null);
        if (include != momcConfig.isObec()) momcDto.setObec(null);
        if (include != momcConfig.isSpravniobvod()) momcDto.setSpravniobvod(null);
        if (include != momcConfig.isPlatiod()) momcDto.setPlatiod(null);
        if (include != momcConfig.isPlatido()) momcDto.setPlatido(null);
        if (include != momcConfig.isIdtransakce()) momcDto.setIdtransakce(null);
        if (include != momcConfig.isGlobalniidnavrhuzmeny()) momcDto.setGlobalniidnavrhuzmeny(null);
        if (include != momcConfig.isVlajkatext()) momcDto.setVlajkatext(null);
        if (include != momcConfig.isVlajkaobrazek()) momcDto.setVlajkaobrazek(null);
        if (include != momcConfig.isZnaktext()) momcDto.setZnaktext(null);
        if (include != momcConfig.isZnakobrazek()) momcDto.setZnakobrazek(null);
        if (include != momcConfig.isMluvnickecharakteristiky()) momcDto.setMluvnickecharakteristiky(null);
        if (include != momcConfig.isGeometriedefbod()) momcDto.setGeometriedefbod(null);
        if (include != momcConfig.isGeometrieorihranice()) momcDto.setGeometrieorihranice(null);
        if (include != momcConfig.isNespravneudaje()) momcDto.setNespravneudaje(null);
        if (include != momcConfig.isDatumvzniku()) momcDto.setDatumvzniku(null);
    }

    private void setMomcDtoFieldsCombinedDB(MomcDto momcDto, MomcDto momcFromDb, MomcBoolean momcConfig, boolean include) {
        if (include != momcConfig.isNazev()) momcDto.setNazev(momcFromDb.getNazev());
        if (include != momcConfig.isNespravny()) momcDto.setNespravny(momcFromDb.getNespravny());
        if (include != momcConfig.isMop()) momcDto.setMop(momcFromDb.getMop());
        if (include != momcConfig.isObec()) momcDto.setObec(momcFromDb.getObec());
        if (include != momcConfig.isSpravniobvod()) momcDto.setSpravniobvod(momcFromDb.getSpravniobvod());
        if (include != momcConfig.isPlatiod()) momcDto.setPlatiod(momcFromDb.getPlatiod());
        if (include != momcConfig.isPlatido()) momcDto.setPlatido(momcFromDb.getPlatido());
        if (include != momcConfig.isIdtransakce()) momcDto.setIdtransakce(momcFromDb.getIdtransakce());
        if (include != momcConfig.isGlobalniidnavrhuzmeny()) momcDto.setGlobalniidnavrhuzmeny(momcFromDb.getGlobalniidnavrhuzmeny());
        if (include != momcConfig.isVlajkatext()) momcDto.setVlajkatext(momcFromDb.getVlajkatext());
        if (include != momcConfig.isVlajkaobrazek()) momcDto.setVlajkaobrazek(momcFromDb.getVlajkaobrazek());
        if (include != momcConfig.isZnaktext()) momcDto.setZnaktext(momcFromDb.getZnaktext());
        if (include != momcConfig.isZnakobrazek()) momcDto.setZnakobrazek(momcFromDb.getZnakobrazek());
        if (include != momcConfig.isMluvnickecharakteristiky()) momcDto.setMluvnickecharakteristiky(momcFromDb.getMluvnickecharakteristiky());
        if (include != momcConfig.isGeometriedefbod()) momcDto.setGeometriedefbod(momcFromDb.getGeometriedefbod());
        if (include != momcConfig.isGeometrieorihranice()) momcDto.setGeometrieorihranice(momcFromDb.getGeometrieorihranice());
        if (include != momcConfig.isNespravneudaje()) momcDto.setNespravneudaje(momcFromDb.getNespravneudaje());
        if (include != momcConfig.isDatumvzniku()) momcDto.setDatumvzniku(momcFromDb.getDatumvzniku());
    }
    //endregion
}
