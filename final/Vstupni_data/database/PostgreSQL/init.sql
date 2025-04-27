CREATE DATABASE ruian;

\c ruian

CREATE EXTENSION postgis;

-- RUIAN tables

CREATE TABLE Stat (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(32),
    Nespravny BOOLEAN,
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    NutsLau VARCHAR(6),
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE RegionSoudrznosti (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(32),
    Nespravny BOOLEAN,
    Stat INTEGER REFERENCES Stat(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    NutsLau VARCHAR(6),
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE Vusc (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(32),
    Nespravny BOOLEAN,
    RegionSoudrznosti INTEGER REFERENCES RegionSoudrznosti(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    NutsLau VARCHAR(6),
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE Okres (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(32),
    Nespravny BOOLEAN,
    Kraj INTEGER,
    Vusc INTEGER REFERENCES Vusc(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    NutsLau VARCHAR(6),
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE Orp (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(48),
    Nespravny BOOLEAN,
    SpravniObecKod INTEGER,
    Vusc INTEGER REFERENCES Vusc(Kod),
    Okres INTEGER REFERENCES Okres(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE Pou (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(48),
    Nespravny BOOLEAN,
    SpravniObecKod INTEGER,
    Orp INTEGER REFERENCES Orp(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE Obec (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(48),
    Nespravny BOOLEAN,
    StatusKod INTEGER,
    Okres INTEGER REFERENCES Okres(Kod),
    Pou INTEGER REFERENCES Pou(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    MluvnickeCharakteristiky JSONB,
    VlajkaText VARCHAR(4000),
    VlajkaObrazek BYTEA,
    ZnakText VARCHAR(4000),
    ZnakObrazek BYTEA,
    CleneniSMRozsahKod INTEGER,
    CleneniSMTypKod INTEGER,
    NutsLau VARCHAR(12),
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE CastObce (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(48),
    Nespravny BOOLEAN,
    Obec INTEGER REFERENCES Obec(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    MluvnickeCharakteristiky JSONB,
    GeometrieDefBod GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE Mop (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(32),
    Nespravny BOOLEAN,
    Obec INTEGER REFERENCES Obec(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    GeometrieDefBod GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE SpravniObvod (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(32),
    Nespravny BOOLEAN,
    SpravniMomcKod INTEGER,
    Obec INTEGER REFERENCES Obec(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    GeometrieDefBod GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE Momc (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(48),
    Nespravny BOOLEAN,
    Mop INTEGER REFERENCES Mop(Kod),
    Obec INTEGER REFERENCES Obec(Kod),
    SpravniObvod INTEGER REFERENCES SpravniObvod(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    VlajkaText VARCHAR(4000),
    VlajkaObrazek BYTEA,
    ZnakText VARCHAR(4000),
    MluvnickeCharakteristiky JSONB,
    ZnakObrazek BYTEA,
    GeometrieDefBod GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE KatastralniUzemi (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(48),
    Nespravny BOOLEAN,
    ExistujeDigitalniMapa BOOLEAN,
    Obec INTEGER REFERENCES Obec(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    RizeniID BIGINT,
    MluvnickeCharakteristiky JSONB,
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE Parcela (
    Id BIGINT PRIMARY KEY,
    Nespravny BOOLEAN,
    KmenoveCislo INTEGER,
    PododdeleniCisla INTEGER,
    VymeraParcely BIGINT,
    ZpusobyVyuzitiPozemku INTEGER,
    DruhCislovaniKod INTEGER,
    DruhPozemkuKod INTEGER,
    KatastralniUzemi INTEGER REFERENCES KatastralniUzemi(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    RizeniID BIGINT,
    BonitovaneDily JSONB,
    ZpusobyOchranyPozemku JSONB,
    GeometrieDefBod GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje JSONB
);

CREATE TABLE Ulice (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(48),
    Nespravny BOOLEAN,
    Obec INTEGER REFERENCES Obec(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    GeometrieDefBod GEOMETRY,
    GeometrieDefCara GEOMETRY,
    NespravneUdaje JSONB
);

CREATE TABLE StavebniObjekt (
    Kod INTEGER PRIMARY KEY,
    Nespravny BOOLEAN,
    CisloDomovni JSONB,
    IdentifikacniParcela BIGINT REFERENCES Parcela(Id),
    TypStavebnihoObjektuKod INTEGER,
    CastObce INTEGER REFERENCES CastObce(Kod),
    Momc INTEGER REFERENCES Momc(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    IsknBudovaId BIGINT,
    Dokonceni TIMESTAMP,
    DruhKonstrukceKod INTEGER,
    ObestavenyProstor INTEGER,
    PocetBytu INTEGER,
    PocetPodlazi INTEGER,
    PodlahovaPlocha INTEGER,
    PripojeniKanalizaceKod INTEGER,
    PripojeniPlynKod INTEGER,
    PripojeniVodovodKod INTEGER,
    VybaveniVytahemKod INTEGER,
    ZastavenaPlocha INTEGER,
    ZpusobVytapeniKod INTEGER,
    ZpusobyOchrany JSONB,
    DetailniTEA JSONB,
    GeometrieDefBod GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje JSONB
);

CREATE TABLE AdresniMisto (
    Kod INTEGER PRIMARY KEY,
    Nespravny BOOLEAN,
    CisloDomovni INTEGER,
    CisloOrientacni INTEGER,
    CisloOrientacniPismeno VARCHAR(1),
    Psc INTEGER,
    StavebniObjekt INTEGER REFERENCES StavebniObjekt(Kod),
    Ulice INTEGER REFERENCES Ulice(Kod),
    VOKod INTEGER,
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    GeometrieDefBod GEOMETRY,
    NespravneUdaje JSONB
);

CREATE TABLE Zsj (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(48),
    Nespravny BOOLEAN,
    KatastralniUzemi INTEGER REFERENCES KatastralniUzemi(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    MluvnickeCharakteristiky JSONB,
    Vymera BIGINT,
    CharakterZsjKod INTEGER,
    GeometrieDefBod GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE VO (
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje JSONB,
    Kod INTEGER PRIMARY KEY,
    Cislo INTEGER,
    Nespravny BOOLEAN,
    Obec INTEGER REFERENCES Obec(Kod),
    MOMC INTEGER REFERENCES Momc(Kod),
    Poznamka VARCHAR(60)
);

CREATE TABLE ZaniklyPrvek (
    TypPrvkuKod VARCHAR(3),
    PrvekId BIGINT PRIMARY KEY,
    IdTransakce BIGINT
);