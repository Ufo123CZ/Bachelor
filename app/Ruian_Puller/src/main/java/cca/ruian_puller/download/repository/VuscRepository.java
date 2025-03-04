package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.VuscDto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VuscRepository extends JpaRepository<VuscDto, Integer> {
}