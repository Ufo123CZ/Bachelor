package cca.ruian_puller.download.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PouDto {
    private Integer kod;
    private String nazev;
    private Boolean nespravny;
    private Integer spravniobeckod;
    private Integer orp;
    private String platiod;
    private String platido;
    private Long idtransakce;
    private Long globalniidnavrhuzmeny;
    private String geometrie;
    private String nespravneudaje;
    private String datumvzniku;
}