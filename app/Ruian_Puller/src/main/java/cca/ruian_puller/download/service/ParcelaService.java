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

import java.util.List;

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
        // Remove all Parcela with null Id
        int initialSize = parcelaDtos.size();
        parcelaDtos.removeIf(parcelaDto -> parcelaDto.getId() == null);
        if (initialSize != parcelaDtos.size()) {
            log.warn("{} removed from Parcela due to null Id", initialSize - parcelaDtos.size());
        }

        // Based on ParcelaBoolean from AppConfig, filter out ParcelaDto
        if (!appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL))
            parcelaDtos.forEach(parcelaDto -> prepare(parcelaDto, appConfig.getParcelaConfig()));

        // Check all foreign keys
        int initialSize2 = parcelaDtos.size();
        parcelaDtos.removeIf(parcelaDto -> !checkFK(parcelaDto));
        if (initialSize2 != parcelaDtos.size()) {
            log.warn("{} removed from Parcela due to missing foreign keys", initialSize2 - parcelaDtos.size());
        }

        // Split list of ParcelaDto into smaller lists
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

    //region Prepare with ParcelaBoolean
    private void prepare(ParcelaDto parcelaDto, ParcelaBoolean parcelaConfig) {
        // Check if this dto is in db already
        ParcelaDto parcelaDtoFromDb = parcelaRepository.findById(parcelaDto.getId()).orElse(null);
        boolean include = parcelaConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL);
        if (parcelaDtoFromDb == null) {
            setParcelaDtoFields(parcelaDto, parcelaConfig, include);
        } else {
            setParcelaDtoFieldsCombinedDB(parcelaDto, parcelaDtoFromDb, parcelaConfig, include);
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

    private void setParcelaDtoFieldsCombinedDB(ParcelaDto parcelaDto, ParcelaDto parcelaDtoFromDb, ParcelaBoolean parcelaConfig, boolean include) {
        if (parcelaDtoFromDb.getNespravny() != null && include == parcelaConfig.isNespravny())
            parcelaDto.setNespravny(parcelaDtoFromDb.getNespravny());
        if (parcelaDtoFromDb.getKmenovecislo() != null && include == parcelaConfig.isKmenovecislo())
            parcelaDto.setKmenovecislo(parcelaDtoFromDb.getKmenovecislo());
        if (parcelaDtoFromDb.getPododdelenicisla() != null && include == parcelaConfig.isPododdelenicisla())
            parcelaDto.setPododdelenicisla(parcelaDtoFromDb.getPododdelenicisla());
        if (parcelaDtoFromDb.getVymeraparcely() != null && include == parcelaConfig.isVymeraparcely())
            parcelaDto.setVymeraparcely(parcelaDtoFromDb.getVymeraparcely());
        if (parcelaDtoFromDb.getZpusobyvyuzitipozemku() != null && include == parcelaConfig.isZpusobyvyuzitipozemku())
            parcelaDto.setZpusobyvyuzitipozemku(parcelaDtoFromDb.getZpusobyvyuzitipozemku());
        if (parcelaDtoFromDb.getDruhcislovanikod() != null && include == parcelaConfig.isDruhcislovanikod())
            parcelaDto.setDruhcislovanikod(parcelaDtoFromDb.getDruhcislovanikod());
        if (parcelaDtoFromDb.getDruhpozemkukod() != null && include == parcelaConfig.isDruhpozemkukod())
            parcelaDto.setDruhpozemkukod(parcelaDtoFromDb.getDruhpozemkukod());
        if (parcelaDtoFromDb.getKatastralniuzemi() != null && include == parcelaConfig.isKatastralniuzemi())
            parcelaDto.setKatastralniuzemi(parcelaDtoFromDb.getKatastralniuzemi());
        if (parcelaDtoFromDb.getPlatiod() != null && include == parcelaConfig.isPlatiod())
            parcelaDto.setPlatiod(parcelaDtoFromDb.getPlatiod());
        if (parcelaDtoFromDb.getPlatido() != null && include == parcelaConfig.isPlatido())
            parcelaDto.setPlatido(parcelaDtoFromDb.getPlatido());
        if (parcelaDtoFromDb.getIdtransakce() != null && include == parcelaConfig.isIdtransakce())
            parcelaDto.setIdtransakce(parcelaDtoFromDb.getIdtransakce());
        if (parcelaDtoFromDb.getRizeniid() != null && include == parcelaConfig.isRizeniid())
            parcelaDto.setRizeniid(parcelaDtoFromDb.getRizeniid());
        if (parcelaDtoFromDb.getBonitovanedily() != null && include == parcelaConfig.isBonitovanedily())
            parcelaDto.setBonitovanedily(parcelaDtoFromDb.getBonitovanedily());
        if (parcelaDtoFromDb.getZpusobyochranypozemku() != null && include == parcelaConfig.isZpusobyochranypozemku())
            parcelaDto.setZpusobyochranypozemku(parcelaDtoFromDb.getZpusobyochranypozemku());
        if (parcelaDtoFromDb.getGeometriedefbod() != null && include == parcelaConfig.isGeometriedefbod())
            parcelaDto.setGeometriedefbod(parcelaDtoFromDb.getGeometriedefbod());
        if (parcelaDtoFromDb.getGeometrieorihranice() != null && include == parcelaConfig.isGeometrieorihranice())
            parcelaDto.setGeometrieorihranice(parcelaDtoFromDb.getGeometrieorihranice());
        if (parcelaDtoFromDb.getNespravneudaje() != null && include == parcelaConfig.isNespravneudaje())
            parcelaDto.setNespravneudaje(parcelaDtoFromDb.getNespravneudaje());
    }
    //endregion
}
