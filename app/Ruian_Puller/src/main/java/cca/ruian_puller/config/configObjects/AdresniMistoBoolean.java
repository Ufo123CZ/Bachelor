package cca.ruian_puller.config.configObjects;

import cca.ruian_puller.config.NodeConst;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AdresniMistoBoolean {
    // Elements from config file
    public static final String NESPRAVNY = "nespravny";
    public static final String CISLODOMOVNI = "cislodomovni";
    public static final String CISLOORIENTACNI = "cisloorientacni";
    public static final String CISLOORIENTACNIPISMENO = "cisloorientacnipismeno";
    public static final String PSC = "psc";
    public static final String STAVEBNI_OBJEKT = "stavebniobjekt";
    public static final String ULICE = "ulice";
    public static final String VOKOD = "vokod";
    public static final String PLATIOD = "platiod";
    public static final String PLATIDO = "platido";
    public static final String IDTRANSAKCE = "idtransakce";
    public static final String GLOBALNIIDNAVRHUZMENY = "globalniidnavrhuzmeny";
    public static final String GEOMETRIEDEFBOD = "geometriedefbod";
    public static final String NESPRAVNEUDAJE = "nespravneudaje";

    // Values of elements
    private boolean nespravny;
    private boolean cislodomovni;
    private boolean cisloorientacni;
    private boolean cisloorientacnipismeno;
    private boolean psc;
    private boolean stavebniobjekt;
    private boolean ulice;
    private boolean vokod;
    private boolean platiod;
    private boolean platido;
    private boolean idtransakce;
    private boolean globalniidnavrhuzmeny;
    private boolean geometriedefbod;
    private boolean nespravneudaje;

    // How to process
    private final String howToProcess;

    /**
     * Constructor for AdresniMistoBoolean.
     *
     * @param howToProcess the processing method
     */
    public AdresniMistoBoolean(String howToProcess) {
        this.howToProcess = howToProcess;

        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            this.nespravny = true;
            this.cislodomovni = true;
            this.cisloorientacni = true;
            this.cisloorientacnipismeno = true;
            this.psc = true;
            this.stavebniobjekt = true;
            this.ulice = true;
            this.vokod = true;
            this.platiod = true;
            this.platido = true;
            this.idtransakce = true;
            this.globalniidnavrhuzmeny = true;
            this.geometriedefbod = true;
            this.nespravneudaje = true;
        }
    }
}