package cca.ruian_puller.download.service;

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

    public void prepareAndSave(List<StavebniObjektDto> stavebniObjektDtos, int commitSize) {
        // Check all foreign keys
        int initialSize = stavebniObjektDtos.size();
        stavebniObjektDtos.removeIf(stavebniObjektDto -> !checkFK(stavebniObjektDto));
        if (initialSize != stavebniObjektDtos.size()) {
            log.warn("{} removed from StavebniObjekt due to missing foreign keys", initialSize - stavebniObjektDtos.size());
        }

        // Split list of StavebniObjektDto into smaller lists
        for (int i = 0; i < stavebniObjektDtos.size(); i += commitSize) {
            int toIndex = Math.min(i + commitSize, stavebniObjektDtos.size());
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

        // Check if the foreign keys exist
        if (!parcelaRepository.existsById(parcelaId) && (!castObceRepository.existsByKod(castObceKod) || !momcRepository.existsByKod(momcKod))) {
            log.warn("StavebniObjekt with Kod {} does not have valid foreign keys: Parcela with Id {}, CastObce with Kod {}, Momc with Kod {}", stavebniObjektDto.getKod(), parcelaId, castObceKod, momcKod);
            return false;
        }
        return true;
    }
}
