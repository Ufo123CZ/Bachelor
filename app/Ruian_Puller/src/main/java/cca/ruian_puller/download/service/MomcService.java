package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.MomcDto;
import cca.ruian_puller.download.repository.MomcRepository;
import cca.ruian_puller.download.repository.MopRepository;
import cca.ruian_puller.download.repository.ObecRepository;
import cca.ruian_puller.download.repository.SpravniObvodRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class MomcService {

    private final MomcRepository momcRepository;
    private final MopRepository mopRepository;
    private final ObecRepository obecRepository;
    private final SpravniObvodRepository spravniObvodRepository;

    @Autowired
    public MomcService(MomcRepository momcRepository, MopRepository mopRepository, ObecRepository obecRepository, SpravniObvodRepository spravniObvodRepository) {
        this.momcRepository = momcRepository;
        this.mopRepository = mopRepository;
        this.obecRepository = obecRepository;
        this.spravniObvodRepository = spravniObvodRepository;
    }

    public void save(MomcDto momcDto) {
        // Get the foreign keys Kod
        Integer mopKod = momcDto.getMop();
        Integer obecKod = momcDto.getObec();
        Integer spravniObvodKod = momcDto.getSpravniobvod();

        // Check if the foreign key Kod exists
        if (!mopRepository.existsByKod(mopKod) && !obecRepository.existsByKod(obecKod) && !spravniObvodRepository.existsByKod(spravniObvodKod)) {
            log.warn("Mop with Kod {} or Obec with Kod {} or SpravniObvod with Kod {} does not exist", mopKod, obecKod, spravniObvodKod);
            return;
        }

        // Save only if the Kod exists
        momcRepository.save(momcDto);
    }
}
