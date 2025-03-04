package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.OkresDto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OkresRepository extends JpaRepository<OkresDto, Integer> {
}
