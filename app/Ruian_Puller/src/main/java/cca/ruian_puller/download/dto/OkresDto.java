package cca.ruian_puller.download.dto;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class OkresDto {
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
    private String geometrie;
    private String nespravneudaje;
    private LocalDateTime datumvzniku;
}
