package cca.ruian_puller.config.configObjects;

import cca.ruian_puller.config.NodeConst;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MomcBoolean {
    // Elements from config file
    public static final String NAZEV = "nazev";
    public static final String NESPRAVNY = "nespravny";
    public static final String MOP = "mop";
    public static final String OBEC = "obec";
    public static final String SPRAVNIOBVOD = "spravniobvod";
    public static final String PLATIOD = "platiod";
    public static final String PLATIDO = "platido";
    public static final String IDTRANSAKCE = "idtransakce";
    public static final String GLOBALNIIDNAVRHUZMENY = "globalniidnavrhuzmeny";
    public static final String VLAJKATEXT = "vlajkatext";
    public static final String VLAJKAOBRAZEK = "vlajkaobrazek";
    public static final String ZNAKTEXT = "znaktext";
    public static final String ZNAKOBRAZEK = "znakobrazek";
    public static final String MLUVNICKECHARAKTERISTIKY = "mluvnickecharakteristiky";
    public static final String GEOMETRIEDEFBOD = "geometriedefbod";
    public static final String GEOMETRIEORIHRANICE = "geometrieorihranice";
    public static final String NESPRAVNEUDAJE = "nespravneudaje";
    public static final String DATUMVZNIKU = "datumvzniku";

    // Values of elements
    private boolean nazev;
    private boolean nespravny;
    private boolean mop;
    private boolean obec;
    private boolean spravniobvod;
    private boolean platiod;
    private boolean platido;
    private boolean idtransakce;
    private boolean globalniidnavrhuzmeny;
    private boolean vlajkatext;
    private boolean vlajkaobrazek;
    private boolean znaktext;
    private boolean znakobrazek;
    private boolean mluvnickecharakteristiky;
    private boolean geometriedefbod;
    private boolean geometrieorihranice;
    private boolean nespravneudaje;
    private boolean datumvzniku;

    // How to process
    private final String howToProcess;

    /**
     * Constructor for MomcBoolean.
     *
     * @param howToProcess the processing method
     */
    public MomcBoolean(String howToProcess) {
        this.howToProcess = howToProcess;

        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            this.nazev = true;
            this.nespravny = true;
            this.mop = true;
            this.obec = true;
            this.spravniobvod = true;
            this.platiod = true;
            this.platido = true;
            this.idtransakce = true;
            this.globalniidnavrhuzmeny = true;
            this.vlajkatext = true;
            this.vlajkaobrazek = true;
            this.znaktext = true;
            this.mluvnickecharakteristiky = true;
            this.znakobrazek = true;
            this.geometriedefbod = true;
            this.geometrieorihranice = true;
            this.nespravneudaje = true;
            this.datumvzniku = true;
        }
    }
}