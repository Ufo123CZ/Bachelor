package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.VuscBoolean;
import cca.ruian_puller.download.dto.VuscDto;
import cca.ruian_puller.download.repository.RegionSoudrznostiRepository;
import cca.ruian_puller.download.repository.VuscRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class VuscService {

    private final VuscRepository vuscRepository;
    private final RegionSoudrznostiRepository regionSoudrznostiRepository;

    @Autowired
    public VuscService(VuscRepository vuscRepository, RegionSoudrznostiRepository regionSoudrznostiRepository) {
        this.vuscRepository = vuscRepository;
        this.regionSoudrznostiRepository = regionSoudrznostiRepository;
    }

    public void prepareAndSave(List<VuscDto> vuscDtos, AppConfig appConfig) {
        // Remove all Vusc with null Kod
        int initialSize = vuscDtos.size();
        vuscDtos.removeIf(vuscDto -> vuscDto.getKod() == null);
        if (initialSize != vuscDtos.size()) {
            log.warn("{} removed from Vusc due to null Kod", initialSize - vuscDtos.size());
        }

        // Based on VuscBoolean from AppConfig, filter out VuscDto
        if (appConfig.getVuscConfig() != null && !appConfig.getVuscConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL))
            vuscDtos.forEach(vuscDto -> prepare(vuscDto, appConfig.getVuscConfig()));

        // Check all foreign keys
        int initialSize2 = vuscDtos.size();
        vuscDtos.removeIf(vuscDto -> !checkFK(vuscDto));
        if (initialSize2 != vuscDtos.size()) {
            log.warn("{} removed from Vusc due to missing foreign keys", initialSize2 - vuscDtos.size());
        }

        // Split list of VuscDto into smaller lists
        for (int i = 0; i < vuscDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), vuscDtos.size());
            List<VuscDto> subList = vuscDtos.subList(i, toIndex);
            vuscRepository.saveAll(subList);
            log.info("Saved {} out of {} Vusc", toIndex, vuscDtos.size());
        }
    }

    private boolean checkFK(VuscDto vuscDto) {
        // Get the foreign keys Kod
        Integer regionSoudrznostiKod = vuscDto.getRegionsoudrznosti();

        // Check if the foreign key Kod for RegionSoudrznosti exists
        if (regionSoudrznostiKod != null && !regionSoudrznostiRepository.existsByKod(regionSoudrznostiKod)) {
            log.warn("Vusc with Kod {} does not have valid foreign keys: RegionSoudrznosti with Kod {}", vuscDto.getKod(), regionSoudrznostiKod);
            return false;
        }

        return true;
    }

    //region Prepare with VuscBoolean
    private void prepare(VuscDto vuscDto, VuscBoolean vuscConfig) {
        // Check if this dto is in db already
        VuscDto vuscDtoFromDb = vuscRepository.findByKod(vuscDto.getKod());
        boolean include = vuscConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL);
        if (vuscDtoFromDb == null) {
            setVuscDtoFields(vuscDto, vuscConfig, include);
        } else {
            setVuscDtoFieldsCombinedDB(vuscDto, vuscDtoFromDb, vuscConfig, include);
        }
    }

    private void setVuscDtoFields(VuscDto vuscDto, VuscBoolean vuscConfig, boolean include) {
        if (include != vuscConfig.isNazev()) vuscDto.setNazev(null);
        if (include != vuscConfig.isNespravny()) vuscDto.setNespravny(null);
        if (include != vuscConfig.isRegionsoudrznosti()) vuscDto.setRegionsoudrznosti(null);
        if (include != vuscConfig.isPlatiod()) vuscDto.setPlatiod(null);
        if (include != vuscConfig.isPlatido()) vuscDto.setPlatido(null);
        if (include != vuscConfig.isIdtransakce()) vuscDto.setIdtransakce(null);
        if (include != vuscConfig.isGlobalniidnavrhuzmeny()) vuscDto.setGlobalniidnavrhuzmeny(null);
        if (include != vuscConfig.isNutslau()) vuscDto.setNutslau(null);
        if (include != vuscConfig.isGeometriedefbod()) vuscDto.setGeometriedefbod(null);
        if (include != vuscConfig.isGeometriegenhranice()) vuscDto.setGeometriegenhranice(null);
        if (include != vuscConfig.isGeometrieorihranice()) vuscDto.setGeometrieorihranice(null);
        if (include != vuscConfig.isNespravneudaje()) vuscDto.setNespravneudaje(null);
        if (include != vuscConfig.isDatumvzniku()) vuscDto.setDatumvzniku(null);
    }

    private void setVuscDtoFieldsCombinedDB(VuscDto vuscDto, VuscDto vuscDtoFromDb, VuscBoolean vuscConfig, boolean include) {
        if (include != vuscConfig.isNazev()) vuscDto.setNazev(vuscDtoFromDb.getNazev());
        if (include != vuscConfig.isNespravny()) vuscDto.setNespravny(vuscDtoFromDb.getNespravny());
        if (include != vuscConfig.isRegionsoudrznosti()) vuscDto.setRegionsoudrznosti(vuscDtoFromDb.getRegionsoudrznosti());
        if (include != vuscConfig.isPlatiod()) vuscDto.setPlatiod(vuscDtoFromDb.getPlatiod());
        if (include != vuscConfig.isPlatido()) vuscDto.setPlatido(vuscDtoFromDb.getPlatido());
        if (include != vuscConfig.isIdtransakce()) vuscDto.setIdtransakce(vuscDtoFromDb.getIdtransakce());
        if (include != vuscConfig.isGlobalniidnavrhuzmeny()) vuscDto.setGlobalniidnavrhuzmeny(vuscDtoFromDb.getGlobalniidnavrhuzmeny());
        if (include != vuscConfig.isNutslau()) vuscDto.setNutslau(vuscDtoFromDb.getNutslau());
        if (include != vuscConfig.isGeometriedefbod()) vuscDto.setGeometriedefbod(vuscDtoFromDb.getGeometriedefbod());
        if (include != vuscConfig.isGeometriegenhranice()) vuscDto.setGeometriegenhranice(vuscDtoFromDb.getGeometriegenhranice());
        if (include != vuscConfig.isGeometrieorihranice()) vuscDto.setGeometrieorihranice(vuscDtoFromDb.getGeometrieorihranice());
        if (include != vuscConfig.isNespravneudaje()) vuscDto.setNespravneudaje(vuscDtoFromDb.getNespravneudaje());
        if (include != vuscConfig.isDatumvzniku()) vuscDto.setDatumvzniku(vuscDtoFromDb.getDatumvzniku());
    }
    //endregion
}
