package cca.ruian_puller.download.service;

import cca.ruian_puller.download.dto.ZaniklyPrvekDto;
import cca.ruian_puller.download.repository.ZaniklyPrvekRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ZaniklyPrvekService {

    private final ZaniklyPrvekRepository zaniklyPrvekRepository;

    @Autowired
    public ZaniklyPrvekService(ZaniklyPrvekRepository zaniklyPrvekRepository) {
        this.zaniklyPrvekRepository = zaniklyPrvekRepository;
    }

    public void save(ZaniklyPrvekDto zaniklyPrvekDto) {
        zaniklyPrvekRepository.save(zaniklyPrvekDto);
    }
}
