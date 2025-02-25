package cca.ruian_puller.download.dto;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class StatDto {
    private Integer kod;
    private String nazev;
    private Boolean nespravny;
    private LocalDateTime platiod;
    private LocalDateTime platido;
    private Long idtransakce;
    private Long globalniidnavrhuzmeny;
    private String nutsLau;
    private String geometrie; // WTK (Well-Known Text) format
    private String nespravneudaje; // Will be JSON
    private LocalDateTime datumvzniku;
}