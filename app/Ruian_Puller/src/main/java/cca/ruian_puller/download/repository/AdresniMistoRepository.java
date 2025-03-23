package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.AdresniMistoDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdresniMistoRepository extends JpaRepository<AdresniMistoDto, Integer> {
    boolean existsByKod(Integer kod);
    AdresniMistoDto findByKod(Integer kod);
}

