package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.CastObceBoolean;
import cca.ruian_puller.download.dto.CastObceDto;
import cca.ruian_puller.download.repository.CastObceRepository;
import cca.ruian_puller.download.repository.ObecRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class CastObceService {

    private final CastObceRepository castObceRepository;
    private final ObecRepository obecRepository;

    @Autowired
    public CastObceService(CastObceRepository castObceRepository, ObecRepository obecRepository) {
        this.castObceRepository = castObceRepository;
        this.obecRepository = obecRepository;
    }

    public void prepareAndSave(List<CastObceDto> castObceDtos, AppConfig appConfig) {
        // Remove all CastObceDto with null Kod
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<CastObceDto> toDelete = new ArrayList<>();
        castObceDtos.forEach(castObceDto -> {
            iterator.getAndIncrement();
            // Remove all CastObceDto with null Kod
            if (castObceDto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(castObceDto);
                return;
            }
            // Check if all foreign keys exist
            if (!checkFK(castObceDto)) {
                removedByFK.getAndIncrement();
                toDelete.add(castObceDto);
                return;
            }
            // If dto is in db already, select it
            CastObceDto castObceFromDb = castObceRepository.findByKod(castObceDto.getKod());
            if (castObceFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(castObceDto, castObceFromDb);
            } else if (appConfig.getCastObceConfig() != null && !appConfig.getCastObceConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(castObceDto, castObceFromDb, appConfig.getCastObceConfig());
            }

            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= castObceDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of CastObceDtos processed");
            }
            if (iterator.get() >= castObceDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of CastObceDtos processed");
            }
            if (iterator.get() >= castObceDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of CastObceDtos processed");
            }
            if (iterator.get() >= castObceDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of CastObceDtos processed");
            }
        });

        // Remove all invalid CastObceDtos
        castObceDtos.removeAll(toDelete);

        // Log if some CastObceDto were removed
        if (removedByNullKod.get() > 0) log.warn("{} removed from CastObce due to null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.warn("{} removed from CastObce due to missing foreign keys", removedByFK.get());

        // Save CastObceDtos to the db
        for (int i = 0; i < castObceDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), castObceDtos.size());
            List<CastObceDto> subList = castObceDtos.subList(i, toIndex);
            castObceRepository.saveAll(subList);
            log.info("Saved {} out of {} CastObce", toIndex, castObceDtos.size());
        }
    }

    private boolean checkFK(CastObceDto castObceDto) {
        // Get the foreign key Kod
        Integer obecKod = castObceDto.getObec();

        // Check if the foreign key Kod for Obec is valid
        if (obecKod != null && !obecRepository.existsById(obecKod)) {
            log.warn("Obec with Kod {} does not have valid foreign keys: Obec with kod {}", castObceDto.getKod(), obecKod);
            return false;
        }

        return true;
    }

    private void updateWithDbValues(CastObceDto castObceDto, CastObceDto castObceFromDb) {
        if (castObceDto.getNazev() == null) castObceDto.setNazev(castObceFromDb.getNazev());
        if (castObceDto.getNespravny() == null) castObceDto.setNespravny(castObceFromDb.getNespravny());
        if (castObceDto.getObec() == null) castObceDto.setObec(castObceFromDb.getObec());
        if (castObceDto.getPlatiod() == null) castObceDto.setPlatiod(castObceFromDb.getPlatiod());
        if (castObceDto.getPlatido() == null) castObceDto.setPlatido(castObceFromDb.getPlatido());
        if (castObceDto.getIdtransakce() == null) castObceDto.setIdtransakce(castObceFromDb.getIdtransakce());
        if (castObceDto.getGlobalniidnavrhuzmeny() == null) castObceDto.setGlobalniidnavrhuzmeny(castObceFromDb.getGlobalniidnavrhuzmeny());
        if (castObceDto.getMluvnickecharakteristiky() == null) castObceDto.setMluvnickecharakteristiky(castObceFromDb.getMluvnickecharakteristiky());
        if (castObceDto.getGeometriedefbod() == null) castObceDto.setGeometriedefbod(castObceFromDb.getGeometriedefbod());
        if (castObceDto.getNespravneudaje() == null) castObceDto.setNespravneudaje(castObceFromDb.getNespravneudaje());
        if (castObceDto.getDatumvzniku() == null) castObceDto.setDatumvzniku(castObceFromDb.getDatumvzniku());
    }

    //region Prepare with CastObceBoolean
    private void prepare(CastObceDto castObceDto, CastObceDto castObceFromDb, CastObceBoolean castObceConfig) {
        boolean include = castObceConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (castObceFromDb == null) {
            setCastObceDtoFields(castObceDto, castObceConfig, include);
        } else {
            setCastObceDtoFieldsCombinedDB(castObceDto, castObceFromDb, castObceConfig, include);
        }
    }

    private void setCastObceDtoFields(CastObceDto castObceDto, CastObceBoolean castObceConfig, boolean include) {
        if (include != castObceConfig.isNazev()) castObceDto.setNazev(null);
        if (include != castObceConfig.isNespravny()) castObceDto.setNespravny(null);
        if (include != castObceConfig.isObec()) castObceDto.setObec(null);
        if (include != castObceConfig.isPlatiod()) castObceDto.setPlatiod(null);
        if (include != castObceConfig.isPlatido()) castObceDto.setPlatido(null);
        if (include != castObceConfig.isIdtransakce()) castObceDto.setIdtransakce(null);
        if (include != castObceConfig.isGlobalniidnavrhuzmeny()) castObceDto.setGlobalniidnavrhuzmeny(null);
        if (include != castObceConfig.isMluvnickecharakteristiky()) castObceDto.setMluvnickecharakteristiky(null);
        if (include != castObceConfig.isGeometriedefbod()) castObceDto.setGeometriedefbod(null);
        if (include != castObceConfig.isNespravneudaje()) castObceDto.setNespravneudaje(null);
        if (include != castObceConfig.isDatumvzniku()) castObceDto.setDatumvzniku(null);
}

    private void setCastObceDtoFieldsCombinedDB(CastObceDto castObceDto, CastObceDto castObceFromDb, CastObceBoolean castObceConfig, boolean include) {
        if (include != castObceConfig.isNazev()) castObceDto.setNazev(castObceFromDb.getNazev());
        if (include != castObceConfig.isNespravny()) castObceDto.setNespravny(castObceFromDb.getNespravny());
        if (include != castObceConfig.isObec()) castObceDto.setObec(castObceFromDb.getObec());
        if (include != castObceConfig.isPlatiod()) castObceDto.setPlatiod(castObceFromDb.getPlatiod());
        if (include != castObceConfig.isPlatido()) castObceDto.setPlatido(castObceFromDb.getPlatido());
        if (include != castObceConfig.isIdtransakce()) castObceDto.setIdtransakce(castObceFromDb.getIdtransakce());
        if (include != castObceConfig.isGlobalniidnavrhuzmeny()) castObceDto.setGlobalniidnavrhuzmeny(castObceFromDb.getGlobalniidnavrhuzmeny());
        if (include != castObceConfig.isMluvnickecharakteristiky()) castObceDto.setMluvnickecharakteristiky(castObceFromDb.getMluvnickecharakteristiky());
        if (include != castObceConfig.isGeometriedefbod()) castObceDto.setGeometriedefbod(castObceFromDb.getGeometriedefbod());
        if (include != castObceConfig.isNespravneudaje()) castObceDto.setNespravneudaje(castObceFromDb.getNespravneudaje());
        if (include != castObceConfig.isDatumvzniku()) castObceDto.setDatumvzniku(castObceFromDb.getDatumvzniku());
    }
    //endregion
}
