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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ZaniklyPrvekService {

    private static final Logger log = LoggerFactory.getLogger(ZaniklyPrvekService.class);
    private final ZaniklyPrvekRepository zaniklyPrvekRepository;

    @Autowired
    public ZaniklyPrvekService(ZaniklyPrvekRepository zaniklyPrvekRepository) {
        this.zaniklyPrvekRepository = zaniklyPrvekRepository;
    }

    public void prepareAndSave(List<ZaniklyPrvekDto> zaniklyPrvekDtos, AppConfig appConfig) {
        AtomicInteger removedByNullPrvekId = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<ZaniklyPrvekDto> toDelete = new ArrayList<>();
        zaniklyPrvekDtos.forEach(zaniklyPrvekDto -> {
            // Remove all ZaniklyPrvek with null PrvekId
            if (zaniklyPrvekDto.getPrvekid() == null) {
                removedByNullPrvekId.getAndIncrement();
                toDelete.add(zaniklyPrvekDto);
                return;
            }
            // If dto is in db already, select it
            ZaniklyPrvekDto zaniklyPrvekFromDb = zaniklyPrvekRepository.findById(zaniklyPrvekDto.getPrvekid()).orElse(null);
            if (zaniklyPrvekFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(zaniklyPrvekDto, zaniklyPrvekFromDb);
            } else if (appConfig.getZaniklyPrvekConfig() != null && !appConfig.getZaniklyPrvekConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(zaniklyPrvekDto, zaniklyPrvekFromDb, appConfig.getZaniklyPrvekConfig());
            }
            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= zaniklyPrvekDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of ZaniklyPrvekDtos processed");
            }
            if (iterator.get() >= zaniklyPrvekDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of ZaniklyPrvekDtos processed");
            }
            if (iterator.get() >= zaniklyPrvekDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of ZaniklyPrvekDtos processed");
            }
            if (iterator.get() >= zaniklyPrvekDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of ZaniklyPrvekDtos processed");
            }
        });
        // Remove all invalid ZaniklyPrvekDtos
        zaniklyPrvekDtos.removeAll(toDelete);
        // Log if some ZaniklyPrvekDto were removed
        if (removedByNullPrvekId.get() > 0) log.info("Removed {} ZaniklyPrvek with null PrvekId", removedByNullPrvekId.get());

        // Save ZaniklyPrvekDtos to db
        for (int i = 0; i < zaniklyPrvekDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), zaniklyPrvekDtos.size());
            List<ZaniklyPrvekDto> subList = zaniklyPrvekDtos.subList(i, toIndex);
            zaniklyPrvekRepository.saveAll(subList);
            log.info("Saved {} out of {} ZaniklyPrvek", toIndex, zaniklyPrvekDtos.size());
        }
    }

    private void updateWithDbValues(ZaniklyPrvekDto zaniklyPrvekDto, ZaniklyPrvekDto zaniklyPrvekFromDb) {
        if (zaniklyPrvekDto.getTypprvkukod() == null) zaniklyPrvekDto.setTypprvkukod(zaniklyPrvekFromDb.getTypprvkukod());
        if (zaniklyPrvekDto.getIdtransakce() == null) zaniklyPrvekDto.setIdtransakce(zaniklyPrvekFromDb.getIdtransakce());
    }

    //region Prepare with ZaniklyPrvekBoolean
    private void prepare(ZaniklyPrvekDto zaniklyPrvekDto, ZaniklyPrvekDto zaniklyPrvekFromDb, ZaniklyPrvekBoolean zaniklyPrvekConfig) {
        boolean include = zaniklyPrvekConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL);
        if (zaniklyPrvekFromDb == null) {
            setZaniklyPrvekDtoFields(zaniklyPrvekDto, zaniklyPrvekConfig, include);
        } else {
            setZaniklyPrvekDtoFieldsCombinedDB(zaniklyPrvekDto, zaniklyPrvekFromDb, zaniklyPrvekConfig, include);
        }
    }

    private void setZaniklyPrvekDtoFields(ZaniklyPrvekDto zaniklyPrvekDto, ZaniklyPrvekBoolean zaniklyPrvekConfig, boolean include) {
        if (include != zaniklyPrvekConfig.typprvkukod) zaniklyPrvekDto.setTypprvkukod(null);
        if (include != zaniklyPrvekConfig.idtransakce) zaniklyPrvekDto.setIdtransakce(null);
    }

    private void setZaniklyPrvekDtoFieldsCombinedDB(ZaniklyPrvekDto zaniklyPrvekDto, ZaniklyPrvekDto zaniklyPrvekFromDb, ZaniklyPrvekBoolean zaniklyPrvekConfig, boolean include) {
        if (include != zaniklyPrvekConfig.typprvkukod) zaniklyPrvekDto.setTypprvkukod(zaniklyPrvekFromDb.getTypprvkukod());
        if (include != zaniklyPrvekConfig.idtransakce) zaniklyPrvekDto.setIdtransakce(zaniklyPrvekFromDb.getIdtransakce());
    }
    //endregion
}
