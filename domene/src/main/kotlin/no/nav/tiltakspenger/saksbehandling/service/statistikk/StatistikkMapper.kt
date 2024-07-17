package no.nav.tiltakspenger.saksbehandling.service.statistikk

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import java.time.LocalDateTime
import java.util.UUID

fun opprettBehandlingMapper(sak: SakDetaljer, behandling: Behandling) =
    SakStatistikkDTO(
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
        utbetaltTidspunkt = null,
        // tekniskTidspunkt skal settes i bigquery når raden opprettes der
        tekniskTidspunkt = null,
        søknadsformat = Format.DIGITAL.name,
        forventetOppstartTidspunkt = behandling.vurderingsperiode.fraOgMed,
        vilkår = listOf(VilkårStatistikkDTO("vilkår", "beskrivelse", Resultat.OPPFYLT)),
        sakYtelse = "IND",
        sakUtland = "N",
        behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
        behandlingStatus = BehandlingStatus.UNDER_BEHANDLING,
        behandlingResultat = null,
        resultatBegrunnelse = null,
        behandlingMetode = BehandlingMetode.MANUELL.name,
        opprettetAv = "system",
        saksbehandler = null,
        ansvarligBeslutter = null,
        ansvarligEnhet = null,
        tilbakekrevingsbeløp = null,
        funksjonellPeriodeFom = null,
        funksjonellPeriodeTom = null,
        avsender = "tiltakspenger-vedtak",
        versjon = "versjon",
    )

fun iverksettBehandlingMapper(sak: SakDetaljer, behandling: Behandling, vedtak: Vedtak): SakStatistikkDTO {
    val resultat = if (vedtak.utfallsperioder.all { it.utfall == UtfallForPeriode.GIR_RETT_TILTAKSPENGER }) {
        BehandlingResultat.INNVILGET
    } else if (vedtak.utfallsperioder.any { it.utfall == UtfallForPeriode.GIR_RETT_TILTAKSPENGER }) {
        BehandlingResultat.DELVIS_INNVILGET
    } else {
        BehandlingResultat.AVSLAG
    }

    return SakStatistikkDTO(
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
        utbetaltTidspunkt = null,
        tekniskTidspunkt = null,
        søknadsformat = Format.DIGITAL.name,
        forventetOppstartTidspunkt = vedtak.periode.fraOgMed,
        vilkår = listOf(VilkårStatistikkDTO("vilkår", "beskrivelse", Resultat.OPPFYLT)),
        sakYtelse = "IND",
        sakUtland = "N",
        behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
        behandlingStatus = BehandlingStatus.FERDIG_BEHANDLET,
        behandlingResultat = resultat,
        resultatBegrunnelse = "resultatBegrunnelse",
        behandlingMetode = BehandlingMetode.MANUELL.name,
        opprettetAv = "system",
        saksbehandler = behandling.saksbehandler,
        ansvarligBeslutter = behandling.beslutter,
        ansvarligEnhet = "må hentes fra NORG",
        tilbakekrevingsbeløp = null,
        funksjonellPeriodeFom = null,
        funksjonellPeriodeTom = null,
        avsender = "tiltakspenger-vedtak",
        versjon = "versjon",
    )
}
