package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.KatastralniUzemiDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KatastralniUzemiRepository extends JpaRepository<KatastralniUzemiDto, Integer> {
    boolean existsByKod(Integer kod);
}
