package cca.ruian_puller.download.dto;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class ParcelaDto {
    private Long id;
    private Boolean nespravny;
    private Integer kmenovecislo;
    private Integer pododdelenicisla;
    private Long vymeraparcely;
    private Integer zpusobyvyuzitipozemku;
    private Integer druhcislovanikod;
    private Integer druhpozemkukod;
    private Integer katastralniuzemi;
    private LocalDateTime platiod;
    private LocalDateTime platido;
    private Long idtransakce;
    private Long rizeniid;
    private String bonitovanedily; // JSON
    private String zpusobochranypozemku; // JSON
    private String geometrie;
    private String nespravneudaje;
}