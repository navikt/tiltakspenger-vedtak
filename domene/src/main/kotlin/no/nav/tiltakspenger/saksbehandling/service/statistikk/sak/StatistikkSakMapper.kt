package no.nav.tiltakspenger.saksbehandling.service.statistikk.sak

import no.nav.tiltakspenger.felles.nå
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.sak.SakDetaljer
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtakstype
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett

fun opprettBehandlingMapper(sak: SakDetaljer, behandling: Behandling, gjelderKode6: Boolean, versjon: String) =
    StatistikkSakDTO(
        sakId = sak.id.toString(),
        saksnummer = sak.saksnummer.toString(),
        behandlingId = behandling.id.toString(),
        relatertBehandlingId = null,
        ident = sak.fnr.verdi,
        mottattTidspunkt = behandling.søknad.opprettet,
        registrertTidspunkt = behandling.opprettet,
        ferdigBehandletTidspunkt = null,
        vedtakTidspunkt = null,
        endretTidspunkt = nå(),
        utbetaltTidspunkt = null,
        tekniskTidspunkt = nå(),
        søknadsformat = Format.DIGITAL.name,
        forventetOppstartTidspunkt = behandling.vurderingsperiode.fraOgMed,
        vilkår = mapVilkår(behandling.vilkårssett),
        sakYtelse = "IND",
        sakUtland = "N",
        behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
        behandlingStatus = BehandlingStatus.UNDER_BEHANDLING,
        behandlingResultat = null,
        resultatBegrunnelse = null,
        behandlingMetode = BehandlingMetode.MANUELL.name,
        // skal være -5 for kode 6
        opprettetAv = if (gjelderKode6) "-5" else "system",
        saksbehandler = if (gjelderKode6) "-5" else behandling.saksbehandler,
        ansvarligBeslutter = if (gjelderKode6) "-5" else null,
        ansvarligEnhet = if (gjelderKode6) "-5" else null,

        tilbakekrevingsbeløp = null,
        funksjonellPeriodeFom = null,
        funksjonellPeriodeTom = null,
        avsender = "tiltakspenger-vedtak",
        versjon = versjon,
        hendelse = "opprettet_behandling",
    )

fun iverksettBehandlingMapper(sak: SakDetaljer, behandling: Behandling, vedtak: Rammevedtak, gjelderKode6: Boolean, versjon: String): StatistikkSakDTO {
    return StatistikkSakDTO(
        sakId = sak.id.toString(),
        saksnummer = sak.saksnummer.toString(),
        behandlingId = vedtak.behandling.id.toString(),
        relatertBehandlingId = null,
        ident = sak.fnr.verdi,
        mottattTidspunkt = behandling.søknad.opprettet,
        registrertTidspunkt = behandling.opprettet,
        ferdigBehandletTidspunkt = vedtak.vedtaksdato,
        vedtakTidspunkt = vedtak.vedtaksdato,
        endretTidspunkt = nå(),
        utbetaltTidspunkt = null,
        tekniskTidspunkt = nå(),
        søknadsformat = Format.DIGITAL.name,
        forventetOppstartTidspunkt = vedtak.periode.fraOgMed,
        vilkår = mapVilkår(behandling.vilkårssett),
        sakYtelse = "IND",
        sakUtland = "N",
        behandlingType = BehandlingType.FØRSTEGANGSBEHANDLING,
        behandlingStatus = BehandlingStatus.FERDIG_BEHANDLET,
        behandlingResultat = when (vedtak.vedtaksType) {
            Vedtakstype.AVSLAG -> BehandlingResultat.AVSLAG
            Vedtakstype.INNVILGELSE -> BehandlingResultat.INNVILGET
            Vedtakstype.STANS -> BehandlingResultat.STANS
            Vedtakstype.FORLENGELSE -> BehandlingResultat.FORLENGELSE
        },
        resultatBegrunnelse = null,
        behandlingMetode = BehandlingMetode.MANUELL.name,

        // skal være -5 for kode 6
        opprettetAv = if (gjelderKode6) "-5" else "system",
        saksbehandler = if (gjelderKode6) "-5" else behandling.saksbehandler,
        ansvarligBeslutter = if (gjelderKode6) "-5" else behandling.beslutter,
        ansvarligEnhet = if (gjelderKode6) "-5" else "må hentes fra NORG",

        tilbakekrevingsbeløp = null,
        funksjonellPeriodeFom = null,
        funksjonellPeriodeTom = null,
        avsender = "tiltakspenger-vedtak",
        versjon = versjon,
        hendelse = "iverksatt_behandling",
    )
}

private fun mapResultat(utfall: UtfallForPeriode): Resultat {
    return when (utfall) {
        UtfallForPeriode.IKKE_OPPFYLT -> Resultat.IKKE_OPPFYLT
        UtfallForPeriode.OPPFYLT -> Resultat.OPPFYLT
        UtfallForPeriode.UAVKLART -> Resultat.UAVKLART
    }
}

private fun mapVilkår(vilkårssett: Vilkårssett): List<VilkårStatistikkDTO> {
    val intro = vilkårssett.introVilkår.utfall().perioder().map {
        VilkårStatistikkDTO(
            vilkår = "intro",
            beskrivelse = "Om bruker deltar på introprogrammet",
            resultat = mapResultat(it.verdi),
        )
    }

    val kvp = vilkårssett.kvpVilkår.utfall().perioder().map {
        VilkårStatistikkDTO(
            vilkår = "kvp",
            beskrivelse = "Om bruker deltar på kvp",
            resultat = mapResultat(it.verdi),
        )
    }

    val alder = vilkårssett.alderVilkår.utfall().perioder().map {
        VilkårStatistikkDTO(
            vilkår = "alder",
            beskrivelse = "Om bruker er over 18 år",
            resultat = mapResultat(it.verdi),
        )
    }

    val kravfrist = vilkårssett.kravfristVilkår.utfall().perioder().map {
        VilkårStatistikkDTO(
            vilkår = "kravfrist",
            beskrivelse = "Om bruker har søkt innen fristen",
            resultat = mapResultat(it.verdi),
        )
    }

    val institusjon = vilkårssett.institusjonsoppholdVilkår.utfall().perioder().map {
        VilkårStatistikkDTO(
            vilkår = "institusjon",
            beskrivelse = "Om bruker bor på institusjon og får dekket livsopphold",
            resultat = mapResultat(it.verdi),
        )
    }

    val tiltak = vilkårssett.tiltakDeltagelseVilkår.utfall().perioder().map {
        VilkårStatistikkDTO(
            vilkår = "tiltakdeltagelse",
            beskrivelse = "Om bruker deltar på tiltak som gir rett til tiltakspenger",
            resultat = mapResultat(it.verdi),
        )
    }

    val livsopphold = vilkårssett.livsoppholdVilkår.utfall().perioder().map {
        VilkårStatistikkDTO(
            vilkår = "livsopphold",
            beskrivelse = "Om bruker får dekket livsopphold fra andre ytelser",
            resultat = mapResultat(it.verdi),
        )
    }

    return intro + kvp + alder + tiltak + kravfrist + institusjon + livsopphold
}
