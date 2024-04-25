package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.saksbehandling.domene.attestering.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.attestering.AttesteringStatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingOpprettet
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Utfallsperiode
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
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.PersonopplysningerDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.RegistrertTiltakDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTO.SøknadDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.StatusMapper.finnStatus

object SammenstillingForBehandlingDTOMapper {

    enum class Kategori(val tittel: String, val vilkår: List<Vilkår>) {
        ALDER("Alder", listOf(Vilkår.ALDER)),
        TILTAK("Tiltak", listOf(Vilkår.TILTAKSPENGER)),
        INTROKVP("Introduksjonsprogrammet og Kvalifiseringsprogrammet", listOf(Vilkår.INTROPROGRAMMET, Vilkår.KVP)),
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
        "TILTAKSPENGER" to FaktaDTO(
            harYtelse = "Bruker mottar tiltakspenger",
            harIkkeYtelse = "Bruker mottar ikke tiltakspenger",
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
                    arrangør = it.gjennomføring.arrangørnavn,
                    dagerIUken = it.deltakelseDagerUke?.toInt() ?: 0,
                    navn = it.gjennomføring.typeNavn,
                    periode = PeriodeDTO(
                        fra = it.deltakelseFom,
                        til = it.deltakelseTom,
                    ),
                    prosent = it.deltakelseProsent?.toInt() ?: 0,
                    status = it.deltakelseStatus.status,
                )
            },
            saksopplysninger = Kategori.entries.map { kategori ->
                KategoriserteSaksopplysningerDTO(
                    kategoriTittel = kategori.tittel,
                    saksopplysninger = emptyList(), // TODO: Her har det skjedd en quickfix for å gjøre kompilatoren glad 🙈
//                    behandling.avklarteSaksopplysninger().filter { kategori.vilkår.contains(it.vilkår) }
//                        .map {
//                            val fakta =
//                                fakta[it.vilkår.tittel] ?: FaktaDTO(harYtelse = "ukjent", harIkkeYtelse = "ukjent")
//                            SaksopplysningUtDTO(
//                                fom = it.,
//                                tom = it.tom,
//                                kilde = it.kilde.navn,
//                                detaljer = it.detaljer,
//                                typeSaksopplysning = it.typeSaksopplysning.name,
//                                vilkårTittel = it.vilkår.tittel,
//                                vilkårFlateTittel = it.vilkår.flateTittel,
//                                fakta = fakta,
//                                utfall = settUtfall(behandling = behandling, saksopplysning = it),
//                            )
//                        },
                    samletUtfall = settSamletUtfallForSaksopplysninger(
                        behandling,
                        emptyList(),  // TODO: Her har det skjedd en quickfix for å gjøre kompilatoren glad 🙈
//                        behandling.avklarteSaksopplysninger().filter { kategori.vilkår.contains(it.vilkår) },
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
            tilstand = when (behandling) {
                // todo: dette kunne kanskje vært en egen "tilstand"-property på de ulike behandlingstypene?
                is BehandlingIverksatt -> "iverksatt"
                is BehandlingTilBeslutter -> "tilBeslutter"
                is BehandlingVilkårsvurdert -> "vilkårsvurdert"
                is BehandlingOpprettet -> "opprettet"
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
        )
    }

    fun settBeslutter(behandling: Førstegangsbehandling): String? =
        when (behandling) {
            is BehandlingIverksatt -> behandling.beslutter
            is BehandlingTilBeslutter -> behandling.beslutter
            else -> null
        }

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
        return when (behandling) {
            is BehandlingVilkårsvurdert -> hentUtfallForVilkår(
                saksopplysning.vilkår,
                behandling.vurderinger()
            ).name

            is BehandlingTilBeslutter -> hentUtfallForVilkår(saksopplysning.vilkår, behandling.vurderinger()).name
            is BehandlingIverksatt -> hentUtfallForVilkår(saksopplysning.vilkår, behandling.vurderinger()).name
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
