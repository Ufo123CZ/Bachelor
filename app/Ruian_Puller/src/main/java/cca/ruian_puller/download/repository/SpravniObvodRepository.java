package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.SpravniObvodDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpravniObvodRepository extends JpaRepository<SpravniObvodDto, Integer> {
    boolean existsByKod(Integer kod);
}
