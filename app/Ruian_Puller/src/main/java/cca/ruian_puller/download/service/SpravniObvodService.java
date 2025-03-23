package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.SpravniObvodBoolean;
import cca.ruian_puller.download.dto.ObecDto;
import cca.ruian_puller.download.dto.SpravniObvodDto;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.SpravniObvodRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class SpravniObvodService {

    private final SpravniObvodRepository spravniObvodRepository;
    private final ObecRepository obecRepository;

    @Autowired
    public SpravniObvodService(SpravniObvodRepository spravniObvodRepository, ObecRepository obecRepository) {
        this.spravniObvodRepository = spravniObvodRepository;
        this.obecRepository = obecRepository;
    }

    public void prepareAndSave(List<SpravniObvodDto> spravniObvodDtos, AppConfig appConfig) {
        // Remove all SpravniObvod with null Kod
        int initialSize = spravniObvodDtos.size();
        spravniObvodDtos.removeIf(spravniObvodDto -> spravniObvodDto.getKod() == null);
        if (initialSize != spravniObvodDtos.size()) {
            log.warn("{} removed from SpravniObvod due to null Kod", initialSize - spravniObvodDtos.size());
        }

        // Based on SpravniObvodBoolean from AppConfig, filter out SpravniObvodDto
        if (!appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL))
            spravniObvodDtos.forEach(spravniObvodDto -> prepare(spravniObvodDto, appConfig.getSpravniObvodConfig()));

        // Check all foreign keys
        int initialSize2 = spravniObvodDtos.size();
        spravniObvodDtos.removeIf(spravniObvodDto -> !checkFK(spravniObvodDto));
        if (initialSize2 != spravniObvodDtos.size()) {
            log.warn("{} removed from SpravniObvod due to missing foreign keys", initialSize2 - spravniObvodDtos.size());
        }

        // Split list of SpravniObvodDto into smaller lists
        for (int i = 0; i < spravniObvodDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), spravniObvodDtos.size());
            List<SpravniObvodDto> subList = spravniObvodDtos.subList(i, toIndex);
            spravniObvodRepository.saveAll(subList);
            log.info("Saved {} out of {} SpravniObvod", toIndex, spravniObvodDtos.size());
        }
    }

    private boolean checkFK(SpravniObvodDto spravniObvodDto) {
        // Get the foreign key Kod
        Integer obecKod = spravniObvodDto.getObec();

        // Check if the foreign key Kod for Obec is valid
        if (obecKod != null && !obecRepository.existsByKod(obecKod)) {
            log.warn("SpravniObvod with Kod {} does not have valid foreign keys: Obec with Kod {}", spravniObvodDto.getKod(), obecKod);
            return false;
        }

        return true;
    }

    //region Prepare with SpravniObvodBoolean
    private void prepare(SpravniObvodDto spravniObvodDto, SpravniObvodBoolean spravniObvodConfig) {
        // Check if this dto is in db already
        SpravniObvodDto spravniObvodDtoFromDb = spravniObvodRepository.findByKod(spravniObvodDto.getKod());
        boolean include = spravniObvodConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (spravniObvodDtoFromDb == null) {
            setSpravniObvodDtoFields(spravniObvodDto, spravniObvodConfig, include);
        } else {
            setSpravniObvodDtoFieldsCombinedDB(spravniObvodDto, spravniObvodDtoFromDb, spravniObvodConfig, include);
        }
    }

    private void setSpravniObvodDtoFields(SpravniObvodDto spravniObvodDto, SpravniObvodBoolean spravniObvodConfig, boolean include) {
        if (include != spravniObvodConfig.isNazev()) spravniObvodDto.setNazev(null);
        if (include != spravniObvodConfig.isNespravny()) spravniObvodDto.setNespravny(null);
        if (include != spravniObvodConfig.isSpravnimomckod()) spravniObvodDto.setSpravnimomckod(null);
        if (include != spravniObvodConfig.isObec()) spravniObvodDto.setObec(null);
        if (include != spravniObvodConfig.isPlatiod()) spravniObvodDto.setPlatiod(null);
        if (include != spravniObvodConfig.isPlatido()) spravniObvodDto.setPlatido(null);
        if (include != spravniObvodConfig.isIdtransakce()) spravniObvodDto.setIdtransakce(null);
        if (include != spravniObvodConfig.isGlobalniidnavrhuzmeny()) spravniObvodDto.setGlobalniidnavrhuzmeny(null);
        if (include != spravniObvodConfig.isGeometriedefbod()) spravniObvodDto.setGeometriedefbod(null);
        if (include != spravniObvodConfig.isGeometrieorihranice()) spravniObvodDto.setGeometrieorihranice(null);
        if (include != spravniObvodConfig.isNespravneudaje()) spravniObvodDto.setNespravneudaje(null);
        if (include != spravniObvodConfig.isDatumvzniku()) spravniObvodDto.setDatumvzniku(null);
    }

    private void setSpravniObvodDtoFieldsCombinedDB(SpravniObvodDto spravniObvodDto, SpravniObvodDto spravniObvodDtoFromDb, SpravniObvodBoolean spravniObvodConfig, boolean include) {
        if (spravniObvodDtoFromDb.getNazev() != null && (include == spravniObvodConfig.isNazev()))
            spravniObvodDto.setNazev(spravniObvodDtoFromDb.getNazev());
        if (spravniObvodDtoFromDb.getNespravny() != null && (include == spravniObvodConfig.isNespravny()))
            spravniObvodDto.setNespravny(spravniObvodDtoFromDb.getNespravny());
        if (spravniObvodDtoFromDb.getSpravnimomckod() != null && (include == spravniObvodConfig.isSpravnimomckod()))
            spravniObvodDto.setSpravnimomckod(spravniObvodDtoFromDb.getSpravnimomckod());
        if (spravniObvodDtoFromDb.getObec() != null && (include == spravniObvodConfig.isObec()))
            spravniObvodDto.setObec(spravniObvodDtoFromDb.getObec());
        if (spravniObvodDtoFromDb.getPlatiod() != null && (include == spravniObvodConfig.isPlatiod()))
            spravniObvodDto.setPlatiod(spravniObvodDtoFromDb.getPlatiod());
        if (spravniObvodDtoFromDb.getPlatido() != null && (include == spravniObvodConfig.isPlatido()))
            spravniObvodDto.setPlatido(spravniObvodDtoFromDb.getPlatido());
        if (spravniObvodDtoFromDb.getIdtransakce() != null && (include == spravniObvodConfig.isIdtransakce()))
            spravniObvodDto.setIdtransakce(spravniObvodDtoFromDb.getIdtransakce());
        if (spravniObvodDtoFromDb.getGlobalniidnavrhuzmeny() != null && (include == spravniObvodConfig.isGlobalniidnavrhuzmeny()))
            spravniObvodDto.setGlobalniidnavrhuzmeny(spravniObvodDtoFromDb.getGlobalniidnavrhuzmeny());
        if (spravniObvodDtoFromDb.getGeometriedefbod() != null && (include == spravniObvodConfig.isGeometriedefbod()))
            spravniObvodDto.setGeometriedefbod(spravniObvodDtoFromDb.getGeometriedefbod());
        if (spravniObvodDtoFromDb.getGeometrieorihranice() != null && (include == spravniObvodConfig.isGeometrieorihranice()))
            spravniObvodDto.setGeometrieorihranice(spravniObvodDtoFromDb.getGeometrieorihranice());
        if (spravniObvodDtoFromDb.getNespravneudaje() != null && (include == spravniObvodConfig.isNespravneudaje()))
            spravniObvodDto.setNespravneudaje(spravniObvodDtoFromDb.getNespravneudaje());
        if (spravniObvodDtoFromDb.getDatumvzniku() != null && (include == spravniObvodConfig.isDatumvzniku()))
            spravniObvodDto.setDatumvzniku(spravniObvodDtoFromDb.getDatumvzniku());
    }
    //endregion
}
