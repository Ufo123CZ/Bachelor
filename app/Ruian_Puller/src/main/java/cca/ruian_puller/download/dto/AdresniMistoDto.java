package cca.ruian_puller.download.dto;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class AdresniMistoDto {
    private Integer kod;
    private Boolean nespravny;
    private Integer cislodomovni;
    private Integer cisloorientacni;
    private Integer cisloorientacnipismeno;
    private Integer psc;
    private Integer stavebniobjekt;
    private Integer ulice;
    private Integer vokod;
    private LocalDateTime platiod;
    private LocalDateTime platido;
    private Long idtransakce;
    private Long globalniidnavrhuzmeny;
    private String geometrie;
    private String nespravneudaje;
}