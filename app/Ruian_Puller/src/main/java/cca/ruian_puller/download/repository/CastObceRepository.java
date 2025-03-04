package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.CastObceDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CastObceRepository extends JpaRepository<CastObceDto, Integer> {
    boolean existsByKod(Integer kod);
}

