package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.attestering.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.attestering.AttesteringStatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Utfallsperiode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.stønadsdager.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.søkere
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltak.Tiltak
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.AntallDagerSaksopplysningerDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.EndringDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.EndringsType
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.PersonopplysningerDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.RegistrertTiltakDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.TiltaksdeltagelsesaksopplysningDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.StatusMapper.finnStatus
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.toDTO
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO

internal object SammenstillingForBehandlingDTOMapper {

    fun mapSammenstillingDTO(
        behandling: Førstegangsbehandling,
        personopplysninger: List<Personopplysninger>,
        attesteringer: List<Attestering>,
    ): SammenstillingForBehandlingDTO {
        return SammenstillingForBehandlingDTO(
            behandlingId = behandling.id.toString(),
            saksbehandler = behandling.saksbehandler,
            beslutter = settBeslutter(behandling),
            vurderingsperiode = PeriodeDTO(
                fraOgMed = behandling.vurderingsperiode.fraOgMed.toString(),
                tilOgMed = behandling.vurderingsperiode.tilOgMed.toString(),
            ),
            tiltaksdeltagelsesaksopplysning = TiltaksdeltagelsesaksopplysningDTO(
                vilkår = Vilkår.TILTAKSDELTAGELSE.tittel,
                vilkårLovreferanse = Lovreferanse.TILTAKSDELTAGELSE.toDTO(),
                saksopplysninger = behandling.tiltak.tiltak.map {
                    RegistrertTiltakDTO(
                        id = it.id.toString(),
                        navn = it.gjennomføring.typeNavn,
                        periode = PeriodeDTO(
                            fraOgMed = it.deltakelseFom.toString(),
                            tilOgMed = it.deltakelseTom.toString(),
                        ),
                        status = it.deltakelseStatus.status,
                        kilde = it.kilde,
                        girRett = it.gjennomføring.rettPåTiltakspenger,
                        deltagelseUtfall = utledVurdertUtfall(behandling, it.id)?.utfall
                            ?: Utfall.KREVER_MANUELL_VURDERING,
                    )
                }.first(),
            ),
            stønadsdager = behandling.tiltak.tiltak.map { settAntallDagerSaksopplysninger(it) },
            personopplysninger = personopplysninger.søkere().map {
                PersonopplysningerDTO(
                    ident = it.ident,
                    fornavn = it.fornavn,
                    etternavn = it.etternavn,
                    skjerming = it.avklartSkjerming(),
                    strengtFortrolig = it.strengtFortrolig,
                    fortrolig = it.fortrolig,
                )
            }.first(),
            behandlingTilstand = behandling.tilstand,
            status = finnStatus(behandling),
            endringslogg = attesteringer.map { att ->
                EndringDTO(
                    type = when (att.svar) {
                        AttesteringStatus.GODKJENT -> EndringsType.GODKJENT.beskrivelse
                        AttesteringStatus.SENDT_TILBAKE -> EndringsType.SENDT_TILBAKE.beskrivelse
                    },
                    begrunnelse = att.begrunnelse ?: "Godkjent av beslutter",
                    endretAv = att.beslutter,
                    endretTidspunkt = att.tidspunkt,
                )
            },
            samletUtfall = settSamletUtfallForUtfallsperioder(
                utfallsperioder = behandling.utfallsperioder,
            ),
            vilkårsett = behandling.vilkårssett.toDTO(),
        )
    }

    private fun utledVurdertUtfall(behandling: Behandling, tiltakId: TiltakId): Vurdering? {
        return when (behandling.tilstand) {
            BehandlingTilstand.VILKÅRSVURDERT -> {
                behandling.vilkårsvurderinger.find { vurdering -> vurdering.grunnlagId == tiltakId.toString() }
            }

            BehandlingTilstand.TIL_BESLUTTER -> {
                behandling.vilkårsvurderinger.find { vurdering -> vurdering.grunnlagId == tiltakId.toString() }
            }

            BehandlingTilstand.IVERKSATT -> {
                behandling.vilkårsvurderinger.find { vurdering -> vurdering.grunnlagId == tiltakId.toString() }
            }

            else -> {
                null
            }
        }
    }

    fun settBeslutter(behandling: Førstegangsbehandling): String? =
        when (behandling.tilstand) {
            BehandlingTilstand.IVERKSATT -> behandling.beslutter
            BehandlingTilstand.TIL_BESLUTTER -> behandling.beslutter
            else -> null
        }

    fun settAntallDagerSaksopplysninger(tiltak: Tiltak): AntallDagerSaksopplysningerDTO {
        val antallDagerSaksopplysninger = tiltak.antallDagerSaksopplysninger

        return AntallDagerSaksopplysningerDTO(
            antallDagerSaksopplysningFraRegister = antallDagerSaksopplysninger.antallDagerSaksopplysningerFraRegister.map {
                settAntallDagerSaksopplysning(
                    it,
                )
            }.first(),
            tiltak = tiltak.gjennomføring.typeNavn,
            tiltakId = tiltak.id.toString(),
        )
    }

    private fun settAntallDagerSaksopplysning(saksopplysning: PeriodeMedVerdi<AntallDager>): AntallDagerDTO =
        AntallDagerDTO(
            antallDager = saksopplysning.verdi.antallDager,
            kilde = saksopplysning.verdi.kilde.toString(),
            periode = PeriodeDTO(
                fraOgMed = saksopplysning.periode.fraOgMed.toString(),
                tilOgMed = saksopplysning.periode.tilOgMed.toString(),
            ),
        )

    fun settSamletUtfallForSaksopplysninger(
        behandling: Behandling,
        saksopplysninger: List<Saksopplysning>,
    ): String {
        if (saksopplysninger.any { s ->
                settUtfall(
                    behandling,
                    s,
                ) == Utfall.IKKE_OPPFYLT.name
            }
        ) {
            return Utfall.IKKE_OPPFYLT.name
        }
        if (saksopplysninger.any { s ->
                settUtfall(
                    behandling,
                    s,
                ) == Utfall.KREVER_MANUELL_VURDERING.name
            }
        ) {
            return Utfall.KREVER_MANUELL_VURDERING.name
        }
        return Utfall.OPPFYLT.name
    }

    private fun settSamletUtfallForUtfallsperioder(utfallsperioder: List<Utfallsperiode>): String {
        if (utfallsperioder.any { utfallsperiode ->
                utfallsperiode.utfall == UtfallForPeriode.KREVER_MANUELL_VURDERING
            }
        ) {
            return Utfall.KREVER_MANUELL_VURDERING.name
        }
        if (utfallsperioder.any { utfallsperiode ->
                utfallsperiode.utfall == UtfallForPeriode.GIR_RETT_TILTAKSPENGER
            }
        ) {
            return Utfall.OPPFYLT.name
        }
        return Utfall.IKKE_OPPFYLT.name
    }

    fun settUtfall(behandling: Behandling, saksopplysning: Saksopplysning): String {
        return when (behandling.tilstand) {
            BehandlingTilstand.VILKÅRSVURDERT -> hentUtfallForVilkår(
                saksopplysning.vilkår,
                behandling.vilkårsvurderinger,
            ).name

            BehandlingTilstand.TIL_BESLUTTER -> hentUtfallForVilkår(
                saksopplysning.vilkår,
                behandling.vilkårsvurderinger,
            ).name

            BehandlingTilstand.IVERKSATT -> hentUtfallForVilkår(
                saksopplysning.vilkår,
                behandling.vilkårsvurderinger,
            ).name

            else -> Utfall.KREVER_MANUELL_VURDERING.name
        }
    }

    fun hentUtfallForVilkår(vilkår: Vilkår, vurderinger: List<Vurdering>): Utfall {
        if (vurderinger.any { it.vilkår == vilkår && it.utfall == Utfall.KREVER_MANUELL_VURDERING }) return Utfall.KREVER_MANUELL_VURDERING
        if (vurderinger.any { it.vilkår == vilkår && it.utfall == Utfall.IKKE_OPPFYLT }) return Utfall.IKKE_OPPFYLT
        if (vurderinger.filter { it.vilkår == vilkår }.all { it.utfall == Utfall.OPPFYLT }) return Utfall.OPPFYLT
        throw IllegalStateException("Kunne ikke finne utfall for vilkår $vilkår")
    }
}
