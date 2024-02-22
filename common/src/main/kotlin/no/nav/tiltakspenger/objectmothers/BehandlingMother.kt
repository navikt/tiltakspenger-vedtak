package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Søknad
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.behandling.Tiltak
import no.nav.tiltakspenger.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.domene.vilkår.Vilkår
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.mars
import java.time.LocalDate

interface BehandlingMother {
    fun behandling(
        periode: Periode = Periode(1.januar(2023), 31.mars(2023)),
        sakId: SakId = SakId.random(),
        søknad: Søknad = ObjectMother.nySøknad(periode = periode),
    ): Søknadsbehandling.Opprettet {
        return Søknadsbehandling.Opprettet.opprettBehandling(
            sakId = sakId,
            søknad = søknad,
        )
    }

    fun saksopplysning(
        fom: LocalDate = 1.januar(2023),
        tom: LocalDate = 31.mars(2023),
        kilde: Kilde = Kilde.SAKSB,
        vilkår: Vilkår = Vilkår.AAP,
        type: TypeSaksopplysning = TypeSaksopplysning.HAR_YTELSE,
        saksbehandler: String? = null,
    ) =
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
    ): BehandlingVilkårsvurdert.Innvilget {
        val behandling = vilkårViHenter().fold(behandling(periode, sakId, søknad)) { b: Søknadsbehandling, vilkår ->
            b.leggTilSaksopplysning(
                saksopplysning(
                    vilkår = vilkår,
                    type = TypeSaksopplysning.HAR_IKKE_YTELSE,
                ),
            ).behandling
        } as BehandlingVilkårsvurdert

        return behandling.vurderPåNytt() as BehandlingVilkårsvurdert.Innvilget
    }

    fun behandlingVilkårsvurdertAvslag(
        periode: Periode = Periode(1.januar(2023), 31.mars(2023)),
        sakId: SakId = SakId.random(),
        søknad: Søknad = ObjectMother.nySøknad(periode = periode),
    ): BehandlingVilkårsvurdert.Avslag {
        val behandling = behandlingVilkårsvurdertInnvilget().leggTilSaksopplysning(
            saksopplysning(vilkår = Vilkår.KVP, type = TypeSaksopplysning.HAR_YTELSE),
        ).behandling as BehandlingVilkårsvurdert

        return behandling.vurderPåNytt() as BehandlingVilkårsvurdert.Avslag
    }

    fun behandlingTilBeslutterInnvilget(): BehandlingTilBeslutter.Innvilget {
        return behandlingVilkårsvurdertInnvilget().copy(
            saksbehandler = "123",
        ).tilBeslutting()
    }

    fun behandlingTilBeslutterAvslag(): BehandlingTilBeslutter.Avslag {
        return behandlingVilkårsvurdertAvslag().copy(
            saksbehandler = "123",
        ).tilBeslutting()
    }

    fun behandlingInnvilgetIverksatt(): BehandlingIverksatt.Innvilget {
        return behandlingTilBeslutterInnvilget().copy(beslutter = "beslutter").iverksett()
    }

    fun vilkårViHenter() = listOf(
        Vilkår.AAP,
        Vilkår.DAGPENGER,
        Vilkår.PLEIEPENGER_NÆRSTÅENDE,
        Vilkår.PLEIEPENGER_SYKT_BARN,
        Vilkår.FORELDREPENGER,
        Vilkår.OPPLÆRINGSPENGER,
        Vilkår.OMSORGSPENGER,
        Vilkår.ALDER,
        Vilkår.TILTAKSPENGER,
        Vilkår.UFØRETRYGD,
        Vilkår.SVANGERSKAPSPENGER,
    )

    fun tiltak(
        id: String = "TiltakId",
        gjennomføring: Tiltak.Gjennomføring = gruppeAmo(),
        fom: LocalDate = 1.januar(2023),
        tom: LocalDate = 31.mars(2023),
        status: Tiltak.DeltakerStatus = Tiltak.DeltakerStatus(
            status = "DELTAR",
            rettTilÅASøke = true,
        ),
        dagerPrUke: Float? = 2F,
        prosent: Float? = 100F,
        kilde: String = "Komet",
    ) =
        Tiltak(
            id = id,
            gjennomføring = gjennomføring,
            deltakelseFom = fom,
            deltakelseTom = tom,
            deltakelseStatus = status,
            deltakelseDagerUke = dagerPrUke,
            deltakelseProsent = prosent,
            kilde = kilde,
            registrertDato = 1.januarDateTime(2023),
            innhentet = 1.januarDateTime(2023),
        )

    fun gruppeAmo() = gjennomføring(typeNavn = "Gruppe AMO", typeKode = "GRUPPEAMO", rettPåTiltakspenger = true)
    fun enkeltAmo() = gjennomføring(typeNavn = "Enkeltplass AMO", typeKode = "ENKELAMO", rettPåTiltakspenger = true)

    fun gjennomføring(
        id: String = "id",
        arrangørnavn: String = "arrangørnavn",
        typeNavn: String = "Gruppe AMO",
        typeKode: String = "GRUPPEAMO",
        rettPåTiltakspenger: Boolean = true,
    ) =
        Tiltak.Gjennomføring(
            id = id,
            arrangørnavn = arrangørnavn,
            typeNavn = typeNavn,
            typeKode = typeNavn,
            rettPåTiltakspenger = rettPåTiltakspenger,
            fom = null,
            tom = null,
        )
}
