package cca.ruian_puller.download.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "okres")
@ToString
public class OkresDto {
    @Id
    private Integer kod;
    private String nazev;
    private Boolean nespravny;
    private Integer kraj;
    private Integer vusc;
    private LocalDateTime platiod;
    private LocalDateTime platido;
    private Long idtransakce;
    private Long globalniidnavrhuzmeny;
    private String nutslau;
    private String geometrie;   // Geometry
    @JdbcTypeCode(SqlTypes.JSON)
    private String nespravneudaje;  // JSON
    private LocalDateTime datumvzniku;
}
