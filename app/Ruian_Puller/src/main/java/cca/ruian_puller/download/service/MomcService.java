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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<MomcDto> toDelete = new ArrayList<>();
        momcDtos.forEach(momcDto -> {
            iterator.getAndIncrement();
            // Remove all MomcDto with null Kod
            if (momcDto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(momcDto);
                return;
            }
            // Check if all foreign keys exist
            if (!checkFK(momcDto)) {
                removedByFK.getAndIncrement();
                toDelete.add(momcDto);
                return;
            }
            // If dto is in db already, select it
            MomcDto momcFromDb = momcRepository.findByKod(momcDto.getKod());
            if (momcFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(momcDto, momcFromDb);
            } else if (appConfig.getMomcConfig() != null && !appConfig.getMomcConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(momcDto, momcFromDb, appConfig.getMomcConfig());
            }
            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= momcDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of MomcDtos processed");
            }
            if (iterator.get() >= momcDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of MomcDtos processed");
            }
            if (iterator.get() >= momcDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of MomcDtos processed");
            }
            if (iterator.get() >= momcDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of MomcDtos processed");
            }
        });

        // Remove all invalid MomcDtos
        momcDtos.removeAll(toDelete);

        // Log if some MomcDto were removed
        if (removedByNullKod.get() > 0) log.warn("{} removed from Momc due to null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.warn("{} removed from Momc due to missing foreign keys", removedByFK.get());

        // Save MomcDtos to the db
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

    private void updateWithDbValues(MomcDto momcDto, MomcDto momcFromDb) {
        if (momcDto.getNazev() == null) momcDto.setNazev(momcFromDb.getNazev());
        if (momcDto.getNespravny() == null) momcDto.setNespravny(momcFromDb.getNespravny());
        if (momcDto.getMop() == null) momcDto.setMop(momcFromDb.getMop());
        if (momcDto.getObec() == null) momcDto.setObec(momcFromDb.getObec());
        if (momcDto.getSpravniobvod() == null) momcDto.setSpravniobvod(momcFromDb.getSpravniobvod());
        if (momcDto.getPlatiod() == null) momcDto.setPlatiod(momcFromDb.getPlatiod());
        if (momcDto.getPlatido() == null) momcDto.setPlatido(momcFromDb.getPlatido());
        if (momcDto.getIdtransakce() == null) momcDto.setIdtransakce(momcFromDb.getIdtransakce());
        if (momcDto.getGlobalniidnavrhuzmeny() == null) momcDto.setGlobalniidnavrhuzmeny(momcFromDb.getGlobalniidnavrhuzmeny());
        if (momcDto.getVlajkatext() == null) momcDto.setVlajkatext(momcFromDb.getVlajkatext());
        if (momcDto.getVlajkaobrazek() == null) momcDto.setVlajkaobrazek(momcFromDb.getVlajkaobrazek());
        if (momcDto.getZnaktext() == null) momcDto.setZnaktext(momcFromDb.getZnaktext());
        if (momcDto.getZnakobrazek() == null) momcDto.setZnakobrazek(momcFromDb.getZnakobrazek());
        if (momcDto.getMluvnickecharakteristiky() == null) momcDto.setMluvnickecharakteristiky(momcFromDb.getMluvnickecharakteristiky());
        if (momcDto.getGeometriedefbod() == null) momcDto.setGeometriedefbod(momcFromDb.getGeometriedefbod());
        if (momcDto.getGeometrieorihranice() == null) momcDto.setGeometrieorihranice(momcFromDb.getGeometrieorihranice());
        if (momcDto.getNespravneudaje() == null) momcDto.setNespravneudaje(momcFromDb.getNespravneudaje());
        if (momcDto.getDatumvzniku() == null) momcDto.setDatumvzniku(momcFromDb.getDatumvzniku());
    }

    //region Prepare with MomcBoolean
    private void prepare(MomcDto momcDto, MomcDto momcFromDb, MomcBoolean momcConfig) {
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
