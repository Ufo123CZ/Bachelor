package cca.ruian_puller.download.service;

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

    public void prepareAndSave(List<ZaniklyPrvekDto> zaniklyPrvekDtos, int commitSize) {
        // Split list of ZaniklyPrvekDto into smaller lists
        for (int i = 0; i < zaniklyPrvekDtos.size(); i += commitSize) {
            int toIndex = Math.min(i + commitSize, zaniklyPrvekDtos.size());
            List<ZaniklyPrvekDto> subList = zaniklyPrvekDtos.subList(i, toIndex);
            zaniklyPrvekRepository.saveAll(subList);
            log.info("Saved {} out of {} ZaniklyPrvek", toIndex, zaniklyPrvekDtos.size());
        }
    }
}
