package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.StavebniObjektDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StavebniObjektRepository extends JpaRepository<StavebniObjektDto, Integer> {
    boolean existsByKod(Integer kod);
    StavebniObjektDto findByKod(Integer kod);
}
