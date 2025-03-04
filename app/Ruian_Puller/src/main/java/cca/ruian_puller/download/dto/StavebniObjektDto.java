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
@Table(name = "stavebniobjekt")
@ToString
public class StavebniObjektDto {
    @Id
    private Integer kod;
    private Boolean nespravny;
    @JdbcTypeCode(SqlTypes.JSON)
    private String cislodomovni; // JSON
    private Long identifikacniparcela;
    private Integer typstavebnihoobjektukod;
    private Integer castobce;
    private Integer momc;
    private LocalDateTime platiod;
    private LocalDateTime platido;
    private Long idtransakce;
    private Long globalniidnavrhuzmeny;
    private Integer isknbudovaid;
    private LocalDateTime dokonceni;
    private Integer druhkonstrukcekod;
    private Integer obestavenyprostor;
    private Integer pocetbytu;
    private Integer pocetpodlazi;
    private Integer podlahovaplocha;
    private Integer pripojenikanalizacekod;
    private Integer pripojeniplynkod;
    private Integer pripojenivodovodkod;
    private Integer vybavenivytahemkod;
    private Integer zastavenaplocha;
    private Integer zpusobvytapenikod;
    @JdbcTypeCode(SqlTypes.JSON)
    private String zpusobyochrany; // JSON
    @JdbcTypeCode(SqlTypes.JSON)
    private String detailnitea; // JSON
    private String geometrie;   // Geometry
    @JdbcTypeCode(SqlTypes.JSON)
    private String nespravneudaje;  // JSON
}
