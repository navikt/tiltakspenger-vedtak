package no.nav.tiltakspenger.saksbehandling.service.statistikk.sak

import java.time.LocalDate
import java.time.LocalDateTime

// https://confluence.adeo.no/display/navdvh/Teknisk+beskrivelse+av+behov+til+felles+saksbehandlingsstatistikk
data class StatistikkSakDTO(
    val sakId: String,
    val saksnummer: String,
    val behandlingId: String,
    val relatertBehandlingId: String?,
    val ident: String,
    // tidspunkt da behandlingen oppstår (søknad mottatt)
    val mottattTidspunkt: LocalDateTime,
    // tidspunkt da behandlingen registreres i basen
    val registrertTidspunkt: LocalDateTime,
    val ferdigBehandletTidspunkt: LocalDateTime?,
    val vedtakTidspunkt: LocalDateTime?,
    // nå
    val endretTidspunkt: LocalDateTime,
    val utbetaltTidspunkt: LocalDateTime?,
    // papir, digital
    val søknadsformat: String,
    // forventet oppstart av tiltak
    val forventetOppstartTidspunkt: LocalDate,
    // forventet oppstart av tiltak
    val tekniskTidspunkt: LocalDate?,
    val vilkår: List<VilkårStatistikkDTO>,
    // IND
    val sakYtelse: String,
    // Om saken gjelder utland. Settes til N
    val sakUtland: String,
    val behandlingType: BehandlingType,
    val behandlingStatus: BehandlingStatus,
    val behandlingResultat: BehandlingResultat?,
    // fylles ut ved klage, avvisning, avslag
    val resultatBegrunnelse: String?,
    // manuell, automatisk
    val behandlingMetode: String,
    // Settes til -5 hvis kode 6 kan være systembruker
    val opprettetAv: String,
    // Settes til -5 hvis kode 6
    val saksbehandler: String?,
    // Settes til -5 hvis kode 6
    val ansvarligBeslutter: String?,
    // Settes til -5 hvis kode 6
    val ansvarligEnhet: String?,
    // beløp som skal tilbakekreves
    val tilbakekrevingsbeløp: Double?,
    // funksjonell periode for tilbakekreving
    val funksjonellPeriodeFom: LocalDate?,
    // funksjonell periode for tilbakekreving
    val funksjonellPeriodeTom: LocalDate?,
    val avsender: String,
    // commit hash
    val versjon: String,
    val hendelse: String,
)

data class VilkårStatistikkDTO(
    val vilkår: String,
    val beskrivelse: String,
    val resultat: Resultat,
)

enum class Format {
    PAPIR,
    DIGITAL,
}

enum class Resultat {
    OPPFYLT,
    IKKE_OPPFYLT,
    IKKE_VURDERT,
    UAVKLART,
}

enum class BehandlingResultat {
    INNVILGET,
    AVSLAG,
    STANS,
    FORLENGELSE,
}

enum class BehandlingStatus {
    UNDER_BEHANDLING,
    FERDIG_BEHANDLET,
    AVSLUTTET,
}

enum class BehandlingType {
    FØRSTEGANGSBEHANDLING,
    REVURDERING,
    KLAGE,
    ANKE,
}

enum class BehandlingMetode {
    MANUELL,
    AUTOMATISK,
}
