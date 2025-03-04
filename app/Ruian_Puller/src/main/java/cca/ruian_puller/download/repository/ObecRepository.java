package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.ObecDto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObecRepository extends JpaRepository<ObecDto, Integer> {
}
