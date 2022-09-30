package no.nav.tiltakspenger.vedtak.rivers

import java.time.LocalDate
import java.time.LocalDateTime

data class YtelseSakDTO(
    val fomGyldighetsperiode: LocalDateTime,
    val tomGyldighetsperiode: LocalDateTime?,
    val datoKravMottatt: LocalDate?,
    val dataKravMottatt: String? = null,
    val fagsystemSakId: Int? = null,
    val status: YtelseSakStatusEnum? = null,
    val ytelsestype: YtelseSakYtelsetypeEnum? = null,
    val vedtak: List<YtelseVedtakDTO> = emptyList(),
    val antallDagerIgjen: Int? = null,
    val antallUkerIgjen: Int? = null
) {

    data class YtelseVedtakDTO(
        val beslutningsDato: LocalDate? = null,
        val periodetypeForYtelse: YtelseVedtakPeriodeTypeForYtelseEnum? = null,
        val vedtaksperiodeFom: LocalDate? = null,
        val vedtaksperiodeTom: LocalDate? = null,
        val vedtaksType: YtelseVedtakVedtakstypeEnum? = null,
        val status: YtelseVedtakStatusEnum? = null,
    )
}


enum class YtelseSakStatusEnum(val navn: String) {
    AKTIV("Aktiv"),
    AVSLU("Lukket"),
    INAKT("Inaktiv")
}

enum class YtelseSakYtelsetypeEnum(val navn: String) {
    AA("Arbeidsavklaringspenger"),
    DAGP("Dagpenger"),
    INDIV("Individstønad"),
    ANNET("Alt annet")
}

enum class YtelseVedtakPeriodeTypeForYtelseEnum(val navn: String) {
    E("Endring"),
    F("Forlenget ventetid"), // Gjelder ikke tiltakspenger
    G("Gjenopptak"),
    N("Annuller sanksjon"), // Gjelder ikke tiltakspenger
    O("Ny rettighet"),
    S("Stans"),
    T("Tidsbegrenset bortfall") // Gjelder ikke tiltakspenger
}

enum class YtelseVedtakVedtakstypeEnum(val navn: String, val ytelseSakYtelsetype: YtelseSakYtelsetypeEnum) {
    AAP("Arbeidsavklaringspenger", YtelseSakYtelsetypeEnum.AA),
    DAGO("Ordinære dagpenger", YtelseSakYtelsetypeEnum.DAGP),
    PERM("Dagpenger under permitteringer", YtelseSakYtelsetypeEnum.DAGP),
    FISK("Dagp. v/perm fra fiskeindustri", YtelseSakYtelsetypeEnum.DAGP),
    LONN("Lønnsgarantimidler - dagpenger", YtelseSakYtelsetypeEnum.DAGP),
    BASI("Tiltakspenger (basisytelse før 2014)", YtelseSakYtelsetypeEnum.INDIV),
    AATFOR("Tvungen forvaltning", YtelseSakYtelsetypeEnum.ANNET),
    AAUNGUFOR("Ung ufør", YtelseSakYtelsetypeEnum.ANNET),
    AA115("§11-5 nedsatt arbeidsevne", YtelseSakYtelsetypeEnum.ANNET),
    AA116("§11-6 behov for bistand", YtelseSakYtelsetypeEnum.ANNET),
    ABOUT("Boutgifter", YtelseSakYtelsetypeEnum.ANNET),
    ADAGR("Daglige reiseutgifter", YtelseSakYtelsetypeEnum.ANNET),
    AFLYT("Flytting", YtelseSakYtelsetypeEnum.ANNET),
    AHJMR("Hjemreise", YtelseSakYtelsetypeEnum.ANNET),
    ANKE("Anke", YtelseSakYtelsetypeEnum.ANNET),
    ARBT("Arbeidstreningplass", YtelseSakYtelsetypeEnum.ANNET),
    ATIF("Tilsyn - familiemedlemmer", YtelseSakYtelsetypeEnum.ANNET),
    ATIO("Tilsyn - barn over 10 år", YtelseSakYtelsetypeEnum.ANNET),
    ATIU("Tilsyn - barn under 10 år", YtelseSakYtelsetypeEnum.ANNET),
    ATTF("§11-6, nødvendig og hensiktsmessig tiltak", YtelseSakYtelsetypeEnum.ANNET),
    ATTK("§11-5, sykdom, skade eller lyte", YtelseSakYtelsetypeEnum.ANNET),
    ATTP("Attføringspenger", YtelseSakYtelsetypeEnum.ANNET),
    AUNDM("Bøker og undervisningsmatriell", YtelseSakYtelsetypeEnum.ANNET),
    BEHOV("Behovsvurdering", YtelseSakYtelsetypeEnum.ANNET),
    BIST14A("Bistandsbehov §14a", YtelseSakYtelsetypeEnum.ANNET),
    BORT("Borteboertillegg", YtelseSakYtelsetypeEnum.ANNET),
    BOUT("Boutgifter", YtelseSakYtelsetypeEnum.ANNET),
    BREI("MOB-Besøksreise", YtelseSakYtelsetypeEnum.ANNET),
    BTIF("Barnetilsyn - familiemedlemmer", YtelseSakYtelsetypeEnum.ANNET),
    BTIL("Barnetillegg", YtelseSakYtelsetypeEnum.ANNET),
    BTIO("Barnetilsyn - barn over 10 år", YtelseSakYtelsetypeEnum.ANNET),
    BTIU("Barnetilsyn - barn under 10 år", YtelseSakYtelsetypeEnum.ANNET),
    DAGR("Daglige reiseutgifter", YtelseSakYtelsetypeEnum.ANNET),
    DEKS("Eksport - dagpenger", YtelseSakYtelsetypeEnum.ANNET),
    DIMP("Import (E303 inn)", YtelseSakYtelsetypeEnum.ANNET),
    EKSG("Eksamensgebyr", YtelseSakYtelsetypeEnum.ANNET),
    FADD("Fadder", YtelseSakYtelsetypeEnum.ANNET),
    FLYT("Flytting", YtelseSakYtelsetypeEnum.ANNET),
    FREI("MOB-Fremreise", YtelseSakYtelsetypeEnum.ANNET),
    FRI_MK_AAP("Fritak fra å sende meldekort AAP", YtelseSakYtelsetypeEnum.ANNET),
    FRI_MK_IND("Fritak fra å sende meldekort individstønad", YtelseSakYtelsetypeEnum.ANNET),
    FSTO("MOB-Flyttestønad", YtelseSakYtelsetypeEnum.ANNET),
    HJMR("Hjemreise", YtelseSakYtelsetypeEnum.ANNET),
    HREI("MOB-Hjemreise", YtelseSakYtelsetypeEnum.ANNET),
    HUSH("Husholdsutgifter", YtelseSakYtelsetypeEnum.ANNET),
    IDAG("Reisetillegg", YtelseSakYtelsetypeEnum.ANNET),
    IEKS("Eksamensgebyr", YtelseSakYtelsetypeEnum.ANNET),
    IFLY("MOB-Flyttehjelp", YtelseSakYtelsetypeEnum.ANNET),
    INDIVFADD("Individstønad fadder", YtelseSakYtelsetypeEnum.ANNET),
    IREI("Hjemreise", YtelseSakYtelsetypeEnum.ANNET),
    ISEM("Semesteravgift", YtelseSakYtelsetypeEnum.ANNET),
    ISKO("Skolepenger", YtelseSakYtelsetypeEnum.ANNET),
    IUND("Bøker og undervisningsmatr.", YtelseSakYtelsetypeEnum.ANNET),
    KLAG1("Klage underinstans", YtelseSakYtelsetypeEnum.ANNET),
    KLAG2("Klage klageinstans", YtelseSakYtelsetypeEnum.ANNET),
    KOMP("Kompensasjon for ekstrautgifter", YtelseSakYtelsetypeEnum.ANNET),
    LREF("Refusjon av legeutgifter", YtelseSakYtelsetypeEnum.ANNET),
    MELD("Meldeplikt attføring", YtelseSakYtelsetypeEnum.ANNET),
    MITR("MOB-Midlertidig transporttilbud", YtelseSakYtelsetypeEnum.ANNET),
    NVURD("Næringsfaglig vurdering", YtelseSakYtelsetypeEnum.ANNET),
    REHAB("Rehabiliteringspenger", YtelseSakYtelsetypeEnum.ANNET),
    RSTO("MOB-Reisestønad", YtelseSakYtelsetypeEnum.ANNET),
    SANK_A("Sanksjon arbeidsgiver", YtelseSakYtelsetypeEnum.ANNET),
    SANK_B("Sanksjon behandler", YtelseSakYtelsetypeEnum.ANNET),
    SANK_S("Sanksjon sykmeldt", YtelseSakYtelsetypeEnum.ANNET),
    SEMA("Semesteravgift", YtelseSakYtelsetypeEnum.ANNET),
    SKOP("Skolepenger", YtelseSakYtelsetypeEnum.ANNET),
    SREI("MOB-Sjømenn", YtelseSakYtelsetypeEnum.ANNET),
    TFOR("Tvungen forvaltning", YtelseSakYtelsetypeEnum.ANNET),
    TILBBET("Tilbakebetaling", YtelseSakYtelsetypeEnum.ANNET),
    TILO("Tilsyn øvrige familiemedlemmer", YtelseSakYtelsetypeEnum.ANNET),
    TILTAK("Tiltaksplass", YtelseSakYtelsetypeEnum.ANNET),
    TILU("Tilsyn barn under 10 år", YtelseSakYtelsetypeEnum.ANNET),
    UFOREYT("Uføreytelser", YtelseSakYtelsetypeEnum.ANNET),
    UNDM("Bøker og undervisningsmatr.", YtelseSakYtelsetypeEnum.ANNET),
    UTESTENG("Utestengning", YtelseSakYtelsetypeEnum.ANNET),
    VENT("Ventestønad", YtelseSakYtelsetypeEnum.ANNET)
}

enum class YtelseVedtakStatusEnum(val navn: String) {
    AVSLU("Avsluttet"),
    GODKJ("Godkjent"),
    INNST("Innstilt"),
    IVERK("Iverksatt"),
    MOTAT("Mottatt"),
    OPPRE("Opprettet"),
    REGIS("Registrert")
}
