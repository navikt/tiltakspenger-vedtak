package no.nav.tiltakspenger.vedtak

import java.time.LocalDate
import java.time.LocalDateTime

//Dokumentert her: https://confluence.adeo.no/display/ARENA/Arena+-+Tjeneste+Webservice+-+Ytelseskontrakt_v3#ArenaTjenesteWebserviceYtelseskontrakt_v3-HentYtelseskontraktListeResponse
data class YtelseSak(
    val fomGyldighetsperiode: LocalDateTime,
    val tomGyldighetsperiode: LocalDateTime?,
    val datoKravMottatt: LocalDate?,
    val dataKravMottatt: String? = null,
    val fagsystemSakId: Int? = null,
    val status: YtelseSakStatus? = null,
    val ytelsestype: YtelseSakYtelsetype? = null,
    val vedtak: List<YtelseVedtak> = emptyList(),
    val antallDagerIgjen: Int? = null,
    val antallUkerIgjen: Int? = null,
    val tidsstempelHosOss: LocalDateTime,
) : Tidsstempler {
    override fun tidsstempelKilde(): LocalDateTime = tidsstempelHosOss
    override fun tidsstempelHosOss(): LocalDateTime = tidsstempelHosOss

    data class YtelseVedtak(
        val beslutningsDato: LocalDate? = null,
        val periodetypeForYtelse: YtelseVedtakPeriodeTypeForYtelse? = null,
        val vedtaksperiodeFom: LocalDate? = null,
        val vedtaksperiodeTom: LocalDate? = null,
        val vedtaksType: YtelseVedtakVedtakstype? = null,
        val status: YtelseVedtakStatus? = null,
    ) {
        enum class YtelseVedtakPeriodeTypeForYtelse(val navn: String) {
            E("Endring"),
            F("Forlenget ventetid"), // Gjelder ikke tiltakspenger
            G("Gjenopptak"),
            N("Annuller sanksjon"), // Gjelder ikke tiltakspenger
            O("Ny rettighet"),
            S("Stans"),
            T("Tidsbegrenset bortfall") // Gjelder ikke tiltakspenger
        }

        enum class YtelseVedtakVedtakstype(val navn: String, val ytelseSakYtelsetype: YtelseSakYtelsetype) {
            AAP("Arbeidsavklaringspenger", YtelseSakYtelsetype.AA),
            DAGO("Ordinære dagpenger", YtelseSakYtelsetype.DAGP),
            PERM("Dagpenger under permitteringer", YtelseSakYtelsetype.DAGP),
            FISK("Dagp. v/perm fra fiskeindustri", YtelseSakYtelsetype.DAGP),
            LONN("Lønnsgarantimidler - dagpenger", YtelseSakYtelsetype.DAGP),
            BASI("Tiltakspenger (basisytelse før 2014)", YtelseSakYtelsetype.INDIV),
            AATFOR("Tvungen forvaltning", YtelseSakYtelsetype.ANNET),
            AAUNGUFOR("Ung ufør", YtelseSakYtelsetype.ANNET),
            AA115("§11-5 nedsatt arbeidsevne", YtelseSakYtelsetype.ANNET),
            AA116("§11-6 behov for bistand", YtelseSakYtelsetype.ANNET),
            ABOUT("Boutgifter", YtelseSakYtelsetype.ANNET),
            ADAGR("Daglige reiseutgifter", YtelseSakYtelsetype.ANNET),
            AFLYT("Flytting", YtelseSakYtelsetype.ANNET),
            AHJMR("Hjemreise", YtelseSakYtelsetype.ANNET),
            ANKE("Anke", YtelseSakYtelsetype.ANNET),
            ARBT("Arbeidstreningplass", YtelseSakYtelsetype.ANNET),
            ATIF("Tilsyn - familiemedlemmer", YtelseSakYtelsetype.ANNET),
            ATIO("Tilsyn - barn over 10 år", YtelseSakYtelsetype.ANNET),
            ATIU("Tilsyn - barn under 10 år", YtelseSakYtelsetype.ANNET),
            ATTF("§11-6, nødvendig og hensiktsmessig tiltak", YtelseSakYtelsetype.ANNET),
            ATTK("§11-5, sykdom, skade eller lyte", YtelseSakYtelsetype.ANNET),
            ATTP("Attføringspenger", YtelseSakYtelsetype.ANNET),
            AUNDM("Bøker og undervisningsmatriell", YtelseSakYtelsetype.ANNET),
            BEHOV("Behovsvurdering", YtelseSakYtelsetype.ANNET),
            BIST14A("Bistandsbehov §14a", YtelseSakYtelsetype.ANNET),
            BORT("Borteboertillegg", YtelseSakYtelsetype.ANNET),
            BOUT("Boutgifter", YtelseSakYtelsetype.ANNET),
            BREI("MOB-Besøksreise", YtelseSakYtelsetype.ANNET),
            BTIF("Barnetilsyn - familiemedlemmer", YtelseSakYtelsetype.ANNET),
            BTIL("Barnetillegg", YtelseSakYtelsetype.ANNET),
            BTIO("Barnetilsyn - barn over 10 år", YtelseSakYtelsetype.ANNET),
            BTIU("Barnetilsyn - barn under 10 år", YtelseSakYtelsetype.ANNET),
            DAGR("Daglige reiseutgifter", YtelseSakYtelsetype.ANNET),
            DEKS("Eksport - dagpenger", YtelseSakYtelsetype.ANNET),
            DIMP("Import (E303 inn)", YtelseSakYtelsetype.ANNET),
            EKSG("Eksamensgebyr", YtelseSakYtelsetype.ANNET),
            FADD("Fadder", YtelseSakYtelsetype.ANNET),
            FLYT("Flytting", YtelseSakYtelsetype.ANNET),
            FREI("MOB-Fremreise", YtelseSakYtelsetype.ANNET),
            FRI_MK_AAP("Fritak fra å sende meldekort AAP", YtelseSakYtelsetype.ANNET),
            FRI_MK_IND("Fritak fra å sende meldekort individstønad", YtelseSakYtelsetype.ANNET),
            FSTO("MOB-Flyttestønad", YtelseSakYtelsetype.ANNET),
            HJMR("Hjemreise", YtelseSakYtelsetype.ANNET),
            HREI("MOB-Hjemreise", YtelseSakYtelsetype.ANNET),
            HUSH("Husholdsutgifter", YtelseSakYtelsetype.ANNET),
            IDAG("Reisetillegg", YtelseSakYtelsetype.ANNET),
            IEKS("Eksamensgebyr", YtelseSakYtelsetype.ANNET),
            IFLY("MOB-Flyttehjelp", YtelseSakYtelsetype.ANNET),
            INDIVFADD("Individstønad fadder", YtelseSakYtelsetype.ANNET),
            IREI("Hjemreise", YtelseSakYtelsetype.ANNET),
            ISEM("Semesteravgift", YtelseSakYtelsetype.ANNET),
            ISKO("Skolepenger", YtelseSakYtelsetype.ANNET),
            IUND("Bøker og undervisningsmatr.", YtelseSakYtelsetype.ANNET),
            KLAG1("Klage underinstans", YtelseSakYtelsetype.ANNET),
            KLAG2("Klage klageinstans", YtelseSakYtelsetype.ANNET),
            KOMP("Kompensasjon for ekstrautgifter", YtelseSakYtelsetype.ANNET),
            LREF("Refusjon av legeutgifter", YtelseSakYtelsetype.ANNET),
            MELD("Meldeplikt attføring", YtelseSakYtelsetype.ANNET),
            MITR("MOB-Midlertidig transporttilbud", YtelseSakYtelsetype.ANNET),
            NVURD("Næringsfaglig vurdering", YtelseSakYtelsetype.ANNET),
            REHAB("Rehabiliteringspenger", YtelseSakYtelsetype.ANNET),
            RSTO("MOB-Reisestønad", YtelseSakYtelsetype.ANNET),
            SANK_A("Sanksjon arbeidsgiver", YtelseSakYtelsetype.ANNET),
            SANK_B("Sanksjon behandler", YtelseSakYtelsetype.ANNET),
            SANK_S("Sanksjon sykmeldt", YtelseSakYtelsetype.ANNET),
            SEMA("Semesteravgift", YtelseSakYtelsetype.ANNET),
            SKOP("Skolepenger", YtelseSakYtelsetype.ANNET),
            SREI("MOB-Sjømenn", YtelseSakYtelsetype.ANNET),
            TFOR("Tvungen forvaltning", YtelseSakYtelsetype.ANNET),
            TILBBET("Tilbakebetaling", YtelseSakYtelsetype.ANNET),
            TILO("Tilsyn øvrige familiemedlemmer", YtelseSakYtelsetype.ANNET),
            TILTAK("Tiltaksplass", YtelseSakYtelsetype.ANNET),
            TILU("Tilsyn barn under 10 år", YtelseSakYtelsetype.ANNET),
            UFOREYT("Uføreytelser", YtelseSakYtelsetype.ANNET),
            UNDM("Bøker og undervisningsmatr.", YtelseSakYtelsetype.ANNET),
            UTESTENG("Utestengning", YtelseSakYtelsetype.ANNET),
            VENT("Ventestønad", YtelseSakYtelsetype.ANNET)
        }

        enum class YtelseVedtakStatus(val navn: String) {
            AVSLU("Avsluttet"),
            GODKJ("Godkjent"),
            INNST("Innstilt"),
            IVERK("Iverksatt"),
            MOTAT("Mottatt"),
            OPPRE("Opprettet"),
            REGIS("Registrert")
        }
    }

    enum class YtelseSakStatus(val navn: String) {
        AKTIV("Aktiv"),
        AVSLU("Lukket"),
        INAKT("Inaktiv")
    }

    enum class YtelseSakYtelsetype(val navn: String) {
        AA("Arbeidsavklaringspenger"),
        DAGP("Dagpenger"),
        INDIV("Individstønad"),
        ANNET("Alt annet")
    }

}
