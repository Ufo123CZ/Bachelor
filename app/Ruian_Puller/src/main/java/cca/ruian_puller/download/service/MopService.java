package cca.ruian_puller.download.service;

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

    public void prepareAndSave(List<MopDto> mopDtos, int commitSize) {
        // Check all foreign keys
        int initialSize = mopDtos.size();
        mopDtos.removeIf(mopDto -> !checkFK(mopDto));
        if (initialSize != mopDtos.size()) {
            log.warn("{} removed from Mop due to missing foreign keys", initialSize - mopDtos.size());
        }

        // Split list of MopDto into smaller lists
        for (int i = 0; i < mopDtos.size(); i += commitSize) {
            int toIndex = Math.min(i + commitSize, mopDtos.size());
            List<MopDto> subList = mopDtos.subList(i, toIndex);
            mopRepository.saveAll(subList);
            log.info("Saved {} out of {} Mop", toIndex, mopDtos.size());
        }
    }

    private boolean checkFK(MopDto mopDto) {
        // Get the foreign key Kod
        Integer obecKod = mopDto.getObec();

        // Check if the foreign key Kod exists
        if (!obecRepository.existsByKod(obecKod)) {
            log.warn("Obec with Kod {} does not exist", obecKod);
            return false;
        }
        return true;
    }
}
