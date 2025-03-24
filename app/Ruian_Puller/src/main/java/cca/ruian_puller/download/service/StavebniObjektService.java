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

import java.util.List;

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
        // Remove all StavebniObjekt with null Kod
        int initialSize = stavebniObjektDtos.size();
        stavebniObjektDtos.removeIf(stavebniObjektDto -> stavebniObjektDto.getKod() == null);
        if (initialSize != stavebniObjektDtos.size()) {
            log.warn("{} removed from StavebniObjekt due to null Kod", initialSize - stavebniObjektDtos.size());
        }

        // Based on StavebniObjektBoolean from AppConfig, filter out StavebniObjektDto
        if (appConfig.getStavebniObjektConfig() != null && !appConfig.getStavebniObjektConfig().getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL))
            stavebniObjektDtos.forEach(stavebniObjektDto -> prepare(stavebniObjektDto, appConfig.getStavebniObjektConfig()));

        // Check all foreign keys
        int initialSize2 = stavebniObjektDtos.size();
        stavebniObjektDtos.removeIf(stavebniObjektDto -> !checkFK(stavebniObjektDto));
        if (initialSize2 != stavebniObjektDtos.size()) {
            log.warn("{} removed from StavebniObjekt due to missing foreign keys", initialSize2 - stavebniObjektDtos.size());
        }

        // Split list of StavebniObjektDto into smaller lists
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

    //region Prepare with StavebniObjektBoolean
    private void prepare(StavebniObjektDto stavebniObjektDto, StavebniObjektBoolean stavebniObjektConfig) {
        // Check if this dto is in db already
        StavebniObjektDto stavebniObjektDtoFromDb = stavebniObjektRepository.findByKod(stavebniObjektDto.getKod());
        boolean include = stavebniObjektConfig.getHowToProcess().equals(NodeConst.HOW_OF_PROCESS_ELEMENT_INCLUDE);
        if (stavebniObjektDtoFromDb == null) {
            setStavebniObjektDtoFields(stavebniObjektDto, stavebniObjektConfig, include);
        } else {
            setStavebniObjektDtoFieldsCombinedDB(stavebniObjektDto, stavebniObjektDtoFromDb, stavebniObjektConfig, include);
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

    private void setStavebniObjektDtoFieldsCombinedDB(StavebniObjektDto stavebniObjektDto, StavebniObjektDto stavebniObjektDtoFromDb, StavebniObjektBoolean stavebniObjektConfig, boolean include) {
        if (include != stavebniObjektConfig.isNespravny()) stavebniObjektDto.setNespravny(stavebniObjektDtoFromDb.getNespravny());
        if (include != stavebniObjektConfig.isCislodomovni()) stavebniObjektDto.setCislodomovni(stavebniObjektDtoFromDb.getCislodomovni());
        if (include != stavebniObjektConfig.isIdentifikacniparcela()) stavebniObjektDto.setIdentifikacniparcela(stavebniObjektDtoFromDb.getIdentifikacniparcela());
        if (include != stavebniObjektConfig.isTypstavebnihoobjektukod()) stavebniObjektDto.setTypstavebnihoobjektukod(stavebniObjektDtoFromDb.getTypstavebnihoobjektukod());
        if (include != stavebniObjektConfig.isCastobce()) stavebniObjektDto.setCastobce(stavebniObjektDtoFromDb.getCastobce());
        if (include != stavebniObjektConfig.isMomc()) stavebniObjektDto.setMomc(stavebniObjektDtoFromDb.getMomc());
        if (include != stavebniObjektConfig.isPlatiod()) stavebniObjektDto.setPlatiod(stavebniObjektDtoFromDb.getPlatiod());
        if (include != stavebniObjektConfig.isPlatido()) stavebniObjektDto.setPlatido(stavebniObjektDtoFromDb.getPlatido());
        if (include != stavebniObjektConfig.isIdtransakce()) stavebniObjektDto.setIdtransakce(stavebniObjektDtoFromDb.getIdtransakce());
        if (include != stavebniObjektConfig.isGlobalniidnavrhuzmeny()) stavebniObjektDto.setGlobalniidnavrhuzmeny(stavebniObjektDtoFromDb.getGlobalniidnavrhuzmeny());
        if (include != stavebniObjektConfig.isIsknbudovaid()) stavebniObjektDto.setIsknbudovaid(stavebniObjektDtoFromDb.getIsknbudovaid());
        if (include != stavebniObjektConfig.isDokonceni()) stavebniObjektDto.setDokonceni(stavebniObjektDtoFromDb.getDokonceni());
        if (include != stavebniObjektConfig.isDruhkonstrukcekod()) stavebniObjektDto.setDruhkonstrukcekod(stavebniObjektDtoFromDb.getDruhkonstrukcekod());
        if (include != stavebniObjektConfig.isObestavenyprostor()) stavebniObjektDto.setObestavenyprostor(stavebniObjektDtoFromDb.getObestavenyprostor());
        if (include != stavebniObjektConfig.isPocetbytu()) stavebniObjektDto.setPocetbytu(stavebniObjektDtoFromDb.getPocetbytu());
        if (include != stavebniObjektConfig.isPocetpodlazi()) stavebniObjektDto.setPocetpodlazi(stavebniObjektDtoFromDb.getPocetpodlazi());
        if (include != stavebniObjektConfig.isPodlahovaplocha()) stavebniObjektDto.setPodlahovaplocha(stavebniObjektDtoFromDb.getPodlahovaplocha());
        if (include != stavebniObjektConfig.isPripojenikanalizacekod()) stavebniObjektDto.setPripojenikanalizacekod(stavebniObjektDtoFromDb.getPripojenikanalizacekod());
        if (include != stavebniObjektConfig.isPripojeniplynkod()) stavebniObjektDto.setPripojeniplynkod(stavebniObjektDtoFromDb.getPripojeniplynkod());
        if (include != stavebniObjektConfig.isPripojenivodovodkod()) stavebniObjektDto.setPripojenivodovodkod(stavebniObjektDtoFromDb.getPripojenivodovodkod());
        if (include != stavebniObjektConfig.isVybavenivytahemkod()) stavebniObjektDto.setVybavenivytahemkod(stavebniObjektDtoFromDb.getVybavenivytahemkod());
        if (include != stavebniObjektConfig.isZastavenaplocha()) stavebniObjektDto.setZastavenaplocha(stavebniObjektDtoFromDb.getZastavenaplocha());
        if (include != stavebniObjektConfig.isZpusobvytapenikod()) stavebniObjektDto.setZpusobvytapenikod(stavebniObjektDtoFromDb.getZpusobvytapenikod());
        if (include != stavebniObjektConfig.isZpusobyochrany()) stavebniObjektDto.setZpusobyochrany(stavebniObjektDtoFromDb.getZpusobyochrany());
        if (include != stavebniObjektConfig.isDetailnitea()) stavebniObjektDto.setDetailnitea(stavebniObjektDtoFromDb.getDetailnitea());
        if (include != stavebniObjektConfig.isGeometriedefbod()) stavebniObjektDto.setGeometriedefbod(stavebniObjektDtoFromDb.getGeometriedefbod());
        if (include != stavebniObjektConfig.isGeometrieorihranice()) stavebniObjektDto.setGeometrieorihranice(stavebniObjektDtoFromDb.getGeometrieorihranice());
        if (include != stavebniObjektConfig.isNespravneudaje()) stavebniObjektDto.setNespravneudaje(stavebniObjektDtoFromDb.getNespravneudaje());
    }
    //endregion
}
