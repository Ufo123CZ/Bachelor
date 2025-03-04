package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.AdresniMistoDto;
import cca.ruian_puller.download.repository.AdresniMistoRepository;
import cca.ruian_puller.download.repository.StavebniObjektRepository;
import cca.ruian_puller.download.repository.UliceRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class AdresniMistoService {

    private final AdresniMistoRepository adresniMistoRepository;
    private final StavebniObjektRepository stavebniObjektRepository;
    private final UliceRepository uliceRepository;

    @Autowired
    public AdresniMistoService(AdresniMistoRepository adresniMistoRepository, StavebniObjektRepository stavebniObjektRepository, UliceRepository uliceRepository) {
        this.adresniMistoRepository = adresniMistoRepository;
        this.stavebniObjektRepository = stavebniObjektRepository;
        this.uliceRepository = uliceRepository;
    }

    public void save(AdresniMistoDto adresniMisto) {
        // Get the foreign keys Kod
        Integer uliceKod = adresniMisto.getUlice();
        Integer stavebniObjektKod = adresniMisto.getStavebniobjekt();

        // Check if the foreign key Kod exists
        boolean somethingIsMissing = false;
        if (!uliceRepository.existsById(uliceKod)) {
            log.warn("Ulice with Kod {} does not exist", uliceKod);
            somethingIsMissing = true;
        }
        if (!stavebniObjektRepository.existsById(stavebniObjektKod)) {
            log.warn("StavebniObjekt with Kod {} does not exist", stavebniObjektKod);
            somethingIsMissing = true;
        }
        if (somethingIsMissing) return;

        // Save only if the Kod exists
        adresniMistoRepository.save(adresniMisto);
    }
}
