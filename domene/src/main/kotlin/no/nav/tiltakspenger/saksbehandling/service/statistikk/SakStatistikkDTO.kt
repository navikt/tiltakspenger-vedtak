package no.nav.tiltakspenger.saksbehandling.service.statistikk

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

// https://confluence.adeo.no/display/navdvh/Teknisk+beskrivelse+av+behov+til+felles+saksbehandlingsstatistikk
data class SakStatistikkDTO(
    val id: UUID,
    val sakId: String,
    val saksnummer: String,
    val behandlingId: String,
    val relatertBehandlingId: String?, // hvis revurdering
    val ident: String,
    val mottattTidspunkt: LocalDateTime, // tidspunkt da behandlingen oppstår (søknad mottatt)
    val registrertTidspunkt: LocalDateTime, // tidspunkt da behandlingen registreres i basen
    val ferdigBehandletTidspunkt: LocalDateTime?,
    val vedtakTidspunkt: LocalDateTime?,
    val endretTidspunkt: LocalDateTime, // nå
    val utbetaltTidspunkt: LocalDateTime?,
    val søknadsformat: String, // papir, digital
    val forventetOppstartTidspunkt: LocalDate, // forventet oppstart av tiltak
    val tekniskTidspunkt: LocalDate?, // forventet oppstart av tiltak
    val vilkår: List<VilkårStatistikkDTO>,
    val sakYtelse: String, // IND
    val sakUtland: String, // Om saken gjelder utland. Settes til N
    val behandlingType: BehandlingType,
    val behandlingStatus: BehandlingStatus,
    val behandlingResultat: BehandlingResultat?,
    val resultatBegrunnelse: String?, // fylles ut ved klage, avvisning, avslag
    val behandlingMetode: String, // manuell, automatisk
    val opprettetAv: String, // Settes til -5 hvis kode 6 kan være systembruker
    val saksbehandler: String?, // Settes til -5 hvis kode 6
    val ansvarligBeslutter: String?, // Settes til -5 hvis kode 6
    val ansvarligEnhet: String?, // Settes til -5 hvis kode 6
    val tilbakekrevingsbeløp: Double?, // beløp som skal tilbakekreves
    val funksjonellPeriodeFom: LocalDate?, // funksjonell periode for tilbakekreving
    val funksjonellPeriodeTom: LocalDate?, // funksjonell periode for tilbakekreving
    val avsender: String,
    val versjon: String, // commit hash
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
}

enum class BehandlingResultat {
    INNVILGET,
    AVSLAG,
    AVVIST,
    DELVIS_INNVILGET,
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
