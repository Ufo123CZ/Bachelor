package cca.ruian_puller.download.repository;

import cca.ruian_puller.download.dto.ObecDto;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ObecRepository extends JpaRepository<ObecDto, Integer> {
    boolean existsByKod(Integer kod);
    ObecDto findByKod(Integer kod);

    // Corrected query to use the entity name and field names
    @Query("SELECT o.nazev FROM ObecDto o WHERE o.kod = ?1")
    String findNameByKod(Integer kod);
    @Query("SELECT o.nespravny FROM ObecDto o WHERE o.kod = ?1")
    Boolean findNespravnyByKod(Integer kod);
    @Query("SELECT o.statuskod FROM ObecDto o WHERE o.kod = ?1")
    Integer findStatuskodByKod(Integer kod);
    @Query("SELECT o.okres FROM ObecDto o WHERE o.kod = ?1")
    Integer findOkresByKod(Integer kod);
    @Query("SELECT o.pou FROM ObecDto o WHERE o.kod = ?1")
    Integer findPouByKod(Integer kod);
    @Query("SELECT o.platiod FROM ObecDto o WHERE o.kod = ?1")
    LocalDateTime findPlatiodByKod(Integer kod);
    @Query("SELECT o.platido FROM ObecDto o WHERE o.kod = ?1")
    LocalDateTime findPlatidoByKod(Integer kod);
    @Query("SELECT o.idtransakce FROM ObecDto o WHERE o.kod = ?1")
    Long findIdtransakceByKod(Integer kod);
    @Query("SELECT o.globalniidnavrhuzmeny FROM ObecDto o WHERE o.kod = ?1")
    Long findGlobalniidnavrhuzmenyByKod(Integer kod);
    @Query("SELECT o.mluvnickecharakteristiky FROM ObecDto o WHERE o.kod = ?1")
    String findMluvnickecharakteristikyByKod(Integer kod);
    @Query("SELECT o.vlajkatext FROM ObecDto o WHERE o.kod = ?1")
    String findVlajkatextByKod(Integer kod);
    @Query("SELECT o.vlajkaobrazek FROM ObecDto o WHERE o.kod = ?1")
    byte[] findVlajkaobrazekByKod(Integer kod);
    @Query("SELECT o.znaktext FROM ObecDto o WHERE o.kod = ?1")
    String findZnaktextByKod(Integer kod);
    @Query("SELECT o.znakobrazek FROM ObecDto o WHERE o.kod = ?1")
    byte[] findZnakobrazekByKod(Integer kod);
    @Query("SELECT o.clenenismrozsahkod FROM ObecDto o WHERE o.kod = ?1")
    Integer findClenenismrozsahkodByKod(Integer kod);
    @Query("SELECT o.clenenismtypkod FROM ObecDto o WHERE o.kod = ?1")
    Integer findClenenismtypkodByKod(Integer kod);
    @Query("SELECT o.nutslau FROM ObecDto o WHERE o.kod = ?1")
    String findNutslauByKod(Integer kod);
    @Query("SELECT o.geometriedefbod FROM ObecDto o WHERE o.kod = ?1")
    Geometry findGeometriedefbodByKod(Integer kod);
    @Query("SELECT o.geometriegenhranice FROM ObecDto o WHERE o.kod = ?1")
    Geometry findGeometriegenhraniceByKod(Integer kod);
    @Query("SELECT o.geometrieorihranice FROM ObecDto o WHERE o.kod = ?1")
    Geometry findGeometrieorihraniceByKod(Integer kod);
    @Query("SELECT o.nespravneudaje FROM ObecDto o WHERE o.kod = ?1")
    String findNespravneudajeByKod(Integer kod);
    @Query("SELECT o.datumvzniku FROM ObecDto o WHERE o.kod = ?1")
    LocalDateTime findDatumvznikuByKod(Integer kod);
}
