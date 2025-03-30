package cca.ruian_puller.config.configObjects;

import cca.ruian_puller.config.NodeConst;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VuscBoolean {
    // Elements from config file
    public static final String NAZEV = "nazev";
    public static final String NESPRAVNY = "nespravny";
    public static final String REGIONSOUDRZNOSTI = "regionsoudrznosti";
    public static final String PLATIOD = "platiod";
    public static final String PLATIDO = "platido";
    public static final String IDTRANSAKCE = "idtransakce";
    public static final String GLOBALNIIDNAVRHUZMENY = "globalniidnavrhuzmeny";
    public static final String NUTSLAU = "nutslau";
    public static final String GEOMETRIEDEFBOD = "geometriedefbod";
    public static final String GEOMETRIEGENHRANICE = "geometriegenhranice";
    public static final String GEOMETRIEORIHRANICE = "geometrieorihranice";
    public static final String NESPRAVNEUDAJE = "nespravneudaje";
    public static final String DATUMVZNIKU = "datumvzniku";

    // Values of elements
    private boolean nazev;
    private boolean nespravny;
    private boolean regionsoudrznosti;
    private boolean platiod;
    private boolean platido;
    private boolean idtransakce;
    private boolean globalniidnavrhuzmeny;
    private boolean nutslau;
    private boolean geometriedefbod;
    private boolean geometriegenhranice;
    private boolean geometrieorihranice;
    private boolean nespravneudaje;
    private boolean datumvzniku;

    // How to process
    private final String howToProcess;

    public VuscBoolean(String howToProcess) {
        this.howToProcess = howToProcess;

        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            this.nazev = true;
            this.nespravny = true;
            this.regionsoudrznosti = true;
            this.platiod = true;
            this.platido = true;
            this.idtransakce = true;
            this.globalniidnavrhuzmeny = true;
            this.nutslau = true;
            this.geometriedefbod = true;
            this.geometriegenhranice = true;
            this.geometrieorihranice = true;
            this.nespravneudaje = true;
            this.datumvzniku = true;
        }
    }
}