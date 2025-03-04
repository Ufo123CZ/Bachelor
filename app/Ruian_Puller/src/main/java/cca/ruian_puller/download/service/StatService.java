package cca.ruian_puller.download.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cca.ruian_puller.download.dto.StatDto;
import cca.ruian_puller.download.repository.StatRepository;

@Service
public class StatService {

    private final StatRepository statRepository;

    @Autowired
    public StatService(StatRepository statRepository) {
        this.statRepository = statRepository;
    }

    public void save(StatDto statDto) {
        statRepository.save(statDto);
    }
}
