package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.PouDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PouRepository extends JpaRepository<PouDto, Integer> {
    boolean existsByKod(Integer kod);
}
