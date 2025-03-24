package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.CastObceBoolean;
import cca.ruian_puller.download.dto.CastObceDto;
import cca.ruian_puller.download.repository.CastObceRepository;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.StavebniObjektRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        // Remove all CastObce with null Kod
        int initialSize = castObceDtos.size();
        castObceDtos.removeIf(castObceDto -> castObceDto.getKod() == null);
        if (initialSize != castObceDtos.size()) {
            log.warn("{} removed from CastObce due to null Kod", initialSize - castObceDtos.size());
        }

        // Based on CastObceBoolean from AppConfig, filter out CastObceDto
        if (appConfig.getCastObceConfig() != null && !appConfig.getCastObceConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL))
            castObceDtos.forEach(castObceDto -> prepare(castObceDto, appConfig.getCastObceConfig()));

        // Check all foreign keys
        int initialSize2 = castObceDtos.size();
        castObceDtos.removeIf(castObceDto -> !checkFK(castObceDto));
        if (initialSize2 != castObceDtos.size()) {
            log.warn("{} removed from CastObce due to missing foreign keys", initialSize2 - castObceDtos.size());
        }

        // Split list of CastObceDto into smaller lists
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

    //region Prepare with CastObceBoolean
    private void prepare(CastObceDto castObceDto, CastObceBoolean castObceConfig) {
        // Check if this dto is in db already
        CastObceDto castObceFromDb = castObceRepository.findByKod(castObceDto.getKod());
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
}
