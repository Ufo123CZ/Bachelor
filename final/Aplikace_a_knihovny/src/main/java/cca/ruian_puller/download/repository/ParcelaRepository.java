package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.ParcelaDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParcelaRepository extends JpaRepository<ParcelaDto, Long> {
    boolean existsById(Long Id);
    Optional<ParcelaDto> findById(Long Id);
}
