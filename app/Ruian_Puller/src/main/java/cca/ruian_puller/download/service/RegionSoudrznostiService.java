package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.RegionSoudrznostiDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cca.ruian_puller.download.repository.RegionSoudrznostiRepository;

@Service
public class RegionSoudrznostiService {
    @Autowired
    private RegionSoudrznostiRepository regionSoudrznostiRepository;

    public void save(RegionSoudrznostiDto regionSoudrznostiDto) {
        regionSoudrznostiRepository.save(regionSoudrznostiDto);
    }
}
