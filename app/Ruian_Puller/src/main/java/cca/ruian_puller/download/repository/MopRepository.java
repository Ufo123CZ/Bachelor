package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.MopDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MopRepository extends JpaRepository<MopDto, Integer> {
    boolean existsByKod(Integer kod);
}
