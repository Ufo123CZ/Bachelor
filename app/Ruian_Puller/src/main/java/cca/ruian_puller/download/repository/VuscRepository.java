package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.VuscDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VuscRepository extends JpaRepository<VuscDto, Integer> {
    boolean existsByKod(Integer kod);
    VuscDto findByKod(Integer kod);
}