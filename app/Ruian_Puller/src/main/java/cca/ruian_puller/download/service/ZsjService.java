package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.ZsjBoolean;
import cca.ruian_puller.download.dto.ZsjDto;
import cca.ruian_puller.download.repository.KatastralniUzemiRepository;
import cca.ruian_puller.download.repository.ZsjRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class ZsjService {

    private final ZsjRepository zsjRepository;
    private final KatastralniUzemiRepository katastralniUzemiRepository;

    @Autowired
    public ZsjService(ZsjRepository zsjRepository, KatastralniUzemiRepository katastralniUzemiRepository) {
        this.zsjRepository = zsjRepository;
        this.katastralniUzemiRepository = katastralniUzemiRepository;
    }

    public void prepareAndSave(List<ZsjDto> zsjDtos, AppConfig appConfig) {
        // Remove all Zsj with null Kod
        int initialSize = zsjDtos.size();
        zsjDtos.removeIf(zsjDto -> zsjDto.getKod() == null);
        if (initialSize != zsjDtos.size()) {
            log.warn("{} removed from Zsj due to null Kod", initialSize - zsjDtos.size());
        }

        // Based on ZsjBoolean from AppConfig, filter out ZsjDto
        if (!appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL))
            zsjDtos.forEach(zsjDto -> prepare(zsjDto, appConfig.getZsjConfig()));

        // Check all foreign keys
        int initialSize2 = zsjDtos.size();
        zsjDtos.removeIf(zsjDto -> !checkFK(zsjDto));
        if (initialSize2 != zsjDtos.size()) {
            log.warn("{} removed from Zsj due to missing foreign keys", initialSize2 - zsjDtos.size());
        }

        // Split list of ZsjDto into smaller lists
        for (int i = 0; i < zsjDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), zsjDtos.size());
            List<ZsjDto> subList = zsjDtos.subList(i, toIndex);
            zsjRepository.saveAll(subList);
            log.info("Saved {} out of {} Zsj", toIndex, zsjDtos.size());
        }
    }

    private boolean checkFK(ZsjDto zsj) {
        // Get the foreign key Kod
        Integer katastralniUzemiKod = zsj.getKatastralniuzemi();

        // Check if the foreign key Kod for KatastralniUzemi exists
        if (katastralniUzemiKod != null && !katastralniUzemiRepository.existsById(katastralniUzemiKod)) {
            log.warn("Zsj with Kod {} does not have valid foreign keys: KatastralniUzemi with Kod {}", zsj.getKod(), katastralniUzemiKod);
            return false;
        }

        return true;
    }

    //region Prepare with ZsjBoolean
    private void prepare(ZsjDto zsjDto, ZsjBoolean zsjConfig) {
        // Check if this dto is in db already
        ZsjDto zsjDtoFromDb = zsjRepository.findById(zsjDto.getKod()).orElse(null);
        boolean include = zsjConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL);
        if (zsjDtoFromDb == null) {
            setZsjDtoFields(zsjDto, zsjConfig, include);
        } else {
            setZsjDtoFieldsCombinedDB(zsjDto, zsjDtoFromDb, zsjConfig, include);
        }
    }

    private void setZsjDtoFields(ZsjDto zsjDto, ZsjBoolean zsjConfig, boolean include) {
        if (include != zsjConfig.isNazev()) zsjDto.setNazev(null);
        if (include != zsjConfig.isNespravny()) zsjDto.setNespravny(null);
        if (include != zsjConfig.isKatastralniuzemi()) zsjDto.setKatastralniuzemi(null);
        if (include != zsjConfig.isPlatiod()) zsjDto.setPlatiod(null);
        if (include != zsjConfig.isPlatido()) zsjDto.setPlatido(null);
        if (include != zsjConfig.isIdtransakce()) zsjDto.setIdtransakce(null);
        if (include != zsjConfig.isGlobalniidnavrhuzmeny()) zsjDto.setGlobalniidnavrhuzmeny(null);
        if (include != zsjConfig.isMluvnickecharakteristiky()) zsjDto.setMluvnickecharakteristiky(null);
        if (include != zsjConfig.isVymera()) zsjDto.setVymera(null);
        if (include != zsjConfig.isCharakterzsjkod()) zsjDto.setCharakterzsjkod(null);
        if (include != zsjConfig.isGeometriedefbod()) zsjDto.setGeometriedefbod(null);
        if (include != zsjConfig.isGeometrieorihranice()) zsjDto.setGeometrieorihranice(null);
        if (include != zsjConfig.isNespravneudaje()) zsjDto.setNespravneudaje(null);
        if (include != zsjConfig.isDatumvzniku()) zsjDto.setDatumvzniku(null);
    }

    private void setZsjDtoFieldsCombinedDB(ZsjDto zsjDto, ZsjDto zsjDtoFromDb, ZsjBoolean zsjConfig, boolean include) {
        if (zsjDtoFromDb.getNazev() != null && include == zsjConfig.isNazev())
            zsjDto.setNazev(zsjDtoFromDb.getNazev());
        if (zsjDtoFromDb.getNespravny() != null && include == zsjConfig.isNespravny())
            zsjDto.setNespravny(zsjDtoFromDb.getNespravny());
        if (zsjDtoFromDb.getKatastralniuzemi() != null && include == zsjConfig.isKatastralniuzemi())
            zsjDto.setKatastralniuzemi(zsjDtoFromDb.getKatastralniuzemi());
        if (zsjDtoFromDb.getPlatiod() != null && include == zsjConfig.isPlatiod())
            zsjDto.setPlatiod(zsjDtoFromDb.getPlatiod());
        if (zsjDtoFromDb.getPlatido() != null && include == zsjConfig.isPlatido())
            zsjDto.setPlatido(zsjDtoFromDb.getPlatido());
        if (zsjDtoFromDb.getIdtransakce() != null && include == zsjConfig.isIdtransakce())
            zsjDto.setIdtransakce(zsjDtoFromDb.getIdtransakce());
        if (zsjDtoFromDb.getGlobalniidnavrhuzmeny() != null && include == zsjConfig.isGlobalniidnavrhuzmeny())
            zsjDto.setGlobalniidnavrhuzmeny(zsjDtoFromDb.getGlobalniidnavrhuzmeny());
        if (zsjDtoFromDb.getMluvnickecharakteristiky() != null && include == zsjConfig.isMluvnickecharakteristiky())
            zsjDto.setMluvnickecharakteristiky(zsjDtoFromDb.getMluvnickecharakteristiky());
        if (zsjDtoFromDb.getVymera() != null && include == zsjConfig.isVymera())
            zsjDto.setVymera(zsjDtoFromDb.getVymera());
        if (zsjDtoFromDb.getCharakterzsjkod() != null && include == zsjConfig.isCharakterzsjkod())
            zsjDto.setCharakterzsjkod(zsjDtoFromDb.getCharakterzsjkod());
        if (zsjDtoFromDb.getGeometriedefbod() != null && include == zsjConfig.isGeometriedefbod())
            zsjDto.setGeometriedefbod(zsjDtoFromDb.getGeometriedefbod());
        if (zsjDtoFromDb.getGeometrieorihranice() != null && include == zsjConfig.isGeometrieorihranice())
            zsjDto.setGeometrieorihranice(zsjDtoFromDb.getGeometrieorihranice());
        if (zsjDtoFromDb.getNespravneudaje() != null && include == zsjConfig.isNespravneudaje())
            zsjDto.setNespravneudaje(zsjDtoFromDb.getNespravneudaje());
        if (zsjDtoFromDb.getDatumvzniku() != null && include == zsjConfig.isDatumvzniku())
            zsjDto.setDatumvzniku(zsjDtoFromDb.getDatumvzniku());
    }
}
