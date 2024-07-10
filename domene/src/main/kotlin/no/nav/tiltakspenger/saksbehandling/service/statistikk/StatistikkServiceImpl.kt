package no.nav.tiltakspenger.saksbehandling.service.statistikk

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class StatistikkServiceImpl() : StatistikkService {
    override fun opprettBehandlingTilDvh(sak: SakDetaljer, behandling: Førstegangsbehandling) {
        val dto = SakStatistikkDTO(
            id = UUID.randomUUID(),
            sakId = sak.id.toString(),
            saksnummer = sak.saknummer.toString(),
            behandlingId = behandling.id.toString(),
            relatertBehandlingId = null, // hvis revurdering
            ident = sak.ident,
            mottattTidspunkt = behandling.søknad().opprettet, // Denne bør vi hente fra den nye saksopplysningen for kravMottattDato?
            registrertTidspunkt = behandling.opprettet,
            ferdigBehandletTidspunkt = null,
            vedtakTidspunkt = null,
            endretTidspunkt = LocalDateTime.now(),
            // tekniskTidspunkt skal settes i bigquery når raden opprettes der
            søknadsformat = Format.DIGITAL.name,
            forventetOppstartTidspunkt = behandling.vurderingsperiode.fraOgMed,
            vilkår = listOf(VilkårStatistikkDTO("vilkår", "beskrivelse", Resultat.OPPFYLT)),
            sakYtelse = "IND",
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            behandlingStatus = BehandlingStatus.UNDER_BEHANDLING,
            behandlingResultat = null,
            resultatBegrunnelse = null,
            behandlingMetode = BehandlingMetode.MANUELL.name,
            opprettetAv = "system",
            saksbehandler = null,
            ansvarligBeslutter = null,
            ansvarligEnhet = null,
            avsender = "tiltakspenger-vedtak",
            versjon = "versjon",
        )
    }

    override fun iverksattBehandlingTilDvh(sak: SakDetaljer, behandling: Behandling, vedtak: Vedtak) {
        val resultat = if (vedtak.utfallsperioder.all { it.utfall == UtfallForPeriode.GIR_RETT_TILTAKSPENGER }) {
            BehandlingResultat.INNVILGET
        } else if (vedtak.utfallsperioder.any { it.utfall == UtfallForPeriode.GIR_RETT_TILTAKSPENGER }) {
            BehandlingResultat.DELVIS_INNVILGET
        } else {
            BehandlingResultat.AVSLAG
        }

        val dto = SakStatistikkDTO(
            id = UUID.randomUUID(),
            sakId = sak.id.toString(),
            saksnummer = sak.saknummer.toString(),
            behandlingId = vedtak.behandling.id.toString(),
            relatertBehandlingId = null, // hvis revurdering
            ident = sak.ident,
            mottattTidspunkt = behandling.søknad().opprettet,
            registrertTidspunkt = behandling.opprettet,
            ferdigBehandletTidspunkt = null, // trenger et tidspunkt på iverksatt behandling hvis vi skal fylle ut denne
            vedtakTidspunkt = vedtak.vedtaksdato,
            endretTidspunkt = LocalDateTime.now(),
            søknadsformat = Format.DIGITAL.name,
            forventetOppstartTidspunkt = vedtak.periode.fraOgMed,
            vilkår = listOf(VilkårStatistikkDTO("vilkår", "beskrivelse", Resultat.OPPFYLT)),
            sakYtelse = "IND",
            behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
            behandlingStatus = BehandlingStatus.FERDIG_BEHANDLET,
            behandlingResultat = resultat,
            resultatBegrunnelse = "resultatBegrunnelse",
            behandlingMetode = BehandlingMetode.MANUELL.name,
            opprettetAv = "system",
            saksbehandler = behandling.saksbehandler,
            ansvarligBeslutter = behandling.beslutter,
            ansvarligEnhet = "må hentes fra NORG",
            avsender = "tiltakspenger-vedtak",
            versjon = "versjon",
        )
    }
}

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
    val søknadsformat: String, // papir, digital
    val forventetOppstartTidspunkt: LocalDate, // forventet oppstart av tiltak
    val vilkår: List<VilkårStatistikkDTO>,
    val sakYtelse: String, // IND
    val behandlingType: BehandlingType,
    val behandlingStatus: BehandlingStatus,
    val behandlingResultat: BehandlingResultat?,
    val resultatBegrunnelse: String?, // fylles ut ved klage, avvisning, avslag
    val behandlingMetode: String, // manuell, automatisk
    val opprettetAv: String, // Settes til -5 hvis kode 6 kan være systembruker
    val saksbehandler: String?, // Settes til -5 hvis kode 6
    val ansvarligBeslutter: String?, // Settes til -5 hvis kode 6
    val ansvarligEnhet: String?, // Settes til -5 hvis kode 6
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
