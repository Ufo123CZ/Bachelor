package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.ZaniklyPrvekBoolean;
import cca.ruian_puller.download.dto.ZaniklyPrvekDto;
import cca.ruian_puller.download.repository.ZaniklyPrvekRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ZaniklyPrvekService {

    private static final Logger log = LoggerFactory.getLogger(ZaniklyPrvekService.class);
    private final ZaniklyPrvekRepository zaniklyPrvekRepository;

    @Autowired
    public ZaniklyPrvekService(ZaniklyPrvekRepository zaniklyPrvekRepository) {
        this.zaniklyPrvekRepository = zaniklyPrvekRepository;
    }

    public void prepareAndSave(List<ZaniklyPrvekDto> zaniklyPrvekDtos, AppConfig appConfig) {
        // Remove all ZaniklyPrvek with null PrvekId
        int initialSize = zaniklyPrvekDtos.size();
        zaniklyPrvekDtos.removeIf(zaniklyPrvekDto -> zaniklyPrvekDto.getPrvekid() == null);
        if (initialSize != zaniklyPrvekDtos.size()) {
            log.warn("{} removed from ZaniklyPrvek due to null PrvekId", initialSize - zaniklyPrvekDtos.size());
        }

        //Based on ZaniklyPrvekBoolean from AppConfig, filter out ZaniklyPrvekDto
        if (!appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL))
            zaniklyPrvekDtos.forEach(zaniklyPrvekDto -> prepare(zaniklyPrvekDto, appConfig.getZaniklyPrvekConfig()));

        // Split list of ZaniklyPrvekDto into smaller lists
        for (int i = 0; i < zaniklyPrvekDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), zaniklyPrvekDtos.size());
            List<ZaniklyPrvekDto> subList = zaniklyPrvekDtos.subList(i, toIndex);
            zaniklyPrvekRepository.saveAll(subList);
            log.info("Saved {} out of {} ZaniklyPrvek", toIndex, zaniklyPrvekDtos.size());
        }
    }

    //region Prepare with ZaniklyPrvekBoolean
    private void prepare(ZaniklyPrvekDto zaniklyPrvekDto, ZaniklyPrvekBoolean zaniklyPrvekConfig) {
        // Check if this dto is in db already
        ZaniklyPrvekDto zaniklyPrvekDtoFromDb = zaniklyPrvekRepository.findById(zaniklyPrvekDto.getPrvekid()).orElse(null);
        boolean include = zaniklyPrvekConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL);
        if (zaniklyPrvekDtoFromDb == null) {
            setZaniklyPrvekDtoFields(zaniklyPrvekDto, zaniklyPrvekConfig, include);
        } else {
            setZaniklyPrvekDtoFieldsCombinedDB(zaniklyPrvekDto, zaniklyPrvekDtoFromDb, zaniklyPrvekConfig, include);
        }
    }

    private void setZaniklyPrvekDtoFields(ZaniklyPrvekDto zaniklyPrvekDto, ZaniklyPrvekBoolean zaniklyPrvekConfig, boolean include) {
        if (include != zaniklyPrvekConfig.typprvkukod) zaniklyPrvekDto.setTypprvkukod(null);
        if (include != zaniklyPrvekConfig.idtransakce) zaniklyPrvekDto.setIdtransakce(null);
    }

    private void setZaniklyPrvekDtoFieldsCombinedDB(ZaniklyPrvekDto zaniklyPrvekDto, ZaniklyPrvekDto zaniklyPrvekDtoFromDb, ZaniklyPrvekBoolean zaniklyPrvekConfig, boolean include) {
        if (zaniklyPrvekDtoFromDb.getTypprvkukod() != null && include == zaniklyPrvekConfig.typprvkukod)
            zaniklyPrvekDto.setTypprvkukod(zaniklyPrvekDtoFromDb.getTypprvkukod());
        if (zaniklyPrvekDtoFromDb.getIdtransakce() != null && include == zaniklyPrvekConfig.idtransakce)
            zaniklyPrvekDto.setIdtransakce(zaniklyPrvekDtoFromDb.getIdtransakce());
    }
    //endregion
}
