package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.VODto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VORepository extends JpaRepository<VODto, Integer> {
    boolean existsByKod(Integer kod);
    VODto findByKod(Integer kod);
}
