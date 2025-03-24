package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.MopBoolean;
import cca.ruian_puller.download.dto.MomcDto;
import cca.ruian_puller.download.dto.MopDto;
import cca.ruian_puller.download.repository.MopRepository;
import cca.ruian_puller.download.repository.ObecRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        // Remove all Mop with null Kod
        int initialSize = mopDtos.size();
        mopDtos.removeIf(mopDto -> mopDto.getKod() == null);
        if (initialSize != mopDtos.size()) {
            log.warn("{} removed from Mop due to null Kod", initialSize - mopDtos.size());
        }

        // Based on MopBoolean from AppConfig, filter out MopDto
        if (appConfig.getMopConfig() != null && !appConfig.getMopConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL))
            mopDtos.forEach(mopDto -> prepare(mopDto, appConfig.getMopConfig()));

        // Check all foreign keys
        int initialSize2 = mopDtos.size();
        mopDtos.removeIf(mopDto -> !checkFK(mopDto));
        if (initialSize2 != mopDtos.size()) {
            log.warn("{} removed from Mop due to missing foreign keys", initialSize2 - mopDtos.size());
        }

        // Split list of MopDto into smaller lists
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

    //region Prepare with MopBoolean
    private void prepare(MopDto mopDto, MopBoolean mopConfig) {
        // Check if this dto is in db already
        MopDto mopFromDb = mopRepository.findByKod(mopDto.getKod());
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
