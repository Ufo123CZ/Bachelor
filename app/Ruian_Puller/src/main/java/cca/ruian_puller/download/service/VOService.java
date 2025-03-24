package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.VOBoolean;
import cca.ruian_puller.download.dto.VODto;
import cca.ruian_puller.download.repository.MomcRepository;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.VORepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class VOService {

    private final VORepository voRepository;
    private final ObecRepository obecRepository;
    private final MomcRepository momcRepository;

    @Autowired
    public VOService(VORepository voRepository, ObecRepository obecRepository, MomcRepository momcRepository) {
        this.voRepository = voRepository;
        this.obecRepository = obecRepository;
        this.momcRepository = momcRepository;
    }

    public void prepareAndSave(List<VODto> voDtos, AppConfig appConfig) {
        // Remove all VO with null Kod
        int initialSize = voDtos.size();
        voDtos.removeIf(voDto -> voDto.getKod() == null);
        if (initialSize != voDtos.size()) {
            log.warn("{} removed from VO due to null Kod", initialSize - voDtos.size());
        }

        // Based on VOBoolean from AppConfig, filter out VODto
        if (appConfig.getVoConfig() != null && !appConfig.getVoConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL))
            voDtos.forEach(voDto -> prepare(voDto, appConfig.getVoConfig()));

        // Check all foreign keys
        int initialSize2 = voDtos.size();
        voDtos.removeIf(voDto -> !checkFK(voDto));
        if (initialSize2 != voDtos.size()) {
            log.warn("{} removed from VO due to missing foreign keys", initialSize2 - voDtos.size());
        }

        // Split list of VODto into smaller lists
        for (int i = 0; i < voDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), voDtos.size());
            List<VODto> subList = voDtos.subList(i, toIndex);
            voRepository.saveAll(subList);
            log.info("Saved {} out of {} VO", toIndex, voDtos.size());
        }
    }

    private boolean checkFK(VODto voDto) {
        // Get the foreign keys Kod
        Integer obecKod = voDto.getObec();
        Integer momcKod = voDto.getMomc();

        // Check if the foreign key Kod for Obec exists
        if (obecKod != null && !obecRepository.existsByKod(obecKod)) {
            log.warn("VO with Kod {} does not have a valid foreign key: Obec with Kod {}", voDto.getKod(), obecKod);
            return false;
        }

        // Check if the foreign key Kod for Momc exists
        if (momcKod != null && !momcRepository.existsByKod(momcKod)) {
            log.warn("VO with Kod {} does not have a valid foreign key: Momc with Kod {}", voDto.getKod(), momcKod);
            return false;
        }

        return true;
    }

    //region Prepare with VOBoolean
    private void prepare(VODto voDto, VOBoolean voConfig) {
        // Check if this dto is in db already
        VODto voDtoFromDb = voRepository.findByKod(voDto.getKod());
        boolean include = voConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (voDtoFromDb == null) {
            setVODtoFields(voDto, voConfig, include);
        } else {
            setVODtoFieldsCombinedDB(voDto, voDtoFromDb, voConfig, include);
        }
    }

    private void setVODtoFields(VODto voDto, VOBoolean voConfig, boolean include) {
        if (include != voConfig.isPlatiod()) voDto.setPlatiod(null);
        if (include != voConfig.isPlatido()) voDto.setPlatido(null);
        if (include != voConfig.isIdtransakce()) voDto.setIdtransakce(null);
        if (include != voConfig.isGlobalniidnavrhuzmeny()) voDto.setGlobalniidnavrhuzmeny(null);
        if (include != voConfig.isGeometriedefbod()) voDto.setGeometriedefbod(null);
        if (include != voConfig.isGeometriegenhranice()) voDto.setGeometriegenhranice(null);
        if (include != voConfig.isGeometrieorihranice()) voDto.setGeometrieorihranice(null);
        if (include != voConfig.isNespravneudaje()) voDto.setNespravneudaje(null);
        if (include != voConfig.isCislo()) voDto.setCislo(null);
        if (include != voConfig.isNespravny()) voDto.setNespravny(null);
        if (include != voConfig.isObec()) voDto.setObec(null);
        if (include != voConfig.isMomc()) voDto.setMomc(null);
        if (include != voConfig.isPoznamka()) voDto.setPoznamka(null);
    }

    private void setVODtoFieldsCombinedDB(VODto voDto, VODto voDtoFromDb, VOBoolean voConfig, boolean include) {
        if (include != voConfig.isPlatiod()) voDto.setPlatiod(voDtoFromDb.getPlatiod());
        if (include != voConfig.isPlatido()) voDto.setPlatido(voDtoFromDb.getPlatido());
        if (include != voConfig.isIdtransakce()) voDto.setIdtransakce(voDtoFromDb.getIdtransakce());
        if (include != voConfig.isGlobalniidnavrhuzmeny()) voDto.setGlobalniidnavrhuzmeny(voDtoFromDb.getGlobalniidnavrhuzmeny());
        if (include != voConfig.isGeometriedefbod()) voDto.setGeometriedefbod(voDtoFromDb.getGeometriedefbod());
        if (include != voConfig.isGeometriegenhranice()) voDto.setGeometriegenhranice(voDtoFromDb.getGeometriegenhranice());
        if (include != voConfig.isGeometrieorihranice()) voDto.setGeometrieorihranice(voDtoFromDb.getGeometrieorihranice());
        if (include != voConfig.isNespravneudaje()) voDto.setNespravneudaje(voDtoFromDb.getNespravneudaje());
        if (include != voConfig.isCislo()) voDto.setCislo(voDtoFromDb.getCislo());
        if (include != voConfig.isNespravny()) voDto.setNespravny(voDtoFromDb.getNespravny());
        if (include != voConfig.isObec()) voDto.setObec(voDtoFromDb.getObec());
        if (include != voConfig.isMomc()) voDto.setMomc(voDtoFromDb.getMomc());
        if (include != voConfig.isPoznamka()) voDto.setPoznamka(voDtoFromDb.getPoznamka());
    }
    //endregion
}
