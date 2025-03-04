package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.StatDto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatRepository extends JpaRepository<StatDto, Integer> {
}
