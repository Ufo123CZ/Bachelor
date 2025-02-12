CREATE DATABASE ruian;

\c ruian

CREATE EXTENSION postgis;

-- Additions to the database

CREATE TABLE MluvnickeCharakteristiky (
    Kod SERIAL PRIMARY KEY,
    Pad2 VARCHAR(48),
    Pad3 VARCHAR(48),
    Pad4 VARCHAR(48),
    Pad5 VARCHAR(48),
    Pad6 VARCHAR(48),
    Pad7 VARCHAR(48)
);

CREATE TABLE CislaDomovni (
    Kod SERIAL PRIMARY KEY,
    Cislo1 INTEGER,
    Cislo2 INTEGER,
    Cislo3 INTEGER,
    Cislo4 INTEGER
);

CREATE TABLE ZpusobOchrany (
    Kod INTEGER PRIMARY KEY,
    TypOchranyKod INTEGER,
    IdTransakce BIGINT,
    RizeniId BIGINT
);

CREATE TABLE DetailniTEA (
    Kod INTEGER PRIMARY KEY,
    PlatiOd TIMESTAMP,
    Nespravny BOOLEAN,
    GlobalniIdNavrhuZmeny BIGINT,
    DruhKonstrukceKod INTEGER,
    PocetBytu INTEGER,
    PocetPodlazi INTEGER,
    PripojeniKanalizaceKod INTEGER,
    PripojeniPlynKod INTEGER,
    PripojeniVodovodKod INTEGER,
    ZpusobVytapeniKod INTEGER,
    AdresniMistoKod INTEGER 
);

-- Bonitovane dily kolekce

CREATE TABLE BonitovanyDil (
    Vymera BIGINT,
    BonitovanaJednotkaKod INTEGER PRIMARY KEY,
    IdTransakce BIGINT,
    RizeniId BIGINT
);

CREATE TABLE BonitovaneDily (
    Kod SERIAL PRIMARY KEY,
    BonitovanyDil INTEGER REFERENCES BonitovanyDil(BonitovanaJednotkaKod)
);

-- RUIAN tables

CREATE TABLE Stat (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(32) NOT NULL,
    Nespravny BOOLEAN,
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    NutsLau VARCHAR(6),
    Geometrie GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE RegionSoudrznosti (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(32) NOT NULL,
    Nespravny BOOLEAN,
    Stat INTEGER REFERENCES Stat(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    NutsLau VARCHAR(6),
    Geometrie GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE Vusc (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(32) NOT NULL,
    Nespravny BOOLEAN,
    RegionSoudrznosti INTEGER REFERENCES RegionSoudrznosti(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    NutsLau VARCHAR(6),
    Geometrie GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE Okres (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(32) NOT NULL,
    Nespravny BOOLEAN,
    -- Kraj INTEGER REFERENCES Kraj(Kod),
    Vusc INTEGER REFERENCES Vusc(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    NutsLau VARCHAR(6),
    Geometrie GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE Orp (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(48) NOT NULL,
    Nespravny BOOLEAN,
    SpravniObecKod INTEGER,
    Vusc INTEGER REFERENCES Vusc(Kod),
    Okres INTEGER REFERENCES Okres(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    Geometrie GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE Pou (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(48) NOT NULL,
    Nespravny BOOLEAN,
    SpravniObecKod INTEGER,
    Orp INTEGER REFERENCES Orp(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    Geometrie GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE Obec (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(48) NOT NULL,
    Nespravny BOOLEAN,
    StatusKod INTEGER,
    Okres INTEGER REFERENCES Okres(Kod),
    Pou INTEGER REFERENCES Pou(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    MluvnickeCharakteristiky INTEGER REFERENCES MluvnickeCharakteristiky(Kod),
    VlajkaText VARCHAR(4000),
    VlajkaObrazek BYTEA,
    ZnakText VARCHAR(4000),
    ZnakObrazek BYTEA,
    CleneniSMRozsahKod INTEGER,
    CleneniSMTypKod INTEGER,
    NutsLau VARCHAR(6),
    Geometrie GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE CastObce (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(48) NOT NULL,
    Nespravny BOOLEAN,
    Obec INTEGER REFERENCES Obec(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    MluvnickeCharakteristiky INTEGER REFERENCES MluvnickeCharakteristiky(Kod),
    Geometrie GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE Mop (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(32) NOT NULL,
    Nespravny BOOLEAN,
    Obec INTEGER REFERENCES Obec(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    Geometrie GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE SpravniObvod (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(32) NOT NULL,
    Nespravny BOOLEAN,
    SpravniMomcKod INTEGER,
    Obec INTEGER REFERENCES Obec(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    Geometrie GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE Momc (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(48) NOT NULL,
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
    MluvnickeCharakteristiky INTEGER REFERENCES MluvnickeCharakteristiky(Kod),
    ZnakObrazek BYTEA,
    Geometrie GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE KatastralniUzemi (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(48) NOT NULL,
    Nespravny BOOLEAN,
    ExistujeDigitalniMapa BOOLEAN,
    Obec INTEGER REFERENCES Obec(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    RizeniID BIGINT,
    MluvnickeCharakteristiky INTEGER REFERENCES MluvnickeCharakteristiky(Kod),
    Geometrie GEOMETRY,
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
    BonitovaneDily INTEGER REFERENCES BonitovaneDily(Kod),
    ZpusobyOchranyPozemku INTEGER REFERENCES ZpusobOchrany(Kod),
    Geometrie GEOMETRY,
    NespravneUdaje JSONB
);

CREATE TABLE Ulice (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(48) NOT NULL,
    Nespravny BOOLEAN,
    Obec INTEGER REFERENCES Obec(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    Geometrie GEOMETRY,
    NespravneUdaje JSONB
);

CREATE TABLE StavebniObjekt (
    Kod INTEGER PRIMARY KEY,
    Nespravny BOOLEAN,
    CisloDomovni INTEGER REFERENCES CislaDomovni(Kod),
    IdentifikacniParcela BIGINT REFERENCES Parcela(Id),
    TypStavebnihoObjektuKod INTEGER,
    CastObce INTEGER REFERENCES CastObce(Kod),
    Momc INTEGER REFERENCES Momc(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    IsknBudovaId INTEGER,
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
    ZpusobyOchrany INTEGER REFERENCES ZpusobOchrany(Kod),
    DetailniTEA INTEGER REFERENCES DetailniTEA(Kod),
    Geometrie GEOMETRY,
    NespravneUdaje JSONB
);

CREATE TABLE AdresniMisto (
    Kod INTEGER PRIMARY KEY,
    Nespravny BOOLEAN,
    CisloDomovni INTEGER,
    CisloOrientacni INTEGER,
    CisloOrientacniPismeno INTEGER,
    Psc INTEGER,
    StavebniObjekt INTEGER REFERENCES StavebniObjekt(Kod),
    Ulice INTEGER REFERENCES Ulice(Kod),
    VOKod INTEGER,
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    Geometrie GEOMETRY,
    NespravneUdaje JSONB
);

CREATE TABLE Zjs (
    Kod INTEGER PRIMARY KEY,
    Nazev VARCHAR(48) NOT NULL,
    Nespravny BOOLEAN,
    KatastralniUzemi INTEGER REFERENCES KatastralniUzemi(Kod),
    PlatiOd TIMESTAMP,
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    MluvnickeCharakteristiky INTEGER REFERENCES MluvnickeCharakteristiky(Kod),
    Vymera BIGINT,
    CharakterZsjKod INTEGER,
    Geometrie GEOMETRY,
    NespravneUdaje JSONB,
    DatumVzniku TIMESTAMP
);

CREATE TABLE VO (
    PlatiDo TIMESTAMP,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    Geometrie GEOMETRY,
    NespravneUdaje JSONB,
    Kod INTEGER PRIMARY KEY,
    Cislo INTEGER,
    Nespravny BOOLEAN,
    Obec INTEGER REFERENCES Obec(Kod),
    MOMC INTEGER REFERENCES Momc(Kod),
    Poznamka VARCHAR(60)
);

CREATE TABLE NespravneUdaj (
    NespravneUdaj BOOLEAN,
    NazevUdaje VARCHAR(4),
    OznacenoDne TIMESTAMP,
    OznacenoInfo VARCHAR(500)
);

CREATE TABLE ZaniklyPrvek (
    TypPrvkuKod VARCHAR(3),
    PrvekId BIGINT,
    IdTransakce BIGINT
);