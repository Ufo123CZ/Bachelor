package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.OrpDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrpRepository extends JpaRepository<OrpDto, Integer> {
    boolean existsByKod(Integer kod);
}
