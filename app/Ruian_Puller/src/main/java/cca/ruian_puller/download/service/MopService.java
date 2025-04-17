package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.MopBoolean;
import cca.ruian_puller.download.dto.MopDto;
import cca.ruian_puller.download.repository.MopRepository;
import cca.ruian_puller.download.repository.ObecRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class MopService {

    private final MopRepository mopRepository;
    private final ObecRepository obecRepository;

    @Autowired
    public MopService(MopRepository mopRepository, ObecRepository obecRepository) {
        this.mopRepository = mopRepository;
        this.obecRepository = obecRepository;
    }

    public void prepareAndSave(List<MopDto> mopDtos, AppConfig appConfig) {
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<MopDto> toDelete = new ArrayList<>();
        mopDtos.forEach(mopDto -> {
            iterator.getAndIncrement();
            // Remove MopDto if it has null Kod
            if (mopDto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(mopDto);
                return;
            }
            // Check if the foreign key is valid
            if (!checkFK(mopDto)) {
                removedByFK.getAndIncrement();
                toDelete.add(mopDto);
                return;
            }
            // If dto is in db already, select it
            MopDto mopFromDb = mopRepository.findByKod(mopDto.getKod());
            if (mopFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(mopDto, mopFromDb);
            } else if (appConfig.getMopConfig() != null && !appConfig.getMopConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(mopDto, mopFromDb, appConfig.getMopConfig());
            }
            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= mopDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of MopDtos processed");
            }
            if (iterator.get() >= mopDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of MopDtos processed");
            }
            if (iterator.get() >= mopDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of MopDtos processed");
            }
            if (iterator.get() >= mopDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of MopDtos processed");
            }
        });

        // Remove all invalid MopDtos
        mopDtos.removeAll(toDelete);

        // Log if some MomcDto were removed
        if (removedByNullKod.get() > 0) log.warn("{} removed from Mop due to null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.warn("{} removed from Mop due to missing foreign keys", removedByFK.get());

        // Save MopDtos to db
        for (int i = 0; i < mopDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), mopDtos.size());
            List<MopDto> subList = mopDtos.subList(i, toIndex);
            mopRepository.saveAll(subList);
            log.info("Saved {} out of {} Mop", toIndex, mopDtos.size());
        }
    }

    private boolean checkFK(MopDto mopDto) {
        // Get the foreign key Kod
        Integer obecKod = mopDto.getObec();

        // Check if the foreign key Kod for Obec is valid
        if (obecKod != null && !obecRepository.existsByKod(obecKod)) {
            log.warn("Mop with Kod {} does not have valid foreign keys: Obec with Kod {}", mopDto.getKod(), obecKod);
            return false;
        }
        return true;
    }

    private void updateWithDbValues(MopDto mopDto, MopDto mopFromDb) {
        if (mopDto.getNazev() == null) mopDto.setNazev(mopFromDb.getNazev());
        if (mopDto.getNespravny() == null) mopDto.setNespravny(mopFromDb.getNespravny());
        if (mopDto.getObec() == null) mopDto.setObec(mopFromDb.getObec());
        if (mopDto.getPlatiod() == null) mopDto.setPlatiod(mopFromDb.getPlatiod());
        if (mopDto.getPlatido() == null) mopDto.setPlatido(mopFromDb.getPlatido());
        if (mopDto.getIdtransakce() == null) mopDto.setIdtransakce(mopFromDb.getIdtransakce());
        if (mopDto.getGlobalniidnavrhuzmeny() == null) mopDto.setGlobalniidnavrhuzmeny(mopFromDb.getGlobalniidnavrhuzmeny());
        if (mopDto.getGeometriedefbod() == null) mopDto.setGeometriedefbod(mopFromDb.getGeometriedefbod());
        if (mopDto.getGeometrieorihranice() == null) mopDto.setGeometrieorihranice(mopFromDb.getGeometrieorihranice());
        if (mopDto.getNespravneudaje() == null) mopDto.setNespravneudaje(mopFromDb.getNespravneudaje());
        if (mopDto.getDatumvzniku() == null) mopDto.setDatumvzniku(mopFromDb.getDatumvzniku());
    }

    //region Prepare with MopBoolean
    private void prepare(MopDto mopDto, MopDto mopFromDb, MopBoolean mopConfig) {
        boolean include = mopConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (mopFromDb == null) {
            setMopDtoFields(mopDto, mopConfig, include);
        } else {
            setMopDtoFieldsCombinedFB(mopDto, mopFromDb, mopConfig, include);
        }
    }

    private void setMopDtoFields(MopDto mopDto, MopBoolean mopConfig, boolean include) {
        if (include != mopConfig.isNazev()) mopDto.setNazev(null);
        if (include != mopConfig.isNespravny()) mopDto.setNespravny(null);
        if (include != mopConfig.isObec()) mopDto.setObec(null);
        if (include != mopConfig.isPlatiod()) mopDto.setPlatiod(null);
        if (include != mopConfig.isPlatido()) mopDto.setPlatido(null);
        if (include != mopConfig.isIdtransakce()) mopDto.setIdtransakce(null);
        if (include != mopConfig.isGlobalniidnavrhuzmeny()) mopDto.setGlobalniidnavrhuzmeny(null);
        if (include != mopConfig.isGeometriedefbod()) mopDto.setGeometriedefbod(null);
        if (include != mopConfig.isGeometrieorihranice()) mopDto.setGeometrieorihranice(null);
        if (include != mopConfig.isNespravneudaje()) mopDto.setNespravneudaje(null);
        if (include != mopConfig.isDatumvzniku()) mopDto.setDatumvzniku(null);
    }

    private void setMopDtoFieldsCombinedFB(MopDto mopDto, MopDto mopFromDb, MopBoolean mopConfig, boolean include) {
        if (include != mopConfig.isNazev()) mopDto.setNazev(mopFromDb.getNazev());
        if (include != mopConfig.isNespravny()) mopDto.setNespravny(mopFromDb.getNespravny());
        if (include != mopConfig.isObec()) mopDto.setObec(mopFromDb.getObec());
        if (include != mopConfig.isPlatiod()) mopDto.setPlatiod(mopFromDb.getPlatiod());
        if (include != mopConfig.isPlatido()) mopDto.setPlatido(mopFromDb.getPlatido());
        if (include != mopConfig.isIdtransakce()) mopDto.setIdtransakce(mopFromDb.getIdtransakce());
        if (include != mopConfig.isGlobalniidnavrhuzmeny()) mopDto.setGlobalniidnavrhuzmeny(mopFromDb.getGlobalniidnavrhuzmeny());
        if (include != mopConfig.isGeometriedefbod()) mopDto.setGeometriedefbod(mopFromDb.getGeometriedefbod());
        if (include != mopConfig.isGeometrieorihranice()) mopDto.setGeometrieorihranice(mopFromDb.getGeometrieorihranice());
        if (include != mopConfig.isNespravneudaje()) mopDto.setNespravneudaje(mopFromDb.getNespravneudaje());
        if (include != mopConfig.isDatumvzniku()) mopDto.setDatumvzniku(mopFromDb.getDatumvzniku());
    }
    //endregion

}
