package cca.ruian_puller.download.service;

import cca.ruian_puller.config.AppConfig;
import cca.ruian_puller.config.NodeConst;
import cca.ruian_puller.config.configObjects.StavebniObjektBoolean;
import cca.ruian_puller.download.dto.StavebniObjektDto;
import cca.ruian_puller.download.repository.CastObceRepository;
import cca.ruian_puller.download.repository.MomcRepository;
import cca.ruian_puller.download.repository.ParcelaRepository;
import cca.ruian_puller.download.repository.StavebniObjektRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class StavebniObjektService {

    private final StavebniObjektRepository stavebniObjektRepository;
    private final ParcelaRepository parcelaRepository;
    private final CastObceRepository castObceRepository;
    private final MomcRepository momcRepository;

    @Autowired
    public StavebniObjektService(StavebniObjektRepository stavebniObjektRepository, ParcelaRepository parcelaRepository, CastObceRepository castObceRepository, MomcRepository momcRepository) {
        this.stavebniObjektRepository = stavebniObjektRepository;
        this.parcelaRepository = parcelaRepository;
        this.castObceRepository = castObceRepository;
        this.momcRepository = momcRepository;
    }

    public void prepareAndSave(List<StavebniObjektDto> stavebniObjektDtos, AppConfig appConfig) {
        AtomicInteger removedByNullKod = new AtomicInteger(0);
        AtomicInteger removedByFK = new AtomicInteger(0);
        AtomicInteger iterator = new AtomicInteger(0);
        AtomicInteger milestone = new AtomicInteger(0);

        List<StavebniObjektDto> toDelete = new ArrayList<>();
        stavebniObjektDtos.forEach(stavebniObjektDto -> {
            // Remove all StavebniObjekt with null Kod
            if (stavebniObjektDto.getKod() == null) {
                removedByNullKod.getAndIncrement();
                toDelete.add(stavebniObjektDto);
                return;
            }
            // Check if the foreign key is valid
            if (!checkFK(stavebniObjektDto)) {
                removedByFK.getAndIncrement();
                toDelete.add(stavebniObjektDto);
                return;
            }
            // If dto is in db already, select it
            StavebniObjektDto stavebniObjektFromDb = stavebniObjektRepository.findByKod(stavebniObjektDto.getKod());
            if (stavebniObjektFromDb != null && appConfig.getHowToProcessTables().equals(NodeConst.HOW_OF_PROCESS_TABLES_ALL)) {
                updateWithDbValues(stavebniObjektDto, stavebniObjektFromDb);
            } else if (appConfig.getStavebniObjektConfig() != null && !appConfig.getStavebniObjektConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
                prepare(stavebniObjektDto, stavebniObjektFromDb, appConfig.getStavebniObjektConfig());
            }
            // Print progress when first cross 25%, 50%, 75% and 100%
            if (iterator.get() >= stavebniObjektDtos.size() * 0.25 && milestone.compareAndSet(0, 1)) {
                log.info("25% of StavebniObjektDtos processed");
            }
            if (iterator.get() >= stavebniObjektDtos.size() * 0.5 && milestone.compareAndSet(1, 2)) {
                log.info("50% of StavebniObjektDtos processed");
            }
            if (iterator.get() >= stavebniObjektDtos.size() * 0.75 && milestone.compareAndSet(2, 3)) {
                log.info("75% of StavebniObjektDtos processed");
            }
            if (iterator.get() >= stavebniObjektDtos.size() && milestone.compareAndSet(3, 4)) {
                log.info("100% of StavebniObjektDtos processed");
            }
        });

        // Remove all invalid StavebniObjektDtos
        stavebniObjektDtos.removeAll(toDelete);

        // Log if some StavebniObjektDto were removed
        if (removedByNullKod.get() > 0) log.warn("Removed {} StavebniObjekt with null Kod", removedByNullKod.get());
        if (removedByFK.get() > 0) log.warn("Removed {} StavebniObjekt with invalid foreign keys", removedByFK.get());

        // Save StavebniObjektDtos to db
        for (int i = 0; i < stavebniObjektDtos.size(); i += appConfig.getCommitSize()) {
            int toIndex = Math.min(i + appConfig.getCommitSize(), stavebniObjektDtos.size());
            List<StavebniObjektDto> subList = stavebniObjektDtos.subList(i, toIndex);
            stavebniObjektRepository.saveAll(subList);
            log.info("Saved {} out of {} StavebniObjekt", toIndex, stavebniObjektDtos.size());
        }
    }

    private boolean checkFK(StavebniObjektDto stavebniObjektDto) {
        // Get the foreign keys Kod
        Long parcelaId = stavebniObjektDto.getIdentifikacniparcela();
        Integer castObceKod = stavebniObjektDto.getCastobce();
        Integer momcKod = stavebniObjektDto.getMomc();

        // ParcelaId is required
        if (parcelaId == null) {
            log.warn("StavebniObjekt with Kod {} does not have a valid ParcelaId", stavebniObjektDto.getKod());
            return false;
        }

        // Check if the foreign key Kod for Parcela exists
        if (!parcelaRepository.existsById(parcelaId)) {
            log.warn("StavebniObjekt with Kod {} does not have a valid foreign key: Parcela with Id {}", stavebniObjektDto.getKod(), parcelaId);
            return false;
        }

        // Check if the foreign key Kod for CastObce exists
        if (castObceKod != null && !castObceRepository.existsByKod(castObceKod)) {
            log.warn("StavebniObjekt with Kod {} does not have a valid foreign key: CastObce with Kod {}", stavebniObjektDto.getKod(), castObceKod);
            return false;
        }

        // Check if the foreign key Kod for Momc exists
        if (momcKod != null && !momcRepository.existsByKod(momcKod)) {
            log.warn("StavebniObjekt with Kod {} does not have a valid foreign key: Momc with Kod {}", stavebniObjektDto.getKod(), momcKod);
            return false;
        }

        return true;
    }

    private void updateWithDbValues(StavebniObjektDto stavebniObjektDto, StavebniObjektDto stavebniObjektFromDb) {
        if (stavebniObjektDto.getNespravny() == null) stavebniObjektDto.setNespravny(stavebniObjektFromDb.getNespravny());
        if (stavebniObjektDto.getCislodomovni() == null) stavebniObjektDto.setCislodomovni(stavebniObjektFromDb.getCislodomovni());
        if (stavebniObjektDto.getIdentifikacniparcela() == null) stavebniObjektDto.setIdentifikacniparcela(stavebniObjektFromDb.getIdentifikacniparcela());
        if (stavebniObjektDto.getTypstavebnihoobjektukod() == null) stavebniObjektDto.setTypstavebnihoobjektukod(stavebniObjektFromDb.getTypstavebnihoobjektukod());
        if (stavebniObjektDto.getCastobce() == null) stavebniObjektDto.setCastobce(stavebniObjektFromDb.getCastobce());
        if (stavebniObjektDto.getMomc() == null) stavebniObjektDto.setMomc(stavebniObjektFromDb.getMomc());
        if (stavebniObjektDto.getPlatiod() == null) stavebniObjektDto.setPlatiod(stavebniObjektFromDb.getPlatiod());
        if (stavebniObjektDto.getPlatido() == null) stavebniObjektDto.setPlatido(stavebniObjektFromDb.getPlatido());
        if (stavebniObjektDto.getIdtransakce() == null) stavebniObjektDto.setIdtransakce(stavebniObjektFromDb.getIdtransakce());
        if (stavebniObjektDto.getGlobalniidnavrhuzmeny() == null) stavebniObjektDto.setGlobalniidnavrhuzmeny(stavebniObjektFromDb.getGlobalniidnavrhuzmeny());
        if (stavebniObjektDto.getIsknbudovaid() == null) stavebniObjektDto.setIsknbudovaid(stavebniObjektFromDb.getIsknbudovaid());
        if (stavebniObjektDto.getDokonceni() == null) stavebniObjektDto.setDokonceni(stavebniObjektFromDb.getDokonceni());
        if (stavebniObjektDto.getDruhkonstrukcekod() == null) stavebniObjektDto.setDruhkonstrukcekod(stavebniObjektFromDb.getDruhkonstrukcekod());
        if (stavebniObjektDto.getObestavenyprostor() == null) stavebniObjektDto.setObestavenyprostor(stavebniObjektFromDb.getObestavenyprostor());
        if (stavebniObjektDto.getPocetbytu() == null) stavebniObjektDto.setPocetbytu(stavebniObjektFromDb.getPocetbytu());
        if (stavebniObjektDto.getPocetpodlazi() == null) stavebniObjektDto.setPocetpodlazi(stavebniObjektFromDb.getPocetpodlazi());
        if (stavebniObjektDto.getPodlahovaplocha() == null) stavebniObjektDto.setPodlahovaplocha(stavebniObjektFromDb.getPodlahovaplocha());
        if (stavebniObjektDto.getPripojenikanalizacekod() == null) stavebniObjektDto.setPripojenikanalizacekod(stavebniObjektFromDb.getPripojenikanalizacekod());
        if (stavebniObjektDto.getPripojeniplynkod() == null) stavebniObjektDto.setPripojeniplynkod(stavebniObjektFromDb.getPripojeniplynkod());
        if (stavebniObjektDto.getPripojenivodovodkod() == null) stavebniObjektDto.setPripojenivodovodkod(stavebniObjektFromDb.getPripojenivodovodkod());
        if (stavebniObjektDto.getVybavenivytahemkod() == null) stavebniObjektDto.setVybavenivytahemkod(stavebniObjektFromDb.getVybavenivytahemkod());
        if (stavebniObjektDto.getZastavenaplocha() == null) stavebniObjektDto.setZastavenaplocha(stavebniObjektFromDb.getZastavenaplocha());
        if (stavebniObjektDto.getZpusobvytapenikod() == null) stavebniObjektDto.setZpusobvytapenikod(stavebniObjektFromDb.getZpusobvytapenikod());
        if (stavebniObjektDto.getZpusobyochrany() == null) stavebniObjektDto.setZpusobyochrany(stavebniObjektFromDb.getZpusobyochrany());
        if (stavebniObjektDto.getDetailnitea() == null) stavebniObjektDto.setDetailnitea(stavebniObjektFromDb.getDetailnitea());
        if (stavebniObjektDto.getGeometriedefbod() == null) stavebniObjektDto.setGeometriedefbod(stavebniObjektFromDb.getGeometriedefbod());
        if (stavebniObjektDto.getGeometrieorihranice() == null) stavebniObjektDto.setGeometrieorihranice(stavebniObjektFromDb.getGeometrieorihranice());
        if (stavebniObjektDto.getNespravneudaje() == null) stavebniObjektDto.setNespravneudaje(stavebniObjektFromDb.getNespravneudaje());
    }

    //region Prepare with StavebniObjektBoolean
    private void prepare(StavebniObjektDto stavebniObjektDto, StavebniObjektDto stavebniObjektFromDb, StavebniObjektBoolean stavebniObjektConfig) {
        boolean include = stavebniObjektConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (stavebniObjektFromDb == null) {
            setStavebniObjektDtoFields(stavebniObjektDto, stavebniObjektConfig, include);
        } else {
            setStavebniObjektDtoFieldsCombinedDB(stavebniObjektDto, stavebniObjektFromDb, stavebniObjektConfig, include);
        }
    }

    private void setStavebniObjektDtoFields(StavebniObjektDto stavebniObjektDto, StavebniObjektBoolean stavebniObjektConfig, boolean include) {
        if (include != stavebniObjektConfig.isNespravny()) stavebniObjektDto.setNespravny(null);
        if (include != stavebniObjektConfig.isCislodomovni()) stavebniObjektDto.setCislodomovni(null);
        if (include != stavebniObjektConfig.isIdentifikacniparcela()) stavebniObjektDto.setIdentifikacniparcela(null);
        if (include != stavebniObjektConfig.isTypstavebnihoobjektukod()) stavebniObjektDto.setTypstavebnihoobjektukod(null);
        if (include != stavebniObjektConfig.isCastobce()) stavebniObjektDto.setCastobce(null);
        if (include != stavebniObjektConfig.isMomc()) stavebniObjektDto.setMomc(null);
        if (include != stavebniObjektConfig.isPlatiod()) stavebniObjektDto.setPlatiod(null);
        if (include != stavebniObjektConfig.isPlatido()) stavebniObjektDto.setPlatido(null);
        if (include != stavebniObjektConfig.isIdtransakce()) stavebniObjektDto.setIdtransakce(null);
        if (include != stavebniObjektConfig.isGlobalniidnavrhuzmeny()) stavebniObjektDto.setGlobalniidnavrhuzmeny(null);
        if (include != stavebniObjektConfig.isIsknbudovaid()) stavebniObjektDto.setIsknbudovaid(null);
        if (include != stavebniObjektConfig.isDokonceni()) stavebniObjektDto.setDokonceni(null);
        if (include != stavebniObjektConfig.isDruhkonstrukcekod()) stavebniObjektDto.setDruhkonstrukcekod(null);
        if (include != stavebniObjektConfig.isObestavenyprostor()) stavebniObjektDto.setObestavenyprostor(null);
        if (include != stavebniObjektConfig.isPocetbytu()) stavebniObjektDto.setPocetbytu(null);
        if (include != stavebniObjektConfig.isPocetpodlazi()) stavebniObjektDto.setPocetpodlazi(null);
        if (include != stavebniObjektConfig.isPodlahovaplocha()) stavebniObjektDto.setPodlahovaplocha(null);
        if (include != stavebniObjektConfig.isPripojenikanalizacekod()) stavebniObjektDto.setPripojenikanalizacekod(null);
        if (include != stavebniObjektConfig.isPripojeniplynkod()) stavebniObjektDto.setPripojeniplynkod(null);
        if (include != stavebniObjektConfig.isPripojenivodovodkod()) stavebniObjektDto.setPripojenivodovodkod(null);
        if (include != stavebniObjektConfig.isVybavenivytahemkod()) stavebniObjektDto.setVybavenivytahemkod(null);
        if (include != stavebniObjektConfig.isZastavenaplocha()) stavebniObjektDto.setZastavenaplocha(null);
        if (include != stavebniObjektConfig.isZpusobvytapenikod()) stavebniObjektDto.setZpusobvytapenikod(null);
        if (include != stavebniObjektConfig.isZpusobyochrany()) stavebniObjektDto.setZpusobyochrany(null);
        if (include != stavebniObjektConfig.isDetailnitea()) stavebniObjektDto.setDetailnitea(null);
        if (include != stavebniObjektConfig.isGeometriedefbod()) stavebniObjektDto.setGeometriedefbod(null);
        if (include != stavebniObjektConfig.isGeometrieorihranice()) stavebniObjektDto.setGeometrieorihranice(null);
        if (include != stavebniObjektConfig.isNespravneudaje()) stavebniObjektDto.setNespravneudaje(null);
    }

    private void setStavebniObjektDtoFieldsCombinedDB(StavebniObjektDto stavebniObjektDto, StavebniObjektDto stavebniObjektFromDb, StavebniObjektBoolean stavebniObjektConfig, boolean include) {
        if (include != stavebniObjektConfig.isNespravny()) stavebniObjektDto.setNespravny(stavebniObjektFromDb.getNespravny());
        if (include != stavebniObjektConfig.isCislodomovni()) stavebniObjektDto.setCislodomovni(stavebniObjektFromDb.getCislodomovni());
        if (include != stavebniObjektConfig.isIdentifikacniparcela()) stavebniObjektDto.setIdentifikacniparcela(stavebniObjektFromDb.getIdentifikacniparcela());
        if (include != stavebniObjektConfig.isTypstavebnihoobjektukod()) stavebniObjektDto.setTypstavebnihoobjektukod(stavebniObjektFromDb.getTypstavebnihoobjektukod());
        if (include != stavebniObjektConfig.isCastobce()) stavebniObjektDto.setCastobce(stavebniObjektFromDb.getCastobce());
        if (include != stavebniObjektConfig.isMomc()) stavebniObjektDto.setMomc(stavebniObjektFromDb.getMomc());
        if (include != stavebniObjektConfig.isPlatiod()) stavebniObjektDto.setPlatiod(stavebniObjektFromDb.getPlatiod());
        if (include != stavebniObjektConfig.isPlatido()) stavebniObjektDto.setPlatido(stavebniObjektFromDb.getPlatido());
        if (include != stavebniObjektConfig.isIdtransakce()) stavebniObjektDto.setIdtransakce(stavebniObjektFromDb.getIdtransakce());
        if (include != stavebniObjektConfig.isGlobalniidnavrhuzmeny()) stavebniObjektDto.setGlobalniidnavrhuzmeny(stavebniObjektFromDb.getGlobalniidnavrhuzmeny());
        if (include != stavebniObjektConfig.isIsknbudovaid()) stavebniObjektDto.setIsknbudovaid(stavebniObjektFromDb.getIsknbudovaid());
        if (include != stavebniObjektConfig.isDokonceni()) stavebniObjektDto.setDokonceni(stavebniObjektFromDb.getDokonceni());
        if (include != stavebniObjektConfig.isDruhkonstrukcekod()) stavebniObjektDto.setDruhkonstrukcekod(stavebniObjektFromDb.getDruhkonstrukcekod());
        if (include != stavebniObjektConfig.isObestavenyprostor()) stavebniObjektDto.setObestavenyprostor(stavebniObjektFromDb.getObestavenyprostor());
        if (include != stavebniObjektConfig.isPocetbytu()) stavebniObjektDto.setPocetbytu(stavebniObjektFromDb.getPocetbytu());
        if (include != stavebniObjektConfig.isPocetpodlazi()) stavebniObjektDto.setPocetpodlazi(stavebniObjektFromDb.getPocetpodlazi());
        if (include != stavebniObjektConfig.isPodlahovaplocha()) stavebniObjektDto.setPodlahovaplocha(stavebniObjektFromDb.getPodlahovaplocha());
        if (include != stavebniObjektConfig.isPripojenikanalizacekod()) stavebniObjektDto.setPripojenikanalizacekod(stavebniObjektFromDb.getPripojenikanalizacekod());
        if (include != stavebniObjektConfig.isPripojeniplynkod()) stavebniObjektDto.setPripojeniplynkod(stavebniObjektFromDb.getPripojeniplynkod());
        if (include != stavebniObjektConfig.isPripojenivodovodkod()) stavebniObjektDto.setPripojenivodovodkod(stavebniObjektFromDb.getPripojenivodovodkod());
        if (include != stavebniObjektConfig.isVybavenivytahemkod()) stavebniObjektDto.setVybavenivytahemkod(stavebniObjektFromDb.getVybavenivytahemkod());
        if (include != stavebniObjektConfig.isZastavenaplocha()) stavebniObjektDto.setZastavenaplocha(stavebniObjektFromDb.getZastavenaplocha());
        if (include != stavebniObjektConfig.isZpusobvytapenikod()) stavebniObjektDto.setZpusobvytapenikod(stavebniObjektFromDb.getZpusobvytapenikod());
        if (include != stavebniObjektConfig.isZpusobyochrany()) stavebniObjektDto.setZpusobyochrany(stavebniObjektFromDb.getZpusobyochrany());
        if (include != stavebniObjektConfig.isDetailnitea()) stavebniObjektDto.setDetailnitea(stavebniObjektFromDb.getDetailnitea());
        if (include != stavebniObjektConfig.isGeometriedefbod()) stavebniObjektDto.setGeometriedefbod(stavebniObjektFromDb.getGeometriedefbod());
        if (include != stavebniObjektConfig.isGeometrieorihranice()) stavebniObjektDto.setGeometrieorihranice(stavebniObjektFromDb.getGeometrieorihranice());
        if (include != stavebniObjektConfig.isNespravneudaje()) stavebniObjektDto.setNespravneudaje(stavebniObjektFromDb.getNespravneudaje());
    }
    //endregion
}
