package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.OkresDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OkresRepository extends JpaRepository<OkresDto, Integer> {
    boolean existsByKod(Integer kod);
    OkresDto findByKod(Integer kod);
}
