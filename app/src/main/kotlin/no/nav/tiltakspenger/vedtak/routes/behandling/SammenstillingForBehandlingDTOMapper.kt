package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.attestering.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.attestering.AttesteringStatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingTilstand
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.UtfallForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDagerDTO
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDagerSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDagerSaksopplysningerDTO
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.søker
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Kategori
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
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
            beslutter = getBeslutter(behandling),
            fom = behandling.vurderingsperiode.fra,
            tom = behandling.vurderingsperiode.til,
            søknad = SøknadDTO(
                søknadsdato = behandling.søknad().opprettet.toLocalDate(),
                arrangoernavn = behandling.søknad().tiltak.arrangør,
                tiltakstype = behandling.søknad().tiltak.typeNavn,
                deltakelseFom = behandling.søknad().tiltak.deltakelseFom,
                deltakelseTom = behandling.søknad().tiltak.deltakelseTom,
            ),
            registrerteTiltak = behandling.vilkårData.tiltakVilkårData.tiltak.map {
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
                    deltagelseUtfall = it.vilkårsvurderTiltaksdeltagelse().utfall.utfallForPeriodisering(), // TODO: Det kan jo være mer enn ett utfall!
                    begrunnelse = it.vilkårsvurderTiltaksdeltagelse().detaljer,
                    antallDagerSaksopplysninger = settAntallDagerSaksopplysninger(it.antallDagerSaksopplysninger),
                )
            },
            // TODO: Vi bør nok ha en litt annen struktur også mot frontend..
            saksopplysninger = Kategori.entries.map { kategori ->
                KategoriserteSaksopplysningerDTO(
                    kategoriTittel = kategori.tittel,
                    saksopplysninger = behandling.livsoppholdVilkårData.livsoppholdYtelser
                        .flatMap { it.value.periodiseringAvSaksopplysningOgUtfall().perioder() }
                        .filter { kategori.vilkår.contains(it.verdi.vilkår) }
                        .map {
                            val fakta =
                                fakta[it.verdi.vilkår.tittel] ?: FaktaDTO(
                                    harYtelse = "ukjent",
                                    harIkkeYtelse = "ukjent",
                                )
                            SaksopplysningUtDTO(
                                fom = it.periode.fra,
                                tom = it.periode.til,
                                kilde = it.verdi.kilde.navn,
                                detaljer = it.verdi.detaljer,
                                typeSaksopplysning = it.verdi.harYtelse.name,
                                vilkårTittel = it.verdi.vilkår.tittel,
                                vilkårFlateTittel = it.verdi.vilkår.flateTittel,
                                fakta = fakta,
                                utfall = it.verdi.utfall.name,
                                vilkårLovReferense = it.verdi.vilkår.lovReference.map { lovRef ->
                                    LovreferanseDTO(
                                        lovverk = lovRef.lovverk,
                                        paragraf = lovRef.paragraf,
                                        beskrivelse = lovRef.beskrivelse,
                                    )
                                },
                            )
                        },
                    samletUtfall = behandling.livsoppholdVilkårData.samletUtfallPerKategori()[kategori]!!.utfallForPeriodisering().name,
                )
            },
            personopplysninger = personopplysninger.søker().let {
                PersonopplysningerDTO(
                    ident = it.ident,
                    fornavn = it.fornavn,
                    etternavn = it.etternavn,
                    skjerming = it.avklartSkjerming(),
                    strengtFortrolig = it.strengtFortrolig,
                    fortrolig = it.fortrolig,
                )
            },
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
            samletUtfall = behandling.livsoppholdVilkårData.samletUtfall().utfallForPeriodisering().name,
            utfallsperioder = behandling.utfallsperioder?.perioder()?.map {
                UtfallsperiodeDTO(
                    fom = it.periode.fra,
                    tom = it.periode.til,
                    antallBarn = it.verdi.antallBarn,
                    utfall = when (it.verdi.utfall) {
                        UtfallForPeriode.GIR_RETT_TILTAKSPENGER -> UtfallForPeriodeDTO.GIR_RETT_TILTAKSPENGER
                        UtfallForPeriode.GIR_IKKE_RETT_TILTAKSPENGER -> UtfallForPeriodeDTO.GIR_IKKE_RETT_TILTAKSPENGER
                        UtfallForPeriode.KREVER_MANUELL_VURDERING -> UtfallForPeriodeDTO.KREVER_MANUELL_VURDERING
                    },
                )
            } ?: emptyList(),
        )
    }

    fun Periodisering<Utfall>.utfallForPeriodisering(): Utfall {
        if (this.perioder().any { it.verdi == Utfall.UAVKLART }) {
            return Utfall.UAVKLART
        }
        if (this.perioder().any { it.verdi == Utfall.OPPFYLT }) {
            return Utfall.OPPFYLT
        }
        return Utfall.IKKE_OPPFYLT
    }

    fun getBeslutter(behandling: Førstegangsbehandling): String? =
        when (behandling.tilstand) {
            BehandlingTilstand.IVERKSATT -> behandling.beslutter
            BehandlingTilstand.TIL_BESLUTTER -> behandling.beslutter
            else -> null
        }

    fun settAntallDagerSaksopplysninger(antallDagerSaksopplysninger: AntallDagerSaksopplysninger): AntallDagerSaksopplysningerDTO =
        AntallDagerSaksopplysningerDTO(
            avklartAntallDager = antallDagerSaksopplysninger.avklartAntallDager.map { settAntallDagerSaksopplysning(it) },
            antallDagerSaksopplysningerFraSBH = antallDagerSaksopplysninger.antallDagerSaksopplysningerFraSBH.map {
                settAntallDagerSaksopplysning(it)
            },
            antallDagerSaksopplysningerFraRegister = antallDagerSaksopplysninger.antallDagerSaksopplysningerFraRegister.map {
                settAntallDagerSaksopplysning(it)
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
}
