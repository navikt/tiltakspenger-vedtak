package no.nav.tiltakspenger.vedtak

import java.time.LocalDate
import java.time.LocalDateTime

//Dokumentert her: https://confluence.adeo.no/display/ARENA/Arena+-+Tjeneste+Webservice+-+TiltakOgAktivitet_v1#ArenaTjenesteWebserviceTiltakOgAktivitet_v1-HentTiltakOgAktiviteterForBrukerResponse
data class Tiltaksaktivitet(
    val tiltaksnavn: String, // TODO: Gjør om til enum?
    val aktivitetId: String,
    val tiltakLokaltNavn: String?,
    val arrangoer: String?,
    val bedriftsnummer: String?,
    val deltakelsePeriode: DeltakelsesPeriode?,
    val deltakelseProsent: Float?,
    val deltakerStatus: DeltakerStatus, // TODO: Gjør om til enum?
    val statusSistEndret: LocalDate?,
    val begrunnelseInnsoeking: String,
    val antallDagerPerUke: Float?,
    val innhentet: LocalDateTime,
) : Tidsstempler {

    override fun oppdatert(): LocalDateTime = statusSistEndret?.atStartOfDay() ?: innhentet
    override fun innhentet(): LocalDateTime = innhentet

    data class DeltakelsesPeriode(
        val fom: LocalDate?,
        val tom: LocalDate?,
    )

    data class DeltakerStatus(
        val termnavn: String,
        val status: String
    )

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