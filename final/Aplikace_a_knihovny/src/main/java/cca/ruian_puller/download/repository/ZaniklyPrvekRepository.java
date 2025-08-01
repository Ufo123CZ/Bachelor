package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.ZaniklyPrvekDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ZaniklyPrvekRepository extends JpaRepository<ZaniklyPrvekDto, Long> {
    boolean existsById(Long Id);
    Optional<ZaniklyPrvekDto> findById(Long Id);
}
