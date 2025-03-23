package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MomcRepository extends JpaRepository<MomcDto, Integer> {
    boolean existsByKod(Integer kod);
    MomcDto findByKod(Integer kod);
}

