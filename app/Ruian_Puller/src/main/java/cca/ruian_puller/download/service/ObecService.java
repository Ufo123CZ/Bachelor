package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.ObecBoolean;
import cca.ruian_puller.download.dto.ObecDto;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.OkresRepository;
import cca.ruian_puller.download.repository.PouRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<ObecDto> toDelete = new ArrayList<>();
        obecDtos.forEach(obecDto -> {
            iterator.getAndIncrement();
            // Remove ObecDto if it has null Kod
            if (obecDto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(obecDto);
                return;
            }
            // Check if the foreign key is valid
            if (!checkFK(obecDto)) {
                removedByFK.getAndIncrement();
                toDelete.add(obecDto);
                return;
            }
            // If dto is in db already, select it
            ObecDto obecFromDb = obecRepository.findByKod(obecDto.getKod());
            if (obecFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(obecDto, obecFromDb);
            } else if (appConfig.getObecConfig() != null && !appConfig.getObecConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(obecDto, obecFromDb, appConfig.getObecConfig());
            }
            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= obecDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of ObecDtos processed");
            }
            if (iterator.get() >= obecDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of ObecDtos processed");
            }
            if (iterator.get() >= obecDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of ObecDtos processed");
            }
            if (iterator.get() >= obecDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of ObecDtos processed");
            }
        });

        // Remove all invalid ObecDtos
        obecDtos.removeAll(toDelete);

        // Log if some ObecDto were removed
        if (removedByNullKod.get() > 0) log.warn("{} removed from Obec due to null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.warn("{} removed from Obec due to missing foreign keys", removedByFK.get());

        // Save ObecDtos to db
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

    private void updateWithDbValues(ObecDto obecDto, ObecDto obecFromDb) {
        if (obecDto.getNazev() == null) obecDto.setNazev(obecFromDb.getNazev());
        if (obecDto.getNespravny() == null) obecDto.setNespravny(obecFromDb.getNespravny());
        if (obecDto.getStatuskod() == null) obecDto.setStatuskod(obecFromDb.getStatuskod());
        if (obecDto.getOkres() == null) obecDto.setOkres(obecFromDb.getOkres());
        if (obecDto.getPou() == null) obecDto.setPou(obecFromDb.getPou());
        if (obecDto.getPlatiod() == null) obecDto.setPlatiod(obecFromDb.getPlatiod());
        if (obecDto.getPlatido() == null) obecDto.setPlatido(obecFromDb.getPlatido());
        if (obecDto.getIdtransakce() == null) obecDto.setIdtransakce(obecFromDb.getIdtransakce());
        if (obecDto.getGlobalniidnavrhuzmeny() == null) obecDto.setGlobalniidnavrhuzmeny(obecFromDb.getGlobalniidnavrhuzmeny());
        if (obecDto.getMluvnickecharakteristiky() == null) obecDto.setMluvnickecharakteristiky(obecFromDb.getMluvnickecharakteristiky());
        if (obecDto.getVlajkatext() == null) obecDto.setVlajkatext(obecFromDb.getVlajkatext());
        if (obecDto.getVlajkaobrazek() == null) obecDto.setVlajkaobrazek(obecFromDb.getVlajkaobrazek());
        if (obecDto.getZnaktext() == null) obecDto.setZnaktext(obecFromDb.getZnaktext());
        if (obecDto.getZnakobrazek() == null) obecDto.setZnakobrazek(obecFromDb.getZnakobrazek());
        if (obecDto.getClenenismrozsahkod() == null) obecDto.setClenenismrozsahkod(obecFromDb.getClenenismrozsahkod());
        if (obecDto.getClenenismtypkod() == null) obecDto.setClenenismtypkod(obecFromDb.getClenenismtypkod());
        if (obecDto.getNutslau() == null) obecDto.setNutslau(obecFromDb.getNutslau());
        if (obecDto.getGeometriedefbod() == null) obecDto.setGeometriedefbod(obecFromDb.getGeometriedefbod());
        if (obecDto.getGeometriegenhranice() == null) obecDto.setGeometriegenhranice(obecFromDb.getGeometriegenhranice());
        if (obecDto.getGeometrieorihranice() == null) obecDto.setGeometrieorihranice(obecFromDb.getGeometrieorihranice());
        if (obecDto.getNespravneudaje() == null) obecDto.setNespravneudaje(obecFromDb.getNespravneudaje());
        if (obecDto.getDatumvzniku() == null) obecDto.setDatumvzniku(obecFromDb.getDatumvzniku());
    }

    private ObecDto getObecFromDb(Integer kod) {
        // Get the ObecDto from the database
        if (obecRepository.existsByKod(kod)) {
            ObecDto obecFromDb = new ObecDto();
            obecFromDb.setNazev(obecRepository.findNameByKod(kod));
            obecFromDb.setNespravny(obecRepository.findNespravnyByKod(kod));
            obecFromDb.setStatuskod(obecRepository.findStatuskodByKod(kod));
            obecFromDb.setOkres(obecRepository.findOkresByKod(kod));
            obecFromDb.setPou(obecRepository.findPouByKod(kod));
            obecFromDb.setPlatiod(obecRepository.findPlatiodByKod(kod));
            obecFromDb.setPlatido(obecRepository.findPlatidoByKod(kod));
            obecFromDb.setIdtransakce(obecRepository.findIdtransakceByKod(kod));
            obecFromDb.setGlobalniidnavrhuzmeny(obecRepository.findGlobalniidnavrhuzmenyByKod(kod));
            obecFromDb.setMluvnickecharakteristiky(obecRepository.findMluvnickecharakteristikyByKod(kod));
            obecFromDb.setVlajkatext(obecRepository.findVlajkatextByKod(kod));
            obecFromDb.setVlajkaobrazek(obecRepository.findVlajkaobrazekByKod(kod));
            obecFromDb.setZnaktext(obecRepository.findZnaktextByKod(kod));
            obecFromDb.setZnakobrazek(obecRepository.findZnakobrazekByKod(kod));
            obecFromDb.setClenenismrozsahkod(obecRepository.findClenenismrozsahkodByKod(kod));
            obecFromDb.setClenenismtypkod(obecRepository.findClenenismtypkodByKod(kod));
            obecFromDb.setNutslau(obecRepository.findNutslauByKod(kod));
            obecFromDb.setGeometriedefbod(obecRepository.findGeometriedefbodByKod(kod));
            obecFromDb.setGeometriegenhranice(obecRepository.findGeometriegenhraniceByKod(kod));
            obecFromDb.setGeometrieorihranice(obecRepository.findGeometrieorihraniceByKod(kod));
            obecFromDb.setNespravneudaje(obecRepository.findNespravneudajeByKod(kod));
            obecFromDb.setDatumvzniku(obecRepository.findDatumvznikuByKod(kod));
            return obecFromDb;
        }
        return null;
    }

    //region Prepare with ObecBoolean
    private void prepare(ObecDto obecDto, ObecDto obecFromDb, ObecBoolean obecConfig) {
        boolean include = obecConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (obecFromDb == null) {
            setObecDtoFields(obecDto, obecConfig, include);
        } else {
            setObecDtoFieldsCombinedDB(obecDto, obecFromDb, obecConfig, include);
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

    private void setObecDtoFieldsCombinedDB(ObecDto obecDto, ObecDto obecFromDb, ObecBoolean obecConfig, boolean include) {
        if (include != obecConfig.isNazev()) obecDto.setNazev(obecFromDb.getNazev());
        if (include != obecConfig.isNespravny()) obecDto.setNespravny(obecFromDb.getNespravny());
        if (include != obecConfig.isStatuskod()) obecDto.setStatuskod(obecFromDb.getStatuskod());
        if (include != obecConfig.isOkres()) obecDto.setOkres(obecFromDb.getOkres());
        if (include != obecConfig.isPou()) obecDto.setPou(obecFromDb.getPou());
        if (include != obecConfig.isPlatiod()) obecDto.setPlatiod(obecFromDb.getPlatiod());
        if (include != obecConfig.isPlatido()) obecDto.setPlatido(obecFromDb.getPlatido());
        if (include != obecConfig.isIdtransakce()) obecDto.setIdtransakce(obecFromDb.getIdtransakce());
        if (include != obecConfig.isGlobalniidnavrhuzmeny()) obecDto.setGlobalniidnavrhuzmeny(obecFromDb.getGlobalniidnavrhuzmeny());
        if (include != obecConfig.isMluvnickecharakteristiky()) obecDto.setMluvnickecharakteristiky(obecFromDb.getMluvnickecharakteristiky());
        if (include != obecConfig.isVlajkatext()) obecDto.setVlajkatext(obecFromDb.getVlajkatext());
        if (include != obecConfig.isVlajkaobrazek()) obecDto.setVlajkaobrazek(obecFromDb.getVlajkaobrazek());
        if (include != obecConfig.isZnaktext()) obecDto.setZnaktext(obecFromDb.getZnaktext());
        if (include != obecConfig.isZnakobrazek()) obecDto.setZnakobrazek(obecFromDb.getZnakobrazek());
        if (include != obecConfig.isClenenismrozsahkod()) obecDto.setClenenismrozsahkod(obecFromDb.getClenenismrozsahkod());
        if (include != obecConfig.isClenenismtypkod()) obecDto.setClenenismtypkod(obecFromDb.getClenenismtypkod());
        if (include != obecConfig.isNutslau()) obecDto.setNutslau(obecFromDb.getNutslau());
        if (include != obecConfig.isGeometriedefbod()) obecDto.setGeometriedefbod(obecFromDb.getGeometriedefbod());
        if (include != obecConfig.isGeometriegenhranice()) obecDto.setGeometriegenhranice(obecFromDb.getGeometriegenhranice());
        if (include != obecConfig.isGeometrieorihranice()) obecDto.setGeometrieorihranice(obecFromDb.getGeometrieorihranice());
        if (include != obecConfig.isNespravneudaje()) obecDto.setNespravneudaje(obecFromDb.getNespravneudaje());
        if (include != obecConfig.isDatumvzniku()) obecDto.setDatumvzniku(obecFromDb.getDatumvzniku());
    }
    //endregion
}
