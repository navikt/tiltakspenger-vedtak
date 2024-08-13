package no.nav.tiltakspenger.saksbehandling.service.statistikk.sak

import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.VedtaksType
import java.time.LocalDateTime
import java.util.UUID

fun opprettBehandlingMapper(sak: SakDetaljer, behandling: Behandling) =
    StatistikkSakDTO(
        id = UUID.randomUUID(),
        sakId = sak.id.toString(),
        saksnummer = sak.saksnummer.toString(),
        behandlingId = behandling.id.toString(),
        relatertBehandlingId = null,
        // hvis revurdering
        ident = sak.fnr.verdi,
        mottattTidspunkt = behandling.søknad.opprettet,
        // Denne bør vi hente fra den nye saksopplysningen for kravMottattDato?
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

fun iverksettBehandlingMapper(sak: SakDetaljer, behandling: Behandling, vedtak: Vedtak): StatistikkSakDTO {
    return StatistikkSakDTO(
        id = UUID.randomUUID(),
        sakId = sak.id.toString(),
        saksnummer = sak.saksnummer.toString(),
        behandlingId = vedtak.behandling.id.toString(),
        // hvis revurdering
        relatertBehandlingId = null,
        ident = sak.fnr.verdi,
        mottattTidspunkt = behandling.søknad.opprettet,
        registrertTidspunkt = behandling.opprettet,
        // trenger et tidspunkt på iverksatt behandling hvis vi skal fylle ut denne
        ferdigBehandletTidspunkt = null,
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
        behandlingResultat = when (vedtak.vedtaksType) {
            VedtaksType.AVSLAG -> BehandlingResultat.AVSLAG
            VedtaksType.INNVILGELSE -> BehandlingResultat.INNVILGET
            // hva skal vi sette her
            VedtaksType.STANS -> BehandlingResultat.AVVIST
            VedtaksType.FORLENGELSE -> BehandlingResultat.INNVILGET
        },
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
