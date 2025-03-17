-- RUIAN tables

CREATE TABLE Stat (
    Kod NUMBER PRIMARY KEY,
    Nazev VARCHAR2(32) NOT NULL,
    Nespravny NUMBER(1),
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    NutsLau VARCHAR2(6),
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON,
    DatumVzniku DATE
);

CREATE TABLE RegionSoudrznosti (
    Kod NUMBER PRIMARY KEY,
    Nazev VARCHAR2(32) NOT NULL,
    Nespravny NUMBER(1),
    Stat NUMBER,
    CONSTRAINT fk_rs_stat FOREIGN KEY (Stat) REFERENCES Stat(Kod),
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    NutsLau VARCHAR2(6),
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON,
    DatumVzniku DATE
);

CREATE TABLE Vusc (
    Kod NUMBER PRIMARY KEY,
    Nazev VARCHAR2(32) NOT NULL,
    Nespravny NUMBER(1),
    RegionSoudrznosti NUMBER,
    CONSTRAINT fk_vusc_rs FOREIGN KEY (RegionSoudrznosti) REFERENCES RegionSoudrznosti(Kod),
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    NutsLau VARCHAR2(6),
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON,
    DatumVzniku DATE
);

CREATE TABLE Okres (
    Kod NUMBER PRIMARY KEY,
    Nazev VARCHAR2(32) NOT NULL,
    Nespravny NUMBER(1),
    Kraj NUMBER,
    Vusc NUMBER,
    CONSTRAINT fk_okres_vusc FOREIGN KEY (Vusc) REFERENCES Vusc(Kod),
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    NutsLau VARCHAR2(6),
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON,
    DatumVzniku DATE
);

CREATE TABLE Orp (
    Kod NUMBER PRIMARY KEY,
    Nazev VARCHAR2(48) NOT NULL,
    Nespravny NUMBER(1),
    SpravniObecKod NUMBER,
    Vusc NUMBER,
    Okres NUMBER,
    CONSTRAINT fk_orp_vusc FOREIGN KEY (Vusc) REFERENCES Vusc(Kod),
    CONSTRAINT fk_orp_okres FOREIGN KEY (Okres) REFERENCES Okres(Kod),
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON,
    DatumVzniku DATE
);

CREATE TABLE Pou (
    Kod NUMBER PRIMARY KEY,
    Nazev VARCHAR2(48) NOT NULL,
    Nespravny NUMBER(1),
    SpravniObecKod NUMBER,
    Orp NUMBER,
    CONSTRAINT fk_pou_orp FOREIGN KEY (Orp) REFERENCES Orp(Kod),
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON,
    DatumVzniku DATE
);

CREATE TABLE Obec (
    Kod NUMBER PRIMARY KEY,
    Nazev VARCHAR2(48) NOT NULL,
    Nespravny NUMBER(1),
    StatusKod NUMBER,
    Okres NUMBER,
    Pou NUMBER,
    CONSTRAINT fk_obec_okres FOREIGN KEY (Okres) REFERENCES Okres(Kod),
    CONSTRAINT fk_obec_pou FOREIGN KEY (Pou) REFERENCES Pou(Kod),
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    MluvnickeCharakteristiky JSON,
    VlajkaText VARCHAR2(4000),
    VlajkaObrazek BLOB,
    ZnakText VARCHAR2(4000),
    ZnakObrazek BLOB,
    CleneniSMRozsahKod NUMBER,
    CleneniSMTypKod NUMBER,
    NutsLau VARCHAR2(12),
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON,
    DatumVzniku DATE
);

CREATE TABLE CastObce (
    Kod NUMBER PRIMARY KEY,
    Nazev VARCHAR2(48) NOT NULL,
    Nespravny NUMBER(1),
    Obec NUMBER,
    CONSTRAINT fk_castobce_obec FOREIGN KEY (Obec) REFERENCES Obec(Kod),
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    MluvnickeCharakteristiky JSON,
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON,
    DatumVzniku DATE
);

CREATE TABLE Mop (
    Kod NUMBER PRIMARY KEY,
    Nazev VARCHAR2(32) NOT NULL,
    Nespravny NUMBER(1),
    Obec NUMBER,
    CONSTRAINT fk_mop_obec FOREIGN KEY (Obec) REFERENCES Obec(Kod),
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON,
    DatumVzniku DATE
);

CREATE TABLE SpravniObvod (
    Kod NUMBER PRIMARY KEY,
    Nazev VARCHAR2(32) NOT NULL,
    Nespravny NUMBER(1),
    SpravniMomcKod NUMBER,
    Obec NUMBER,
    CONSTRAINT fk_spravniobvod_obec FOREIGN KEY (Obec) REFERENCES Obec(Kod),
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON,
    DatumVzniku DATE
);

CREATE TABLE Momc (
    Kod NUMBER PRIMARY KEY,
    Nazev VARCHAR2(48) NOT NULL,
    Nespravny NUMBER(1),
    Mop NUMBER,
    Obec NUMBER,
    SpravniObvod NUMBER,
    CONSTRAINT fk_momc_mop FOREIGN KEY (Mop) REFERENCES Mop(Kod),
    CONSTRAINT fk_momc_obec FOREIGN KEY (Obec) REFERENCES Obec(Kod),
    CONSTRAINT fk_momc_spravniobvod FOREIGN KEY (SpravniObvod) REFERENCES SpravniObvod(Kod),
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    VlajkaText VARCHAR2(4000),
    VlajkaObrazek BLOB,
    ZnakText VARCHAR2(4000),
    MluvnickeCharakteristiky JSON,
    ZnakObrazek BLOB,
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON,
    DatumVzniku DATE
);

CREATE TABLE KatastralniUzemi (
    Kod NUMBER PRIMARY KEY,
    Nazev VARCHAR2(48) NOT NULL,
    Nespravny NUMBER(1),
    ExistujeDigitalniMapa NUMBER(1),
    Obec NUMBER,
    CONSTRAINT fk_katastralniuzemi_obec FOREIGN KEY (Obec) REFERENCES Obec(Kod),
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    RizeniID NUMBER(19),
    MluvnickeCharakteristiky JSON,
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON,
    DatumVzniku DATE
);

CREATE TABLE Parcela (
    Id NUMBER(19) PRIMARY KEY,
    Nespravny NUMBER(1),
    KmenoveCislo NUMBER,
    PododdeleniCisla NUMBER,
    VymeraParcely NUMBER(19),
    ZpusobyVyuzitiPozemku NUMBER,
    DruhCislovaniKod NUMBER,
    DruhPozemkuKod NUMBER,
    KatastralniUzemi NUMBER,
    CONSTRAINT fk_parcela_katastralniuzemi FOREIGN KEY (KatastralniUzemi) REFERENCES KatastralniUzemi(Kod),
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    RizeniID NUMBER(19),
    BonitovaneDily JSON,
    ZpusobyOchranyPozemku JSON,
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON
);

CREATE TABLE Ulice (
    Kod NUMBER PRIMARY KEY,
    Nazev VARCHAR2(48) NOT NULL,
    Nespravny NUMBER(1),
    Obec NUMBER,
    CONSTRAINT fk_ulice_obec FOREIGN KEY (Obec) REFERENCES Obec(Kod),
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON
);

CREATE TABLE StavebniObjekt (
    Kod NUMBER PRIMARY KEY,
    Nespravny NUMBER(1),
    CisloDomovni JSON,
    IdentifikacniParcela NUMBER(19),
    CONSTRAINT fk_stavebniobjekt_parcela FOREIGN KEY (IdentifikacniParcela) REFERENCES Parcela(Id),
    TypStavebnihoObjektuKod NUMBER,
    CastObce NUMBER,
    Momc NUMBER,
    CONSTRAINT fk_stavebniobjekt_castobce FOREIGN KEY (CastObce) REFERENCES CastObce(Kod),
    CONSTRAINT fk_stavebniobjekt_momc FOREIGN KEY (Momc) REFERENCES Momc(Kod),
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    IsknBudovaId NUMBER(19),
    Dokonceni DATE,
    DruhKonstrukceKod NUMBER,
    ObestavenyProstor NUMBER,
    PocetBytu NUMBER,
    PocetPodlazi NUMBER,
    PodlahovaPlocha NUMBER,
    PripojeniKanalizaceKod NUMBER,
    PripojeniPlynKod NUMBER,
    PripojeniVodovodKod NUMBER,
    VybaveniVytahemKod NUMBER,
    ZastavenaPlocha NUMBER,
    ZpusobVytapeniKod NUMBER,
    ZpusobyOchrany JSON,
    DetailniTEA JSON,
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON
);

CREATE TABLE AdresniMisto (
    Kod NUMBER PRIMARY KEY,
    Nespravny NUMBER(1),
    CisloDomovni NUMBER,
    CisloOrientacni NUMBER,
    CisloOrientacniPismeno VARCHAR2(1),
    Psc NUMBER,
    StavebniObjekt NUMBER,
    Ulice NUMBER,
    CONSTRAINT fk_adresnimisto_stavebniobjekt FOREIGN KEY (StavebniObjekt) REFERENCES StavebniObjekt(Kod),
    CONSTRAINT fk_adresnimisto_ulice FOREIGN KEY (Ulice) REFERENCES Ulice(Kod),
    VOKod NUMBER,
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON
);

CREATE TABLE Zsj (
    Kod NUMBER PRIMARY KEY,
    Nazev VARCHAR2(48) NOT NULL,
    Nespravny NUMBER(1),
    KatastralniUzemi NUMBER,
    CONSTRAINT fk_zsj_katastralniuzemi FOREIGN KEY (KatastralniUzemi) REFERENCES KatastralniUzemi(Kod),
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    MluvnickeCharakteristiky JSON,
    Vymera NUMBER(19),
    CharakterZsjKod NUMBER,
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON,
    DatumVzniku DATE
);

CREATE TABLE VO (
    PlatiOd DATE,
    PlatiDo DATE,
    IdTransakce NUMBER(19),
    GlobalniIdNavrhuZmeny NUMBER(19),
    GeometrieDefBod SDO_GEOMETRY,
    GeometrieGenHranice SDO_GEOMETRY,
    GeometrieOriHranice SDO_GEOMETRY,
    NespravneUdaje JSON,
    Kod NUMBER PRIMARY KEY,
    Cislo NUMBER,
    Nespravny NUMBER(1),
    Obec NUMBER,
    Momc NUMBER,
    CONSTRAINT fk_vo_obec FOREIGN KEY (Obec) REFERENCES Obec(Kod),
    CONSTRAINT fk_vo_momc FOREIGN KEY (Momc) REFERENCES Momc(Kod),
    Poznamka VARCHAR2(60)
);

CREATE TABLE ZaniklyPrvek (
    TypPrvkuKod VARCHAR2(3),
    PrvekId NUMBER(19) PRIMARY KEY,
    IdTransakce NUMBER(19)
);