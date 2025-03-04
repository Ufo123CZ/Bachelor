package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.StatDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatRepository extends JpaRepository<StatDto, Integer> {
    boolean existsByKod(Integer kod);
}
