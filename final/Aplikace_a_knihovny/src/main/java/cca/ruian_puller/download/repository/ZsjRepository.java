package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.ZsjDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZsjRepository extends JpaRepository<ZsjDto, Integer> {
    boolean existsByKod(Integer kod);
    ZsjDto findByKod(Integer kod);
}
