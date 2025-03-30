package cca.ruian_puller.config.configObjects;

import cca.ruian_puller.config.NodeConst;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StavebniObjektBoolean {
    // Elements from config file
    public static final String NESPRAVNY = "nespravny";
    public static final String CISLODOMOVNI = "cislodomovni";
    public static final String IDENTIFIKACNIPARCELA = "identifikacniparcela";
    public static final String TYPSTAVEBNIHOOBJEKTUKOD = "typstavebnihoobjektukod";
    public static final String CASTOBCE = "castobce";
    public static final String MOMC = "momc";
    public static final String PLATIOD = "platiod";
    public static final String PLATIDO = "platido";
    public static final String IDTRANSAKCE = "idtransakce";
    public static final String GLOBALNIIDNAVRHUZMENY = "globalniidnavrhuzmeny";
    public static final String ISKNBUDOVAID = "isknbudovaid";
    public static final String DOKONCENI = "dokonceni";
    public static final String DRUHKONSTRUKCEKOD = "druhkonstrukcekod";
    public static final String OBESTAVENYPROSTOR = "obestavenyprostor";
    public static final String POCETBYTU = "pocetbytu";
    public static final String POCETPODLAZI = "pocetpodlazi";
    public static final String PODLAHOVAPLOCHA = "podlahovaplocha";
    public static final String PRIPPOJENIKANALIZACEKOD = "pripojenikanalizacekod";
    public static final String PRIPPOJENIPLYNKOD = "pripojeniplynkod";
    public static final String PRIPPOJENIVODOVODKOD = "pripojenivodovodkod";
    public static final String VYBAVENIVYTAHEMKOD = "vybavenivytahemkod";
    public static final String ZASTAVENAPLOCHA = "zastavenaplocha";
    public static final String ZPUSOBVYTAPENIKOD = "zpusobvytapenikod";
    public static final String ZPUSOBYOCHRANY = "zpusobyochrany";
    public static final String DETAILNITEA = "detailnitea";
    public static final String GEOMETRIEDEFBOD = "geometriedefbod";
    public static final String GEOMETRIEORIHRANICE = "geometrieorihranice";
    public static final String NESPRAVNEUDAJE = "nespravneudaje";

    // Values of elements
    private boolean nespravny;
    private boolean cislodomovni;
    private boolean identifikacniparcela;
    private boolean typstavebnihoobjektukod;
    private boolean castobce;
    private boolean momc;
    private boolean platiod;
    private boolean platido;
    private boolean idtransakce;
    private boolean globalniidnavrhuzmeny;
    private boolean isknbudovaid;
    private boolean dokonceni;
    private boolean druhkonstrukcekod;
    private boolean obestavenyprostor;
    private boolean pocetbytu;
    private boolean pocetpodlazi;
    private boolean podlahovaplocha;
    private boolean pripojenikanalizacekod;
    private boolean pripojeniplynkod;
    private boolean pripojenivodovodkod;
    private boolean vybavenivytahemkod;
    private boolean zastavenaplocha;
    private boolean zpusobvytapenikod;
    private boolean zpusobyochrany;
    private boolean detailnitea;
    private boolean geometriedefbod;
    private boolean geometrieorihranice;
    private boolean nespravneudaje;

    // How to process
    private final String howToProcess;

    public StavebniObjektBoolean(String howToProcess) {
        this.howToProcess = howToProcess;

        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            this.nespravny = true;
            this.cislodomovni = true;
            this.identifikacniparcela = true;
            this.typstavebnihoobjektukod = true;
            this.castobce = true;
            this.momc = true;
            this.platiod = true;
            this.platido = true;
            this.idtransakce = true;
            this.globalniidnavrhuzmeny = true;
            this.isknbudovaid = true;
            this.dokonceni = true;
            this.druhkonstrukcekod = true;
            this.obestavenyprostor = true;
            this.pocetbytu = true;
            this.pocetpodlazi = true;
            this.podlahovaplocha = true;
            this.pripojenikanalizacekod = true;
            this.pripojeniplynkod = true;
            this.pripojenivodovodkod = true;
            this.vybavenivytahemkod = true;
            this.zastavenaplocha = true;
            this.zpusobvytapenikod = true;
            this.zpusobyochrany = true;
            this.detailnitea = true;
            this.geometriedefbod = true;
            this.geometrieorihranice = true;
            this.nespravneudaje = true;
        }
    }
}
