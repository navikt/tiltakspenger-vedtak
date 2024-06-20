package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.attestering.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.attestering.AttesteringStatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Utfallsperiode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDagerDTO
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.søkere
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Lovreferanse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.saksbehandling.service.søker.PeriodeDTO
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtfallForPeriodeDTO
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtfallsperiodeDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.AlderssaksopplysningDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.AntallDagerSaksopplysningerDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.EndringDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.EndringsType
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.LovreferanseDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.PersonopplysningerDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.RegistrertTiltakDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.SaksopplysningUtDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.TiltaksdeltagelsesaksopplysningDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.YtelsessaksopplysningerDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.StatusMapper.finnStatus

object SammenstillingForBehandlingDTOMapper {

    fun mapSammenstillingDTO(
        behandling: Førstegangsbehandling,
        personopplysninger: List<Personopplysninger>,
        attesteringer: List<Attestering>,
    ): SammenstillingForBehandlingDTO {
        return SammenstillingForBehandlingDTO(
            behandlingId = behandling.id.toString(),
            saksbehandler = behandling.saksbehandler,
            beslutter = settBeslutter(behandling),
            vurderingsperiode = PeriodeDTO(fra = behandling.vurderingsperiode.fra, til = behandling.vurderingsperiode.til),
            søknadsdato = behandling.søknad().opprettet.toLocalDate(),
            tiltaksdeltagelsesaksopplysninger = TiltaksdeltagelsesaksopplysningDTO(
                vilkår = Vilkår.TILTAKSDELTAGELSE.tittel,
                vilkårLovreferanse = LovreferanseDTO.fraLovreferanse(Lovreferanse.TILTAKSDELTAGELSE),
                saksopplysninger = behandling.tiltak.map {
                    RegistrertTiltakDTO(
                        id = it.id.toString(),
                        arrangør = it.gjennomføring.arrangørnavn,
                        navn = it.gjennomføring.typeNavn,
                        periode = PeriodeDTO(
                            fra = it.deltakelseFom,
                            til = it.deltakelseTom,
                        ),
                        status = it.deltakelseStatus.status,
                        kilde = it.kilde,
                        girRett = it.gjennomføring.rettPåTiltakspenger,
                        harSøkt = true,
                        deltagelseUtfall = utledDeltagelseUtfall(behandling, it.id)?.utfall
                            ?: Utfall.KREVER_MANUELL_VURDERING,
                        begrunnelse = utledDeltagelseUtfall(behandling, it.id)?.detaljer
                            ?: "Fant ikke noe utfall for tiltaksdeltagelse",
                    )
                },
            ),
            stønadsdager = behandling.tiltak.map { settAntallDagerSaksopplysninger(it) },
            alderssaksopplysning = behandling.saksopplysninger().filter { saksopplysning -> saksopplysning.vilkår == Vilkår.ALDER }.map { it ->
                AlderssaksopplysningDTO(
                    periode = PeriodeDTO(fra = it.fom, til = it.tom),
                    kilde = it.kilde.navn,
                    detaljer = it.detaljer,
                    vilkår = it.vilkår.tittel,
                    vilkårTittel = it.vilkår.flateTittel,
                    utfall = settUtfall(behandling = behandling, saksopplysning = it),
                    vilkårLovreferanse = it.vilkår.lovReferanse.map {
                        LovreferanseDTO(
                            lovverk = it.lovverk,
                            paragraf = it.paragraf,
                            beskrivelse = it.beskrivelse,
                        )
                    },
                    grunnlag = 1.januar(2000), // Dette må vi hente fra noe sted. Kommer senere
                )
            }.first(),
            ytelsessaksopplysninger = YtelsessaksopplysningerDTO(
                vilkår = "ANDRE_YTELSER",
                vilkårLovreferanse = LovreferanseDTO(
                    lovverk = Lovreferanse.AAP.lovverk,
                    paragraf = Lovreferanse.AAP.paragraf,
                    beskrivelse = Lovreferanse.AAP.beskrivelse,
                ),
                saksopplysninger = behandling.saksopplysninger().filter { saksopplysning -> saksopplysning.vilkår != Vilkår.ALDER }
                    .map { it ->
                        SaksopplysningUtDTO(
                            periode = PeriodeDTO(fra = it.fom, til = it.tom),
                            kilde = it.kilde.navn,
                            detaljer = it.detaljer,
                            saksopplysning = it.vilkår.tittel,
                            saksopplysningTittel = it.vilkår.flateTittel,
                            utfall = settUtfall(behandling = behandling, saksopplysning = it),
                        )
                    },
                samletUtfall = settSamletUtfallForSaksopplysninger(behandling = behandling, saksopplysninger = behandling.saksopplysninger().filter { saksopplysning -> saksopplysning.vilkår != Vilkår.ALDER }),
            ),
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
            behandlingsteg = when (behandling.tilstand) {
                BehandlingTilstand.IVERKSATT -> "iverksatt"
                BehandlingTilstand.TIL_BESLUTTER -> "tilBeslutter"
                BehandlingTilstand.VILKÅRSVURDERT -> "vilkårsvurdert"
                BehandlingTilstand.OPPRETTET -> "opprettet"
            },
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
            utfallsperioder = behandling.utfallsperioder.map {
                UtfallsperiodeDTO(
                    fom = it.fom,
                    tom = it.tom,
                    antallBarn = it.antallBarn,
                    utfall = when (it.utfall) {
                        UtfallForPeriode.GIR_RETT_TILTAKSPENGER -> UtfallForPeriodeDTO.GIR_RETT_TILTAKSPENGER
                        UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER -> UtfallForPeriodeDTO.GIR_IKKE_RETT_TILTAKSPENGER
                        UtfallForPeriode.KREVER_MANUELL_VURDERING -> UtfallForPeriodeDTO.KREVER_MANUELL_VURDERING
                    },
                )
            },
            kravdatoSaksopplysninger = mapKravdatoSaksopplysningerDTO(kravdatoSaksopplysninger = behandling.kravdatoSaksopplysninger, vilkårsvurderinger = behandling.vilkårsvurderinger),
        )
    }

    private fun mapKravdatoSaksopplysningerDTO(kravdatoSaksopplysninger: KravdatoSaksopplysninger, vilkårsvurderinger: List<Vurdering>): SammenstillingForBehandlingDTO.KravdatoSaksopplysningerDTO {
        val opprinneligSøknadstidspunkt = kravdatoSaksopplysninger.kravdatoSaksopplysningFraSøknad
        val søknadstidspunktFraSaksbehandler = kravdatoSaksopplysninger.kravdatoSaksopplysningFraSaksbehandler
        return SammenstillingForBehandlingDTO.KravdatoSaksopplysningerDTO(
            samletUtfall = hentUtfallForVilkår(
                Vilkår.FRIST_FOR_FRAMSETTING_AV_KRAV,
                vilkårsvurderinger
                    .filter { it.vilkår === Vilkår.FRIST_FOR_FRAMSETTING_AV_KRAV },
            ).toString(),
            opprinneligKravdato = mapKravdatoSaksopplysningDTO(opprinneligSøknadstidspunkt!!),
            kravdatoFraSaksbehandler = if (søknadstidspunktFraSaksbehandler != null) mapKravdatoSaksopplysningDTO(søknadstidspunktFraSaksbehandler) else null,
            vurderinger = vilkårsvurderinger
                .filter { it.vilkår === Vilkår.FRIST_FOR_FRAMSETTING_AV_KRAV }
                .map { it.toVurderingDTO() },
            lovreferanse = LovreferanseDTO.fraLovreferanse(Lovreferanse.FRIST_FOR_FRAMSETTING_AV_KRAV),
        )
    }

    private fun mapKravdatoSaksopplysningDTO(kravdatoSaksopplysning: KravdatoSaksopplysning): SammenstillingForBehandlingDTO.KravdatoSaksopplysningDTO =
        SammenstillingForBehandlingDTO.KravdatoSaksopplysningDTO(
            kravdato = kravdatoSaksopplysning.kravdato,
            kilde = kravdatoSaksopplysning.kilde.toString(),
        )

    private fun Vurdering.toVurderingDTO(): SammenstillingForBehandlingDTO.VurderingDTO =
        SammenstillingForBehandlingDTO.VurderingDTO(
            periode = PeriodeDTO(
                fra = this.fom!!,
                til = this.tom!!,
            ),
            utfall = this.utfall.toString(),
        )

    private fun utledDeltagelseUtfall(behandling: Behandling, tiltakId: TiltakId): Vurdering? {
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
            avklartAntallDager = antallDagerSaksopplysninger.avklartAntallDager.map { settAntallDagerSaksopplysning(it) },
            antallDagerSaksopplysningerFraRegister = antallDagerSaksopplysninger.antallDagerSaksopplysningerFraRegister.map {
                settAntallDagerSaksopplysning(
                    it,
                )
            }.first(),
            tiltak = tiltak.gjennomføring.typeNavn,
            arrangør = tiltak.gjennomføring.arrangørnavn,
            tiltakId = tiltak.id.toString(),
        )
    }

    fun settAntallDagerSaksopplysning(saksopplysning: PeriodeMedVerdi<AntallDager>): AntallDagerDTO =
        AntallDagerDTO(
            antallDager = saksopplysning.verdi.antallDager,
            kilde = saksopplysning.verdi.kilde.toString(),
            periode = PeriodeDTO(
                fra = saksopplysning.periode.fra,
                til = saksopplysning.periode.til,
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

    fun settSamletUtfallForUtfallsperioder(utfallsperioder: List<Utfallsperiode>): String {
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
