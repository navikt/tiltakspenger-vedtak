package no.nav.tiltakspenger.vedtak.rivers

import java.time.LocalDate

data class TiltaksaktivitetDTO(
    val tiltaksnavn: TiltaksnavnEnum,
    val aktivitetId: String,
    val tiltakLokaltNavn: String?,
    val arrangoer: String?,
    val bedriftsnummer: String?,
    val deltakelsePeriode: DeltakelsesPeriodeDTO?,
    val deltakelseProsent: Float?,
    val deltakerStatus: DeltakerStatusDTO,
    val statusSistEndret: LocalDate?,
    val begrunnelseInnsoeking: String,
    val antallDagerPerUke: Float?,
) {

    data class DeltakelsesPeriodeDTO(
        val fom: LocalDate?,
        val tom: LocalDate?,
    )

    data class DeltakerStatusDTO(
        val statusNavn: String,
        val status: DeltakerStatusEnum
    )

    enum class TiltaksnavnEnum(val tekst: String) {
        MENTOR("Mentor"),
        MIDLONTIL("Midlertidig lønnstilskudd"),
        PV("Produksjonsverksted (PV)"),
        REFINO("Resultatbasert finansiering av oppfølging"),
        SUPPEMP("Supported Employment"),
        ETAB("Egenetablering"),
        FORSAMOENK("Forsøk AMO enkeltplass"),
        FORSAMOGRU("Forsøk AMO gruppe"),
        FORSFAGENK("Forsøk fag- og yrkesopplæring enkeltplass"),
        FORSFAGGRU("Forsøk fag- og yrkesopplæring gruppe"),
        FORSHOYUTD("Forsøk høyere utdanning"),
        FUNKSJASS("Funksjonsassistanse"),
        GRUFAGYRKE("Gruppe Fag- og yrkesopplæring VGS og høyere yrkesfaglig utdanning"),
        GRUPPEAMO("Gruppe AMO"),
        HOYEREUTD("Høyere utdanning"),
        INDJOBSTOT("Individuell jobbstøtte (IPS)"),
        INDOPPFAG("Oppfølging"),
        INDOPPRF("Resultatbasert finansiering av formidlingsbistand"),
        INKLUTILS("Inkluderingstilskudd"),
        IPSUNG("Individuell karrierestøtte (IPS Ung)"),
        JOBBK("Jobbklubb"),
        LONNTILAAP("Arbeidsavklaringspenger som lønnstilskudd"),
        AMBF2("Kvalifisering i arbeidsmarkedsbedrift"),
        ARBFORB("Arbeidsforberedende trening (AFT)"),
        ARBRRHDAG("Arbeidsrettet rehabilitering (dag)"),
        ARBTREN("Arbeidstrening"),
        AVKLARAG("Avklaring"),
        BIO("Bedriftsintern opplæring (BIO)"),
        DIGIOPPARB("Digitalt oppfølgingstiltak for arbeidsledige (jobbklubb)"),
        EKSPEBIST("Ekspertbistand"),
        ENKELAMO("Enkeltplass AMO"),
        ENKFAGYRKE("Enkeltplass Fag- og yrkesopplæring VGS og høyere yrkesfaglig utdanning"),
        TIDSUBLONN("Tidsubestemt lønnstilskudd"),
        TILPERBED("Tilretteleggingstilskudd for arbeidssøker"),
        TILRTILSK("Forebyggings- og tilretteleggingstilskudd IA virksomheter og BHT-honorar"),
        UTVAOONAV("Utvidet oppfølging i NAV"),
        UTVOPPFOPL("Utvidet oppfølging i opplæring"),
        VARLONTIL("Varig lønnstilskudd"),
        VASV("Varig tilrettelagt arbeid i skjermet virksomhet"),
        VATIAROR("Varig tilrettelagt arbeid i ordinær virksomhet"),
        VV("Varig vernet arbeid (VVA)")
    }

    enum class DeltakerStatusEnum(val tekst: String) {
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
