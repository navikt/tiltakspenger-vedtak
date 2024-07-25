package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.objectmothers.ObjectMother.beslutter
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler123
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.behandling.stønadsdager.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.stønadsdager.AntallDagerSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltak.Tiltak
import java.time.LocalDate

interface BehandlingMother {
    fun behandling(
        periode: Periode = Periode(1.januar(2023), 31.mars(2023)),
        sakId: SakId = SakId.random(),
        søknad: Søknad = ObjectMother.nySøknad(periode = periode),
        personopplysningFødselsdato: LocalDate = 1.januar(2000),
    ): Førstegangsbehandling =
        Førstegangsbehandling.opprettBehandling(
            sakId = sakId,
            søknad = søknad,
            fødselsdato = personopplysningFødselsdato,
        )

    fun saksopplysning(
        fom: LocalDate = 1.januar(2023),
        tom: LocalDate = 31.mars(2023),
        kilde: Kilde = Kilde.SAKSB,
        vilkår: Vilkår = Vilkår.AAP,
        type: TypeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
        saksbehandler: String? = null,
    ): Saksopplysning =
        Saksopplysning(
            fom = fom,
            tom = tom,
            kilde = kilde,
            vilkår = vilkår,
            detaljer = "",
            typeSaksopplysning = type,
            saksbehandler = saksbehandler,
        )

    fun behandlingVilkårsvurdertInnvilget(
        periode: Periode = Periode(1.januar(2023), 31.mars(2023)),
        sakId: SakId = SakId.random(),
        søknad: Søknad = ObjectMother.nySøknad(periode = periode),
    ): Førstegangsbehandling {
        val behandling = vilkårViHenter().fold(behandling(periode, sakId, søknad)) { b: Behandling, vilkår ->
            b.leggTilSaksopplysning(
                saksopplysning(
                    fom = periode.fraOgMed,
                    tom = periode.tilOgMed,
                    vilkår = vilkår,
                    type = TypeSaksopplysning.HAR_IKKE_YTELSE,
                ),
            ).behandling as Førstegangsbehandling
        }

        return behandling.vilkårsvurder()
    }

    fun behandlingVilkårsvurdertAvslag(
        periode: Periode = Periode(1.januar(2023), 31.mars(2023)),
        sakId: SakId = SakId.random(),
        søknad: Søknad = ObjectMother.nySøknad(periode = periode),
    ): Førstegangsbehandling {
        val behandling = behandlingVilkårsvurdertInnvilget().leggTilSaksopplysning(
            saksopplysning(
                fom = 1.januar(2023),
                tom = 31.mars(2023),
                vilkår = Vilkår.INSTITUSJONSOPPHOLD,
                type = TypeSaksopplysning.HAR_YTELSE,
            ),
        ).behandling as Førstegangsbehandling

        return behandling.vilkårsvurder()
    }

    fun behandlingTilBeslutterInnvilget(): Førstegangsbehandling =
        behandlingVilkårsvurdertInnvilget().copy(saksbehandler = saksbehandler123().navIdent)
            .tilBeslutting(saksbehandler123())

    fun behandlingTilBeslutterAvslag(): Førstegangsbehandling =
        behandlingVilkårsvurdertAvslag().copy(saksbehandler = saksbehandler123().navIdent)
            .tilBeslutting(saksbehandler123())

    fun behandlingInnvilgetIverksatt(): Førstegangsbehandling =
        behandlingTilBeslutterInnvilget().copy(beslutter = beslutter().navIdent).iverksett(beslutter())

    fun vilkårViHenter() = listOf(
        Vilkår.AAP,
        Vilkår.DAGPENGER,
        Vilkår.PLEIEPENGER_NÆRSTÅENDE,
        Vilkår.PLEIEPENGER_SYKT_BARN,
        Vilkår.FORELDREPENGER,
        Vilkår.OPPLÆRINGSPENGER,
        Vilkår.OMSORGSPENGER,
        Vilkår.ALDER,
        Vilkår.UFØRETRYGD,
        Vilkår.SVANGERSKAPSPENGER,
    )

    fun tiltak(
        id: TiltakId = TiltakId.random(),
        eksternId: String = "TiltakId",
        gjennomføring: Tiltak.Gjennomføring = gruppeAmo(),
        fom: LocalDate = 1.januar(2023),
        tom: LocalDate = 31.mars(2023),
        status: Tiltak.DeltakerStatus = Tiltak.DeltakerStatus(
            status = "DELTAR",
            rettTilÅSøke = true,
        ),
        dagerPrUke: Float? = 2F,
        prosent: Float? = 100F,
        kilde: String = "Komet",
        antallDagerFraSaksbehandler: List<PeriodeMedVerdi<AntallDager>> = emptyList(),
    ) =
        Tiltak(
            id = id,
            eksternId = eksternId,
            gjennomføring = gjennomføring,
            deltakelseFom = fom,
            deltakelseTom = tom,
            deltakelseStatus = status,
            deltakelseProsent = prosent,
            kilde = kilde,
            registrertDato = 1.januarDateTime(2023),
            innhentet = 1.januarDateTime(2023),
            antallDagerSaksopplysninger = AntallDagerSaksopplysninger(
                antallDagerSaksopplysningerFraSBH = antallDagerFraSaksbehandler,
                antallDagerSaksopplysningerFraRegister =
                listOf(
                    antallDagerFraRegister(
                        periode = Periode(
                            fraOgMed = fom,
                            tilOgMed = tom,
                        ),
                    ),
                ),
                avklartAntallDager = emptyList(),
            ),
        )

    fun antallDagerFraRegister(periode: Periode) =
        PeriodeMedVerdi(
            verdi = AntallDager(
                antallDager = 5,
                kilde = Kilde.ARENA,
                saksbehandlerIdent = null,
            ),
            periode = periode,
        )

    fun antallDagerFraSaksbehandler(periode: Periode, saksbehandlerIdent: String = "test") =
        PeriodeMedVerdi(
            verdi = AntallDager(
                antallDager = 5,
                kilde = Kilde.SAKSB,
                saksbehandlerIdent = saksbehandlerIdent,
            ),
            periode = periode,
        )

    fun gruppeAmo() = gjennomføring(typeNavn = "Gruppe AMO", typeKode = "GRUPPEAMO", rettPåTiltakspenger = true)
    fun enkeltAmo() = gjennomføring(typeNavn = "Enkeltplass AMO", typeKode = "ENKELAMO", rettPåTiltakspenger = true)

    fun gjennomføring(
        id: String = "id",
        arrangørnavn: String = "arrangørnavn",
        typeNavn: String = "Gruppe AMO",
        typeKode: String = "GRUPPEAMO",
        rettPåTiltakspenger: Boolean = true,
    ): Tiltak.Gjennomføring =
        Tiltak.Gjennomføring(
            id = id,
            arrangørnavn = arrangørnavn,
            typeNavn = typeNavn,
            typeKode = typeNavn,
            rettPåTiltakspenger = rettPåTiltakspenger,
        )
}
