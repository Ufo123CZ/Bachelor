package cca.ruian_puller.download.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cca.ruian_puller.download.dto.StatDto;
import cca.ruian_puller.download.repository.StatRepository;

@Service
public class StatService {
    @Autowired
    private StatRepository statRepository;

    public void save(StatDto statDto) {
        statRepository.save(statDto);
    }
}
