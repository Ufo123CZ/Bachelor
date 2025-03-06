package cca.ruian_puller.download.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cca.ruian_puller.download.dto.StatDto;
import cca.ruian_puller.download.repository.StatRepository;

import java.util.List;

@Service
@Log4j2
public class StatService {

    private final StatRepository statRepository;

    @Autowired
    public StatService(StatRepository statRepository) {
        this.statRepository = statRepository;
    }

    public void prepareAndSave(List<StatDto> statDtos, int comitSize) {
        // Split list of StatDto into smaller lists
        for (int i = 0; i < statDtos.size(); i += comitSize) {
            int toIndex = Math.min(i + comitSize, statDtos.size());
            List<StatDto> subList = statDtos.subList(i, toIndex);
            statRepository.saveAll(subList);
            log.info("Saved {} out of {} Stat", toIndex, statDtos.size());
        }
    }
}
