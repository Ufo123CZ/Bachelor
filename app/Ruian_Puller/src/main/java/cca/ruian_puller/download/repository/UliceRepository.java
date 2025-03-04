package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.UliceDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UliceRepository extends JpaRepository<UliceDto, Integer> {
    boolean existsByKod(Integer kod);
}
