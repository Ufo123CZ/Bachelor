package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.ParcelaBoolean;
import cca.ruian_puller.download.dto.ParcelaDto;
import cca.ruian_puller.download.repository.KatastralniUzemiRepository;
import cca.ruian_puller.download.repository.ParcelaRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class ParcelaService {

    private final ParcelaRepository parcelaRepository;
    private final KatastralniUzemiRepository katastralniUzemiRepository;

    @Autowired
    public ParcelaService(ParcelaRepository parcelaRepository, KatastralniUzemiRepository katastralniUzemiRepository) {
        this.parcelaRepository = parcelaRepository;
        this.katastralniUzemiRepository = katastralniUzemiRepository;
    }

    public void prepareAndSave(List<ParcelaDto> parcelaDtos, AppConfig appConfig) {
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<ParcelaDto> toDelete = new ArrayList<>();
        parcelaDtos.forEach(parcelaDto -> {
            // Remove all Parcela with null Kod
            if (parcelaDto.getId() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(parcelaDto);
                return;
            }
            // Check if the foreign key is valid
            if (!checkFK(parcelaDto)) {
                removedByFK.getAndIncrement();
                toDelete.add(parcelaDto);
                return;
            }
            // If dto is in db already, select it
            ParcelaDto parcelaFromDb = parcelaRepository.findById(parcelaDto.getId()).orElse(null);
            if (parcelaFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(parcelaDto, parcelaFromDb);
            } else if (appConfig.getParcelaConfig() != null && !appConfig.getParcelaConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(parcelaDto, parcelaFromDb, appConfig.getParcelaConfig());
            }
            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= parcelaDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of ParcelaDtos processed");
            }
            if (iterator.get() >= parcelaDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of ParcelaDtos processed");
            }
            if (iterator.get() >= parcelaDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of ParcelaDtos processed");
            }
            if (iterator.get() >= parcelaDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of ParcelaDtos processed");
            }
        });

        // Remove all invalid ParcelaDtos
        parcelaDtos.removeAll(toDelete);

        // Log if some ParcelaDto were removed
        if (removedByNullKod.get() > 0) log.warn("Removed {} Parcela with null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.warn("Removed {} Parcela with invalid foreign keys", removedByFK.get());

        // Save ParcelaDtos to db
        for (int i = 0; i < parcelaDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), parcelaDtos.size());
            List<ParcelaDto> subList = parcelaDtos.subList(i, toIndex);
            parcelaRepository.saveAll(subList);
            log.info("Saved {} out of {} Parcela", toIndex, parcelaDtos.size());
        }
    }

    private boolean checkFK(ParcelaDto parcelaDto) {
        // Get the foreign key Kod
        Integer katastralniUzemiKod = parcelaDto.getKatastralniuzemi();

        // Check if the foreign key Kod for KatastralniUzemi exists
        if (katastralniUzemiKod != null && !katastralniUzemiRepository.existsByKod(katastralniUzemiKod)) {
            log.warn("Parcela with Id {} does not have valid foreign keys: KatastralniUzemi with Kod {}", parcelaDto.getId(), katastralniUzemiKod);
            return false;
        }

        return true;
    }

    private void updateWithDbValues(ParcelaDto parcelaDto, ParcelaDto parcelaFromDb) {
        if (parcelaDto.getNespravny() == null) parcelaDto.setNespravny(parcelaFromDb.getNespravny());
        if (parcelaDto.getKmenovecislo() == null) parcelaDto.setKmenovecislo(parcelaFromDb.getKmenovecislo());
        if (parcelaDto.getPododdelenicisla() == null) parcelaDto.setPododdelenicisla(parcelaFromDb.getPododdelenicisla());
        if (parcelaDto.getVymeraparcely() == null) parcelaDto.setVymeraparcely(parcelaFromDb.getVymeraparcely());
        if (parcelaDto.getZpusobyvyuzitipozemku() == null) parcelaDto.setZpusobyvyuzitipozemku(parcelaFromDb.getZpusobyvyuzitipozemku());
        if (parcelaDto.getDruhcislovanikod() == null) parcelaDto.setDruhcislovanikod(parcelaFromDb.getDruhcislovanikod());
        if (parcelaDto.getDruhpozemkukod() == null) parcelaDto.setDruhpozemkukod(parcelaFromDb.getDruhpozemkukod());
        if (parcelaDto.getKatastralniuzemi() == null) parcelaDto.setKatastralniuzemi(parcelaFromDb.getKatastralniuzemi());
        if (parcelaDto.getPlatiod() == null) parcelaDto.setPlatiod(parcelaFromDb.getPlatiod());
        if (parcelaDto.getPlatido() == null) parcelaDto.setPlatido(parcelaFromDb.getPlatido());
        if (parcelaDto.getIdtransakce() == null) parcelaDto.setIdtransakce(parcelaFromDb.getIdtransakce());
        if (parcelaDto.getRizeniid() == null) parcelaDto.setRizeniid(parcelaFromDb.getRizeniid());
        if (parcelaDto.getBonitovanedily() == null) parcelaDto.setBonitovanedily(parcelaFromDb.getBonitovanedily());
        if (parcelaDto.getZpusobyochranypozemku() == null) parcelaDto.setZpusobyochranypozemku(parcelaFromDb.getZpusobyochranypozemku());
        if (parcelaDto.getGeometriedefbod() == null) parcelaDto.setGeometriedefbod(parcelaFromDb.getGeometriedefbod());
        if (parcelaDto.getGeometrieorihranice() == null) parcelaDto.setGeometrieorihranice(parcelaFromDb.getGeometrieorihranice());
        if (parcelaDto.getNespravneudaje() == null) parcelaDto.setNespravneudaje(parcelaFromDb.getNespravneudaje());
    }

    //region Prepare with ParcelaBoolean
    private void prepare(ParcelaDto parcelaDto, ParcelaDto parcelaFromDb, ParcelaBoolean parcelaConfig) {
        boolean include = parcelaConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL);
        if (parcelaFromDb == null) {
            setParcelaDtoFields(parcelaDto, parcelaConfig, include);
        } else {
            setParcelaDtoFieldsCombinedDB(parcelaDto, parcelaFromDb, parcelaConfig, include);
        }
    }

    private void setParcelaDtoFields(ParcelaDto parcelaDto, ParcelaBoolean parcelaConfig, boolean include) {
        if (include != parcelaConfig.isNespravny()) parcelaDto.setNespravny(null);
        if (include != parcelaConfig.isKmenovecislo()) parcelaDto.setKmenovecislo(null);
        if (include != parcelaConfig.isPododdelenicisla()) parcelaDto.setPododdelenicisla(null);
        if (include != parcelaConfig.isVymeraparcely()) parcelaDto.setVymeraparcely(null);
        if (include != parcelaConfig.isZpusobyvyuzitipozemku()) parcelaDto.setZpusobyvyuzitipozemku(null);
        if (include != parcelaConfig.isDruhcislovanikod()) parcelaDto.setDruhcislovanikod(null);
        if (include != parcelaConfig.isDruhpozemkukod()) parcelaDto.setDruhpozemkukod(null);
        if (include != parcelaConfig.isKatastralniuzemi()) parcelaDto.setKatastralniuzemi(null);
        if (include != parcelaConfig.isPlatiod()) parcelaDto.setPlatiod(null);
        if (include != parcelaConfig.isPlatido()) parcelaDto.setPlatido(null);
        if (include != parcelaConfig.isIdtransakce()) parcelaDto.setIdtransakce(null);
        if (include != parcelaConfig.isRizeniid()) parcelaDto.setRizeniid(null);
        if (include != parcelaConfig.isBonitovanedily()) parcelaDto.setBonitovanedily(null);
        if (include != parcelaConfig.isZpusobyochranypozemku()) parcelaDto.setZpusobyochranypozemku(null);
        if (include != parcelaConfig.isGeometriedefbod()) parcelaDto.setGeometriedefbod(null);
        if (include != parcelaConfig.isGeometrieorihranice()) parcelaDto.setGeometrieorihranice(null);
        if (include != parcelaConfig.isNespravneudaje()) parcelaDto.setNespravneudaje(null);
    }

    private void setParcelaDtoFieldsCombinedDB(ParcelaDto parcelaDto, ParcelaDto parcelaFromDb, ParcelaBoolean parcelaConfig, boolean include) {
        if (include != parcelaConfig.isNespravny()) parcelaDto.setNespravny(parcelaFromDb.getNespravny());
        if (include != parcelaConfig.isKmenovecislo()) parcelaDto.setKmenovecislo(parcelaFromDb.getKmenovecislo());
        if (include != parcelaConfig.isPododdelenicisla()) parcelaDto.setPododdelenicisla(parcelaFromDb.getPododdelenicisla());
        if (include != parcelaConfig.isVymeraparcely()) parcelaDto.setVymeraparcely(parcelaFromDb.getVymeraparcely());
        if (include != parcelaConfig.isZpusobyvyuzitipozemku()) parcelaDto.setZpusobyvyuzitipozemku(parcelaFromDb.getZpusobyvyuzitipozemku());
        if (include != parcelaConfig.isDruhcislovanikod()) parcelaDto.setDruhcislovanikod(parcelaFromDb.getDruhcislovanikod());
        if (include != parcelaConfig.isDruhpozemkukod()) parcelaDto.setDruhpozemkukod(parcelaFromDb.getDruhpozemkukod());
        if (include != parcelaConfig.isKatastralniuzemi()) parcelaDto.setKatastralniuzemi(parcelaFromDb.getKatastralniuzemi());
        if (include != parcelaConfig.isPlatiod()) parcelaDto.setPlatiod(parcelaFromDb.getPlatiod());
        if (include != parcelaConfig.isPlatido()) parcelaDto.setPlatido(parcelaFromDb.getPlatido());
        if (include != parcelaConfig.isIdtransakce()) parcelaDto.setIdtransakce(parcelaFromDb.getIdtransakce());
        if (include != parcelaConfig.isRizeniid()) parcelaDto.setRizeniid(parcelaFromDb.getRizeniid());
        if (include != parcelaConfig.isBonitovanedily()) parcelaDto.setBonitovanedily(parcelaFromDb.getBonitovanedily());
        if (include != parcelaConfig.isZpusobyochranypozemku()) parcelaDto.setZpusobyochranypozemku(parcelaFromDb.getZpusobyochranypozemku());
        if (include != parcelaConfig.isGeometriedefbod()) parcelaDto.setGeometriedefbod(parcelaFromDb.getGeometriedefbod());
        if (include != parcelaConfig.isGeometrieorihranice()) parcelaDto.setGeometrieorihranice(parcelaFromDb.getGeometrieorihranice());
        if (include != parcelaConfig.isNespravneudaje()) parcelaDto.setNespravneudaje(parcelaFromDb.getNespravneudaje());
    }
    //endregion
}
