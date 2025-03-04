package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.OkresDto;
import cca.ruian_puller.download.repository.OkresRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OkresService {
    @Autowired
    private OkresRepository okresRepository;

    public void save(OkresDto okresDto) {
        okresRepository.save(okresDto);
    }
}
