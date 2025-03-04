package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.PouDto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PouRepository extends JpaRepository<PouDto, Integer> {
}
