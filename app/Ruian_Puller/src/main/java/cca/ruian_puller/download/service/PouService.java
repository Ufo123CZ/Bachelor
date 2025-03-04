package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.PouDto;
import cca.ruian_puller.download.repository.PouRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PouService {
    @Autowired
    private PouRepository pouRepository;

    public void save(PouDto pouDto) {
        pouRepository.save(pouDto);
    }
}
