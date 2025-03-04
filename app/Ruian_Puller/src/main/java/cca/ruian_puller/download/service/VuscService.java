package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.VuscDto;
import cca.ruian_puller.download.repository.VuscRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VuscService {
    @Autowired
    private VuscRepository vuscRepository;

    public void save(VuscDto vuscDto) {
        vuscRepository.save(vuscDto);
    }
}
