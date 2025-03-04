package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.ObecDto;
import cca.ruian_puller.download.repository.ObecRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ObecService {
    @Autowired
    private ObecRepository obecRepository;

    public void save(ObecDto obecDto) {
        obecRepository.save(obecDto);
    }
}
