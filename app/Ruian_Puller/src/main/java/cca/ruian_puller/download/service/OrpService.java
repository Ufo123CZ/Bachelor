package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.ObecBoolean;
import cca.ruian_puller.config.configObjects.OrpBoolean;
import cca.ruian_puller.download.dto.ObecDto;
import cca.ruian_puller.download.dto.OrpDto;
import cca.ruian_puller.download.repository.OkresRepository;
import cca.ruian_puller.download.repository.OrpRepository;
import cca.ruian_puller.download.repository.VuscRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class OrpService {

    private final OrpRepository orpRepository;
    private final VuscRepository vuscRepository;
    private final OkresRepository okresRepository;

    @Autowired
    public OrpService(OrpRepository orpRepository, VuscRepository vuscRepository, OkresRepository okresRepository) {
        this.orpRepository = orpRepository;
        this.vuscRepository = vuscRepository;
        this.okresRepository = okresRepository;
    }

    public void prepareAndSave(List<OrpDto> orpDtos, AppConfig appConfig) {
        // Remove all Orp with null Kod
        int initialSize = orpDtos.size();
        orpDtos.removeIf(orpDto -> orpDto.getKod() == null);
        if (initialSize != orpDtos.size()) {
            log.warn("{} removed from Orp due to null Kod", initialSize - orpDtos.size());
        }

        // Based on OrpBoolean from AppConfig, filter out OrpDto
        if (!appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL))
            orpDtos.forEach(orpDto -> prepare(orpDto, appConfig.getOrpConfig()));

        // Check all foreign keys
        int initialSize2 = orpDtos.size();
        orpDtos.removeIf(orpDto -> !checkBF(orpDto));
        if (initialSize2 != orpDtos.size()) {
            log.warn("{} removed from Orp due to missing foreign keys", initialSize2 - orpDtos.size());
        }

        // Split list of OrpDto into smaller lists
        for (int i = 0; i < orpDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), orpDtos.size());
            List<OrpDto> subList = orpDtos.subList(i, toIndex);
            orpRepository.saveAll(subList);
            log.info("Saved {} out of {} Orp", toIndex, orpDtos.size());
        }
    }

    private boolean checkBF(OrpDto orpDto) {
        // Get the foreign keys Kod
        Integer vuscKod = orpDto.getVusc();
        Integer okresKod = orpDto.getOkres();

        // Check if the foreign key Kod for Vusc exists
        if (vuscKod != null && !vuscRepository.existsByKod(vuscKod)) {
            log.warn("Orp with Kod {} does not have a valid foreign key: Vusc with Kod {}", orpDto.getKod(), vuscKod);
            return false;
        }

        // Check if the foreign key Kod for Okres exists
        if (okresKod != null && !okresRepository.existsByKod(okresKod)) {
            log.warn("Orp with Kod {} does not have a valid foreign key: Okres with Kod {}", orpDto.getKod(), okresKod);
            return false;
        }

        return true;
    }

    //region Prepare with OrpBoolean
    private void prepare(OrpDto orpDto, OrpBoolean orpConfig) {
        // Check if this dto is in db already
        OrpDto orpDtoFromDb = orpRepository.findByKod(orpDto.getKod());
        boolean include = orpConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (orpDtoFromDb == null) {
            setOrpDtoFields(orpDto, orpConfig, include);
        } else {
            setOrpDtoFieldsCombinedDB(orpDto, orpDtoFromDb, orpConfig, include);
        }
    }

    private void setOrpDtoFields(OrpDto orpDto, OrpBoolean orpConfig, boolean include) {
        if (include != orpConfig.isNazev()) orpDto.setNazev(null);
        if (include != orpConfig.isNespravny()) orpDto.setNespravny(null);
        if (include != orpConfig.isSpravniobeckod()) orpDto.setSpravniobeckod(null);
        if (include != orpConfig.isVusc()) orpDto.setVusc(null);
        if (include != orpConfig.isOkres()) orpDto.setOkres(null);
        if (include != orpConfig.isPlatiod()) orpDto.setPlatiod(null);
        if (include != orpConfig.isPlatido()) orpDto.setPlatido(null);
        if (include != orpConfig.isIdtransakce()) orpDto.setIdtransakce(null);
        if (include != orpConfig.isGlobalniidnavrhuzmeny()) orpDto.setGlobalniidnavrhuzmeny(null);
        if (include != orpConfig.isGeometriedefbod()) orpDto.setGeometriedefbod(null);
        if (include != orpConfig.isGeometriegenhranice()) orpDto.setGeometriegenhranice(null);
        if (include != orpConfig.isGeometrieorihranice()) orpDto.setGeometrieorihranice(null);
        if (include != orpConfig.isNespravneudaje()) orpDto.setNespravneudaje(null);
        if (include != orpConfig.isDatumvzniku()) orpDto.setDatumvzniku(null);
    }

    private void setOrpDtoFieldsCombinedDB(OrpDto orpDto, OrpDto orpDtoFromDb, OrpBoolean orpConfig, boolean include) {
        if (orpDtoFromDb.getNazev() != null && (include == orpConfig.isNazev()))
            orpDto.setNazev(orpDtoFromDb.getNazev());
        if (orpDtoFromDb.getNespravny() != null && (include == orpConfig.isNespravny()))
            orpDto.setNespravny(orpDtoFromDb.getNespravny());
        if (orpDtoFromDb.getSpravniobeckod() != null && (include == orpConfig.isSpravniobeckod()))
            orpDto.setSpravniobeckod(orpDtoFromDb.getSpravniobeckod());
        if (orpDtoFromDb.getVusc() != null && (include == orpConfig.isVusc()))
            orpDto.setVusc(orpDtoFromDb.getVusc());
        if (orpDtoFromDb.getOkres() != null && (include == orpConfig.isOkres()))
            orpDto.setOkres(orpDtoFromDb.getOkres());
        if (orpDtoFromDb.getPlatiod() != null && (include == orpConfig.isPlatiod()))
            orpDto.setPlatiod(orpDtoFromDb.getPlatiod());
        if (orpDtoFromDb.getPlatido() != null && (include == orpConfig.isPlatido()))
            orpDto.setPlatido(orpDtoFromDb.getPlatido());
        if (orpDtoFromDb.getIdtransakce() != null && (include == orpConfig.isIdtransakce()))
            orpDto.setIdtransakce(orpDtoFromDb.getIdtransakce());
        if (orpDtoFromDb.getGlobalniidnavrhuzmeny() != null && (include == orpConfig.isGlobalniidnavrhuzmeny()))
            orpDto.setGlobalniidnavrhuzmeny(orpDtoFromDb.getGlobalniidnavrhuzmeny());
        if (orpDtoFromDb.getGeometriedefbod() != null && (include == orpConfig.isGeometriedefbod()))
            orpDto.setGeometriedefbod(orpDtoFromDb.getGeometriedefbod());
        if (orpDtoFromDb.getGeometriegenhranice() != null && (include == orpConfig.isGeometriegenhranice()))
            orpDto.setGeometriegenhranice(orpDtoFromDb.getGeometriegenhranice());
        if (orpDtoFromDb.getGeometrieorihranice() != null && (include == orpConfig.isGeometrieorihranice()))
            orpDto.setGeometrieorihranice(orpDtoFromDb.getGeometrieorihranice());
        if (orpDtoFromDb.getNespravneudaje() != null && (include == orpConfig.isNespravneudaje()))
            orpDto.setNespravneudaje(orpDtoFromDb.getNespravneudaje());
        if (orpDtoFromDb.getDatumvzniku() != null && (include == orpConfig.isDatumvzniku()))
            orpDto.setDatumvzniku(orpDtoFromDb.getDatumvzniku());
    }
    //endregion
}
