package no.nav.tiltakspenger.vedtak

import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet.Tiltaksgruppe.AFT
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet.Tiltaksgruppe.ARBRREHAB
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet.Tiltaksgruppe.AVKLARING
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet.Tiltaksgruppe.FORSOK
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet.Tiltaksgruppe.LONNTILS
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet.Tiltaksgruppe.OPPFOLG
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet.Tiltaksgruppe.OPPL
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet.Tiltaksgruppe.TILRETTE
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet.Tiltaksgruppe.UTFAS
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet.Tiltaksgruppe.VARIGASV
import java.time.LocalDate
import java.time.LocalDateTime

//Dokumentert her: https://confluence.adeo.no/display/ARENA/Arena+-+Tjeneste+Webservice+-+TiltakOgAktivitet_v1#ArenaTjenesteWebserviceTiltakOgAktivitet_v1-HentTiltakOgAktiviteterForBrukerResponse
data class Tiltaksaktivitet(
    val tiltak: Tiltak, // Det vi får her er teksten i Enumen, ikke koden. Det er litt klønete..
    val aktivitetId: String,
    val tiltakLokaltNavn: String?,
    val arrangør: String?,
    val bedriftsnummer: String?,
    val deltakelsePeriode: DeltakelsesPeriode,
    val deltakelseProsent: Float?,
    val deltakerStatus: DeltakerStatus,
    val statusSistEndret: LocalDate?,
    val begrunnelseInnsøking: String,
    val antallDagerPerUke: Float?,
    val tidsstempelHosOss: LocalDateTime,
) : Tidsstempler {

    override fun tidsstempelKilde(): LocalDateTime = statusSistEndret?.atStartOfDay() ?: tidsstempelHosOss
    override fun tidsstempelHosOss(): LocalDateTime = tidsstempelHosOss

    data class DeltakelsesPeriode(
        val fom: LocalDate?,
        val tom: LocalDate?,
    )

    companion object {
        // https://trello.com/c/KVY0kO8n/129-mapping-av-tiltakstype-fra-s%C3%B8knaden
        // Verdiene man kan angi i søknaden har korresponderende kodeverdier i Tiltaksnavn fra Arena
        fun mapTiltaksType(tiltaksType: String): Tiltak? =
            when (tiltaksType) {
                "JOBSOK" -> Tiltak.JOBBK
                "PRAKSORD" -> Tiltak.ARBTREN
                "AMO" -> Tiltak.GRUPPEAMO
                "Annet" -> null //TODO: Hvordan mappe Annet?
                else -> null
            }
    }

    enum class Tiltaksgruppe(val navn: String) {
        AFT("Arbeidsforberedende trening"),
        AMB("Tiltak i arbeidsmarkedsbedrift"),
        ARBPRAKS("Arbeidspraksis"),
        ARBRREHAB("Arbeidsrettet rehabilitering"),
        ARBTREN("Arbeidstrening"),
        AVKLARING("Avklaring"),
        BEHPSSAM("Behandling - lettere psykiske/sammensatte lidelser"),
        ETAB("Egenetablering"),
        FORSOK("Forsøk"),
        LONNTILS("Lønnstilskudd"),
        OPPFOLG("Oppfølging"),
        OPPL("Opplæring"),
        TILRETTE("Tilrettelegging"),
        UTFAS("Tiltak under utfasing"),
        VARIGASV("Varig tilrettelagt arbeid"),
        JOBBSKAP("Jobbskapingsprosjekter"),
        BIO("Bedriftsintern opplæring (BIO)"),
        BISTAND("Arbeid med Bistand (AB)"),
        INST_S("Nye plasser institusjonelle tiltak"),
        MIDSYSS("Midlertidig sysselsetting"),
    }

    enum class Tiltak(val navn: String, val tiltaksgruppe: Tiltaksgruppe, val rettPåTiltakspenger: Boolean) {
        MENTOR("Mentor", OPPFOLG, true),
        MIDLONTIL("Midlertidig lønnstilskudd", LONNTILS, false),
        PV("Produksjonsverksted (PV)", UTFAS, false),
        REFINO("Resultatbasert finansiering av oppfølging", FORSOK, true),
        SUPPEMP("Supported Employment", FORSOK, true),
        ETAB("Egenetablering", Tiltaksgruppe.ETAB, false),
        FORSAMOENK("Forsøk AMO enkeltplass", FORSOK, true),
        FORSAMOGRU("Forsøk AMO gruppe", FORSOK, false),
        FORSFAGENK("Forsøk fag- og yrkesopplæring enkeltplass", FORSOK, true),
        FORSFAGGRU("Forsøk fag- og yrkesopplæring gruppe", FORSOK, false),
        FORSHOYUTD("Forsøk høyere utdanning", FORSOK, true),
        FUNKSJASS("Funksjonsassistanse", TILRETTE, true),
        GRUFAGYRKE("Gruppe Fag- og yrkesopplæring VGS og høyere yrkesfaglig utdanning", OPPL, true),
        GRUPPEAMO("Gruppe AMO", OPPL, true),
        AMO("AMO", OPPL, true),
        HOYEREUTD("Høyere utdanning", OPPL, true),
        INDJOBSTOT("Individuell jobbstøtte (IPS)", OPPFOLG, true),
        INDOPPFAG("Oppfølging", OPPFOLG, true),
        INDOPPRF("Resultatbasert finansiering av formidlingsbistand", FORSOK, true),
        INKLUTILS("Inkluderingstilskudd", TILRETTE, true),
        IPSUNG("Individuell karrierestøtte (IPS Ung)", OPPFOLG, true),
        JOBBK("Jobbklubb", OPPFOLG, true),
        LONNTILAAP("Arbeidsavklaringspenger som lønnstilskudd", FORSOK, false),
        AMBF2("Kvalifisering i arbeidsmarkedsbedrift", UTFAS, false),
        ARBFORB("Arbeidsforberedende trening (AFT)", AFT, true),
        ARBRRHDAG("Arbeidsrettet rehabilitering (dag)", ARBRREHAB, true),
        ARBTREN("Arbeidstrening", Tiltaksgruppe.ARBTREN, true),
        AVKLARAG("Avklaring", AVKLARING, true),
        BIO("Bedriftsintern opplæring (BIO)", Tiltaksgruppe.BIO, false),
        DIGIOPPARB("Digitalt oppfølgingstiltak for arbeidsledige (jobbklubb)", OPPFOLG, true),
        EKSPEBIST("Ekspertbistand", TILRETTE, false),
        ENKELAMO("Enkeltplass AMO", OPPL, true),
        ENKFAGYRKE("Enkeltplass Fag- og yrkesopplæring VGS og høyere yrkesfaglig utdanning", OPPL, true),
        TIDSUBLONN("Tidsubestemt lønnstilskudd", UTFAS, false),
        TILPERBED("Tilretteleggingstilskudd for arbeidssøker", UTFAS, true),
        TILRTILSK("Forebyggings- og tilretteleggingstilskudd IA virksomheter og BHT-honorar", UTFAS, false),
        UTVAOONAV("Utvidet oppfølging i NAV", FORSOK, true),
        UTVOPPFOPL("Utvidet oppfølging i opplæring", OPPFOLG, true),
        VARLONTIL("Varig lønnstilskudd", LONNTILS, false),
        VASV("Varig tilrettelagt arbeid i skjermet virksomhet", VARIGASV, false),
        VATIAROR("Varig tilrettelagt arbeid i ordinær virksomhet", VARIGASV, false),
        VV("Varig vernet arbeid (VVA)", UTFAS, false)
    }

    enum class DeltakerStatus(val tekst: String) {
        AKTUELL("Aktuell"),
        AVSLAG("Fått avslag"),
        DELAVB("Deltakelse avbrutt"),
        FULLF("Fullført"),
        GJENN("Gjennomføres"),
        GJENN_AVB("Gjennomføring avbrutt"),
        GJENN_AVL("Gjennomføring avlyst"),
        IKKAKTUELL("Ikke aktuell"),
        IKKEM("Ikke møtt"),
        INFOMOETE("Informasjonsmøte"),
        JATAKK("Takket ja til tilbud"),
        NEITAKK("Takket nei til tilbud"),
        TILBUD("Godkjent tiltaksplass"),
        VENTELISTE("Venteliste")
    }
}
