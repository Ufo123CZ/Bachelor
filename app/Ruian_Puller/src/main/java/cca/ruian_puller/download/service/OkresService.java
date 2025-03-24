package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.OkresBoolean;
import cca.ruian_puller.download.dto.OkresDto;
import cca.ruian_puller.download.dto.VuscDto;
import cca.ruian_puller.download.repository.OkresRepository;
import cca.ruian_puller.download.repository.VuscRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class OkresService {

    private final OkresRepository okresRepository;
    private final VuscRepository vuscRepository;

    @Autowired
    public OkresService(OkresRepository okresRepository, VuscRepository vuscRepository) {
        this.okresRepository = okresRepository;
        this.vuscRepository = vuscRepository;
    }

    public void prepareAndSave(List<OkresDto> okresDtos, AppConfig appConfig) {
        // Remove all Okres with null Kod
        int initialSize = okresDtos.size();
        okresDtos.removeIf(okresDto -> okresDto.getKod() == null);
        if (initialSize != okresDtos.size()) {
            log.warn("{} removed from Okres due to null Kod", initialSize - okresDtos.size());
        }

        // Based on OkresBoolean from AppConfig, filter out OkresDto
        if (appConfig.getOkresConfig() != null && !appConfig.getOkresConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL))
            okresDtos.forEach(okresDto -> prepare(okresDto, appConfig.getOkresConfig()));

        // Check all foreign keys
        int initialSize2 = okresDtos.size();
        okresDtos.removeIf(okresDto -> !checkFK(okresDto));
        if (initialSize2 != okresDtos.size()) {
            log.warn("{} removed from Okres due to missing foreign keys", initialSize2 - okresDtos.size());
        }

        // Split list of OkresDto into smaller lists
        for (int i = 0; i < okresDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), okresDtos.size());
            List<OkresDto> subList = okresDtos.subList(i, toIndex);
            okresRepository.saveAll(subList);
            log.info("Saved {} out of {} Okres", toIndex, okresDtos.size());
        }
    }

    private boolean checkFK(OkresDto okresDto) {
        // Get the foreign key Kod
        Integer vuscKod = okresDto.getVusc();

        // Check if the foreign key Kod for Vusc exists
        if (vuscKod != null && !vuscRepository.existsByKod(vuscKod)) {
            log.warn("Okres with Kod {} does not have valid foreign keys: Vusc with Kod {}", okresDto.getKod(), vuscKod);
            return false;
        }

        return true;
    }

    //region Prepare with OkresBoolean
    private void prepare(OkresDto okresDto, OkresBoolean okresConfig) {
        // Check if this dto is in db already
        OkresDto okresDtoFromDb = okresRepository.findByKod(okresDto.getKod());
        boolean include = okresConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (okresDtoFromDb == null) {
            setOkresDtoFields(okresDto, okresConfig, include);
        } else {
            setOkresDtoFieldsCombinedDB(okresDto, okresDtoFromDb, okresConfig, include);
        }
    }

    private void setOkresDtoFields(OkresDto okresDto, OkresBoolean okresConfig, boolean include) {
        if (include != okresConfig.isNazev()) okresDto.setNazev(null);
        if (include != okresConfig.isNespravny()) okresDto.setNespravny(null);
        if (include != okresConfig.isKraj()) okresDto.setKraj(null);
        if (include != okresConfig.isVusc()) okresDto.setVusc(null);
        if (include != okresConfig.isPlatiod()) okresDto.setPlatiod(null);
        if (include != okresConfig.isPlatido()) okresDto.setPlatido(null);
        if (include != okresConfig.isIdtransakce()) okresDto.setIdtransakce(null);
        if (include != okresConfig.isGlobalniidnavrhuzmeny()) okresDto.setGlobalniidnavrhuzmeny(null);
        if (include != okresConfig.isNutslau()) okresDto.setNutslau(null);
        if (include != okresConfig.isGeometriedefbod()) okresDto.setGeometriedefbod(null);
        if (include != okresConfig.isGeometriegenhranice()) okresDto.setGeometriegenhranice(null);
        if (include != okresConfig.isGeometrieorihranice()) okresDto.setGeometrieorihranice(null);
        if (include != okresConfig.isNespravneudaje()) okresDto.setNespravneudaje(null);
        if (include != okresConfig.isDatumvzniku()) okresDto.setDatumvzniku(null);
    }

    private void setOkresDtoFieldsCombinedDB(OkresDto okresDto, OkresDto okresDtoFromDb, OkresBoolean okresConfig, boolean include) {
        if (include != okresConfig.isNazev()) okresDto.setNazev(okresDtoFromDb.getNazev());
        if (include != okresConfig.isNespravny()) okresDto.setNespravny(okresDtoFromDb.getNespravny());
        if (include != okresConfig.isKraj()) okresDto.setKraj(okresDtoFromDb.getKraj());
        if (include != okresConfig.isVusc()) okresDto.setVusc(okresDtoFromDb.getVusc());
        if (include != okresConfig.isPlatiod()) okresDto.setPlatiod(okresDtoFromDb.getPlatiod());
        if (include != okresConfig.isPlatido()) okresDto.setPlatido(okresDtoFromDb.getPlatido());
        if (include != okresConfig.isIdtransakce()) okresDto.setIdtransakce(okresDtoFromDb.getIdtransakce());
        if (include != okresConfig.isGlobalniidnavrhuzmeny()) okresDto.setGlobalniidnavrhuzmeny(okresDtoFromDb.getGlobalniidnavrhuzmeny());
        if (include != okresConfig.isNutslau()) okresDto.setNutslau(okresDtoFromDb.getNutslau());
        if (include != okresConfig.isGeometriedefbod()) okresDto.setGeometriedefbod(okresDtoFromDb.getGeometriedefbod());
        if (include != okresConfig.isGeometriegenhranice()) okresDto.setGeometriegenhranice(okresDtoFromDb.getGeometriegenhranice());
        if (include != okresConfig.isGeometrieorihranice()) okresDto.setGeometrieorihranice(okresDtoFromDb.getGeometrieorihranice());
        if (include != okresConfig.isNespravneudaje()) okresDto.setNespravneudaje(okresDtoFromDb.getNespravneudaje());
        if (include != okresConfig.isDatumvzniku()) okresDto.setDatumvzniku(okresDtoFromDb.getDatumvzniku());
    }
    //endregion
}
