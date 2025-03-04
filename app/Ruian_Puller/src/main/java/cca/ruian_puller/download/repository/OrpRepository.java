package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.OrpDto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrpRepository extends JpaRepository<OrpDto, Integer> {
}
