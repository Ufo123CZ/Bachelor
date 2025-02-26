package cca.ruian_puller.download.dto;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class StavebniObjektDto {
    private Integer kod;
    private Boolean nespravny;
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
    private String zpusobyochrany; // JSON
    private String detailnitea; // JSON
    private String geometrie;
    private String nespravneudaje;
}
