package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.StavebniObjektDto;
import cca.ruian_puller.download.repository.CastObceRepository;
import cca.ruian_puller.download.repository.MomcRepository;
import cca.ruian_puller.download.repository.ParcelaRepository;
import cca.ruian_puller.download.repository.StavebniObjektRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void save(StavebniObjektDto stavebniObjektDto) {
        // Get the foreign keys Kod
        Long parcelaId = stavebniObjektDto.getIdentifikacniparcela();
        Integer castObceKod = stavebniObjektDto.getCastobce();
        Integer momcKod = stavebniObjektDto.getMomc();

        // Check if the foreign keys exist
        boolean somethingIsMissing = false;
        if (!parcelaRepository.existsById(parcelaId)) {
            log.warn("Parcela with Id {} does not exist", parcelaId);
            somethingIsMissing = true;
        }
        if (!castObceRepository.existsByKod(castObceKod)) {
            log.warn("CastObce with Kod {} does not exist", castObceKod);
            somethingIsMissing = true;
        }
        if (!momcRepository.existsByKod(momcKod)) {
            log.warn("Momc with Kod {} does not exist", momcKod);
            somethingIsMissing = true;
        }
        if (somethingIsMissing) return;

        // Save only if the Kod exists
        stavebniObjektRepository.save(stavebniObjektDto);
    }
}
