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
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato.KravdatoSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDagerDTO
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDagerSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDagerSaksopplysningerDTO
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.søkere
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import no.nav.tiltakspenger.saksbehandling.service.søker.PeriodeDTO
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtfallForPeriodeDTO
import no.nav.tiltakspenger.vedtak.clients.utbetaling.UtfallsperiodeDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.EndringDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.EndringsType
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.FaktaDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.KategoriserteSaksopplysningerDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.LovreferanseDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.PersonopplysningerDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.RegistrertTiltakDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.SaksopplysningUtDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.SøknadDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.StatusMapper.finnStatus

object SammenstillingForBehandlingDTOMapper {
    enum class Kategori(val tittel: String, val vilkår: List<Vilkår>) {
        ALDER("Alder", listOf(Vilkår.ALDER)),
        INTROKVP(
            "Introduksjonsprogrammet og Kvalifiseringsprogrammet",
            listOf(Vilkår.INTROPROGRAMMET, Vilkår.KVP),
        ),
        UTBETALINGER(
            "Utbetalinger",
            listOf(
                Vilkår.FORELDREPENGER,
                Vilkår.PLEIEPENGER_SYKT_BARN,
                Vilkår.PLEIEPENGER_NÆRSTÅENDE,
                Vilkår.ALDERSPENSJON,
                Vilkår.PENSJONSINNTEKT,
                Vilkår.ETTERLØNN,
                Vilkår.AAP,
                Vilkår.DAGPENGER,
                Vilkår.GJENLEVENDEPENSJON,
                Vilkår.FORELDREPENGER,
                Vilkår.JOBBSJANSEN,
                Vilkår.UFØRETRYGD,
                Vilkår.OMSORGSPENGER,
                Vilkår.OPPLÆRINGSPENGER,
                Vilkår.OVERGANGSSTØNAD,
                Vilkår.SYKEPENGER,
                Vilkår.SVANGERSKAPSPENGER,
                Vilkår.SUPPLERENDESTØNADFLYKTNING,
            ),
        ),
        INSTITUSJONSOPPHOLD("Institusjonsopphold", listOf(Vilkår.INSTITUSJONSOPPHOLD)),
    }

    private val fakta = hashMapOf(
        "AAP" to FaktaDTO(
            harYtelse = "Bruker mottar AAP",
            harIkkeYtelse = "Bruker mottar ikke AAP",
        ),
        "ALDER" to FaktaDTO(
            harYtelse = "Bruker er under 18 år",
            harIkkeYtelse = "Bruker er over 18 år",
        ),
        "ALDERSPENSJON" to FaktaDTO(
            harYtelse = "Bruker mottar alderspensjon",
            harIkkeYtelse = "Bruker mottar ikke alderspensjon",
        ),
        "DAGPENGER" to FaktaDTO(
            harYtelse = "Bruker mottar dagpenger",
            harIkkeYtelse = "Bruker mottar ikke dagpenger",
        ),
        "FORELDREPENGER" to FaktaDTO(
            harYtelse = "Bruker mottar foreldrepenger",
            harIkkeYtelse = "Bruker mottar ikke foreldrepenger",
        ),
        "GJENLEVENDEPENSJON" to FaktaDTO(
            harYtelse = "Bruker mottar gjenlevendepensjon",
            harIkkeYtelse = "Bruker mottar ikke gjenlevendepensjon",
        ),
        "INSTITUSJONSOPPHOLD" to FaktaDTO(
            harYtelse = "Bruker har institusjonsopphold",
            harIkkeYtelse = "Bruker har ikke institusjonsopphold",
        ),
        "INTROPROGRAMMET" to FaktaDTO(
            harYtelse = "Bruker deltar på introduksjonsprogrammet",
            harIkkeYtelse = "Bruker deltar ikke i introduksjonsprogrammet",
        ),
        "JOBBSJANSEN" to FaktaDTO(
            harYtelse = "Bruker deltar på jobbsjansen",
            harIkkeYtelse = "Bruker deltar ikke på jobbsjansen",
        ),
        "KOMMUNALEYTELSER" to FaktaDTO(
            harYtelse = "Bruker mottar kommunaleytelser",
            harIkkeYtelse = "Bruker mottar ikke kommunaleytelser",
        ),
        "KVP" to FaktaDTO(
            harYtelse = "Bruker går på KVP",
            harIkkeYtelse = "Bruker går ikke på KVP",
        ),
        "LØNNSINNTEKT" to FaktaDTO(
            harYtelse = "Bruker mottar lønnsinntekt",
            harIkkeYtelse = "Bruker mottar ikke lønnsinntekt",
        ),
        "OMSORGSPENGER" to FaktaDTO(
            harYtelse = "Bruker mottar omsorgspenger",
            harIkkeYtelse = "Bruker mottar ikke omsorgspenger",
        ),
        "OPPLÆRINGSPENGER" to FaktaDTO(
            harYtelse = "Bruker mottar opplæringspenger",
            harIkkeYtelse = "Bruker mottar ikke opplæringspenger",
        ),
        "OVERGANGSSTØNAD" to FaktaDTO(
            harYtelse = "Bruker mottar overgangsstønad",
            harIkkeYtelse = "Bruker mottar ikke overgangsstønad",
        ),
        "PENSJONSINNTEKT" to FaktaDTO(
            harYtelse = "Bruker mottar pensjonsinntekt",
            harIkkeYtelse = "Bruker mottar ikke pensjonsinntekt",
        ),
        "PLEIEPENGER_NÆRSTÅENDE" to FaktaDTO(
            harYtelse = "Bruker mottar pleiepenger nærstående",
            harIkkeYtelse = "Bruker mottar ikke pleiepenger nærstående",
        ),
        "PLEIEPENGER_SYKT_BARN" to FaktaDTO(
            harYtelse = "Bruker mottar pleiepenger sykt barn",
            harIkkeYtelse = "Bruker mottar ikke pleiepenger sykt barn",
        ),
        "STATLIGEYTELSER" to FaktaDTO(
            harYtelse = "Bruker mottar statligeytelser",
            harIkkeYtelse = "Bruker mottar ikke statligeytelser",
        ),
        "SUPPLERENDESTØNADALDER" to FaktaDTO(
            harYtelse = "Bruker mottar supplerendestønadalder",
            harIkkeYtelse = "Bruker mottar ikke supplerendestønadalder",
        ),
        "SUPPLERENDESTØNADFLYKTNING" to FaktaDTO(
            harYtelse = "Bruker mottar supplerende stønad flyktning",
            harIkkeYtelse = "Bruker mottar ikke supplerende stønad flyktning",
        ),
        "SVANGERSKAPSPENGER" to FaktaDTO(
            harYtelse = "Bruker mottar svangerskapspenger",
            harIkkeYtelse = "Bruker mottar ikke svangerskapspenger",
        ),
        "SYKEPENGER" to FaktaDTO(
            harYtelse = "Bruker mottar sykepenger",
            harIkkeYtelse = "Bruker mottar ikke sykepenger",
        ),
        "UFØRETRYGD" to FaktaDTO(
            harYtelse = "Bruker mottar uføretrygd",
            harIkkeYtelse = "Bruker mottar ikke uføretrygd",
        ),
        "ETTERLØNN" to FaktaDTO(
            harYtelse = "Bruker mottar etterlønn",
            harIkkeYtelse = "Bruker mottar ikke etterlønn",
        ),
    )

    fun mapSammenstillingDTO(
        behandling: Førstegangsbehandling,
        personopplysninger: List<Personopplysninger>,
        attesteringer: List<Attestering>,
    ): SammenstillingForBehandlingDTO {
        return SammenstillingForBehandlingDTO(
            behandlingId = behandling.id.toString(),
            saksbehandler = behandling.saksbehandler,
            beslutter = settBeslutter(behandling),
            fom = behandling.vurderingsperiode.fra,
            tom = behandling.vurderingsperiode.til,
            søknad = SøknadDTO(
                søknadsdato = behandling.søknad().opprettet.toLocalDate(),
                arrangoernavn = behandling.søknad().tiltak.arrangør,
                tiltakstype = behandling.søknad().tiltak.typeNavn,
                deltakelseFom = behandling.søknad().tiltak.deltakelseFom,
                deltakelseTom = behandling.søknad().tiltak.deltakelseTom,
            ),
            registrerteTiltak = behandling.tiltak.map {
                RegistrertTiltakDTO(
                    id = it.id.toString(),
                    arrangør = it.gjennomføring.arrangørnavn,
                    navn = it.gjennomføring.typeNavn,
                    periode = PeriodeDTO(
                        fra = it.deltakelseFom,
                        til = it.deltakelseTom,
                    ),
                    prosent = it.deltakelseProsent?.toInt() ?: 0,
                    status = it.deltakelseStatus.status,
                    kilde = it.kilde,
                    girRett = it.gjennomføring.rettPåTiltakspenger,
                    harSøkt = true,
                    deltagelseUtfall = utledDeltagelseUtfall(behandling, it.id)?.utfall
                        ?: Utfall.KREVER_MANUELL_VURDERING,
                    begrunnelse = utledDeltagelseUtfall(behandling, it.id)?.detaljer
                        ?: "Fant ikke noe utfall for tiltaksdeltagelse",
                    antallDagerSaksopplysninger = settAntallDagerSaksopplysninger(it.antallDagerSaksopplysninger),
                )
            },

            saksopplysninger = Kategori.entries.map { kategori ->
                KategoriserteSaksopplysningerDTO(
                    kategoriTittel = kategori.tittel,
                    saksopplysninger = behandling.saksopplysninger().filter { kategori.vilkår.contains(it.vilkår) }
                        .map { it ->
                            val fakta =
                                fakta[it.vilkår.tittel] ?: FaktaDTO(harYtelse = "ukjent", harIkkeYtelse = "ukjent")
                            SaksopplysningUtDTO(
                                fom = it.fom,
                                tom = it.tom,
                                kilde = it.kilde.navn,
                                detaljer = it.detaljer,
                                typeSaksopplysning = it.typeSaksopplysning.name,
                                vilkårTittel = it.vilkår.tittel,
                                vilkårFlateTittel = it.vilkår.flateTittel,
                                fakta = fakta,
                                utfall = settUtfall(behandling = behandling, saksopplysning = it),
                                vilkårLovReferense = it.vilkår.lovReference.map {
                                    LovreferanseDTO(
                                        lovverk = it.lovverk,
                                        paragraf = it.paragraf,
                                        beskrivelse = it.beskrivelse,
                                    )
                                },
                            )
                        },
                    samletUtfall = settSamletUtfallForSaksopplysninger(
                        behandling,
                        behandling.saksopplysninger().filter { kategori.vilkår.contains(it.vilkår) },
                    ),
                )
            },
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
            tilstand = when (behandling.tilstand) {
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
            opprinneligKravdato = mapKravdatoSaksopplysningDTO(opprinneligSøknadstidspunkt!!),
            kravdatoFraSaksbehandler = if (søknadstidspunktFraSaksbehandler != null) mapKravdatoSaksopplysningDTO(søknadstidspunktFraSaksbehandler) else null,
            vurderinger = vilkårsvurderinger
                .filter { it.vilkår === Vilkår.FRIST_FOR_FRAMSETTING_AV_KRAV }
                .map { it.toVurderingDTO() },
        )
    }

    private fun mapKravdatoSaksopplysningDTO(kravdatoSaksopplysning: KravdatoSaksopplysning): SammenstillingForBehandlingDTO.KravdatoSaksopplysningDTO =
        SammenstillingForBehandlingDTO.KravdatoSaksopplysningDTO(
            verdi = kravdatoSaksopplysning.kravdato,
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

    fun settAntallDagerSaksopplysninger(antallDagerSaksopplysninger: AntallDagerSaksopplysninger): AntallDagerSaksopplysningerDTO =
        AntallDagerSaksopplysningerDTO(
            avklartAntallDager = antallDagerSaksopplysninger.avklartAntallDager.map { settAntallDagerSaksopplysning(it) },
            antallDagerSaksopplysningerFraSBH = antallDagerSaksopplysninger.antallDagerSaksopplysningerFraSBH.map {
                settAntallDagerSaksopplysning(
                    it,
                )
            },
            antallDagerSaksopplysningerFraRegister = antallDagerSaksopplysninger.antallDagerSaksopplysningerFraRegister.map {
                settAntallDagerSaksopplysning(
                    it,
                )
            },
        )

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
