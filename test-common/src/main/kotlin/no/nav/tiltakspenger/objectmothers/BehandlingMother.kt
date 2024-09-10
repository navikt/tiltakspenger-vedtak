package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.common.TestApplicationContext
import no.nav.tiltakspenger.felles.AttesteringId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.random
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.objectmothers.ObjectMother.beslutter
import no.nav.tiltakspenger.objectmothers.ObjectMother.personSøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler123
import no.nav.tiltakspenger.objectmothers.ObjectMother.søknadTiltak
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Attestering
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Attesteringsstatus
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus.Deltar
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde.Komet
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LeggTilLivsoppholdSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.leggTilLivsoppholdSaksopplysning
import java.time.LocalDate
import java.time.LocalDateTime

interface BehandlingMother {
    /** Felles default vurderingsperiode for testdatatypene */
    fun vurderingsperiode() = Periode(1.januar(2023), 31.mars(2023))

    /**
     * Dette er kun det første steget i en behandlingsprosess. Bruk andre helper-funksjoner for å starte i en senere tilstand.
     */
    fun behandlingUnderBehandlingUavklart(
        periode: Periode = ObjectMother.vurderingsperiode(),
        sakId: SakId = SakId.random(),
        saksnummer: Saksnummer = Saksnummer("202301011001"),
        fnr: Fnr = Fnr.random(),
        søknad: Søknad = ObjectMother.nySøknad(periode = periode),
        personopplysningFødselsdato: LocalDate = 1.januar(2000),
        registrerteTiltak: List<Tiltak> =
            listOf(
                ObjectMother.tiltak(
                    eksternId = søknad.tiltak.id,
                    deltakelseFom = periode.fraOgMed,
                    deltakelseTom = periode.tilOgMed,
                ),
            ),
        saksbehandler: Saksbehandler = ObjectMother.saksbehandler(),
    ): Førstegangsbehandling =
        Førstegangsbehandling
            .opprettBehandling(
                sakId = sakId,
                saksnummer = saksnummer,
                fnr = fnr,
                søknad = søknad,
                fødselsdato = personopplysningFødselsdato,
                saksbehandler = saksbehandler,
                registrerteTiltak = registrerteTiltak,
            ).getOrNull()!!

    fun behandlingUnderBehandlingInnvilget(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        sakId: SakId = SakId.random(),
        søknad: Søknad = ObjectMother.nySøknad(periode = vurderingsperiode),
        saksbehandler: Saksbehandler = ObjectMother.saksbehandler(),
        årsakTilEndring: ÅrsakTilEndring = ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT,
        behandling: Førstegangsbehandling =
            behandlingUnderBehandlingUavklart(
                periode = vurderingsperiode,
                sakId = sakId,
                søknad = søknad,
                saksbehandler = saksbehandler,
            ),
        livsoppholdCommand: LeggTilLivsoppholdSaksopplysningCommand =
            LeggTilLivsoppholdSaksopplysningCommand(
                behandlingId = behandling.id,
                saksbehandler = saksbehandler,
                harYtelseForPeriode =
                LeggTilLivsoppholdSaksopplysningCommand.HarYtelseForPeriode(
                    periode = vurderingsperiode,
                    harYtelse = false,
                ),
                årsakTilEndring = årsakTilEndring,
            ),
    ): Førstegangsbehandling =
        behandlingUnderBehandlingUavklart(
            periode = vurderingsperiode,
            sakId = sakId,
            søknad = søknad,
            saksbehandler = saksbehandler,
        ).leggTilLivsoppholdSaksopplysning(
            command = livsoppholdCommand,
        ).getOrNull()!!

    fun behandlingUnderBehandlingAvslag(
        periode: Periode = ObjectMother.vurderingsperiode(),
        sakId: SakId = SakId.random(),
        søknad: Søknad = ObjectMother.nySøknad(periode = periode),
        saksbehandler: Saksbehandler = ObjectMother.saksbehandler(),
    ): Førstegangsbehandling {
        val behandling =
            behandlingUnderBehandlingUavklart(
                periode = periode,
                sakId = sakId,
                søknad = søknad,
                saksbehandler = saksbehandler,
            )

        behandling.vilkårssett.oppdaterLivsopphold(
            LeggTilLivsoppholdSaksopplysningCommand(
                behandlingId = behandling.id,
                saksbehandler = saksbehandler,
                årsakTilEndring = null,
                harYtelseForPeriode =
                LeggTilLivsoppholdSaksopplysningCommand.HarYtelseForPeriode(
                    periode = behandling.vurderingsperiode,
                    harYtelse = true,
                ),
            ),
        )

        return behandling
    }

    fun godkjentAttestering(beslutter: Saksbehandler = ObjectMother.beslutter()): Attestering =
        Attestering(
            id = AttesteringId.random(),
            status = Attesteringsstatus.GODKJENT,
            begrunnelse = null,
            beslutter = beslutter.navIdent,
            tidspunkt = LocalDateTime.now(),
        )

    fun behandlingTilBeslutterInnvilget(saksbehandler: Saksbehandler = saksbehandler123()): Førstegangsbehandling {
        val behandling = behandlingUnderBehandlingInnvilget(saksbehandler = saksbehandler)
        return behandling.tilBeslutning(saksbehandler)
    }

    fun behandlingTilBeslutterAvslag(): Førstegangsbehandling =
        behandlingUnderBehandlingAvslag()
            .copy(saksbehandler = saksbehandler123().navIdent)
            .tilBeslutning(saksbehandler123())

    fun behandlingInnvilgetIverksatt(): Førstegangsbehandling =
        behandlingTilBeslutterInnvilget(saksbehandler123())
            .copy(beslutter = beslutter().navIdent)
            .iverksett(beslutter(), godkjentAttestering())

    fun tiltak(
        id: TiltakId = TiltakId.random(),
        eksternId: String = "arenaId",
        gjennomføring: Tiltak.Gjennomføring = gruppeAmo(),
        fom: LocalDate = 1.januar(2023),
        tom: LocalDate = 31.mars(2023),
        status: TiltakDeltakerstatus = Deltar,
        dagerPrUke: Float? = 5F,
        prosent: Float? = 100F,
        kilde: Tiltakskilde = Komet,
    ) = Tiltak(
        id = id,
        eksternId = eksternId,
        gjennomføring = gjennomføring,
        deltakelsesperiode = Periode(fom, tom),
        deltakelseStatus = status,
        deltakelseProsent = prosent,
        kilde = kilde,
        registrertDato = 1.januarDateTime(2023),
        antallDagerPerUke = dagerPrUke,
        innhentetTidspunkt = 1.januarDateTime(2023),
    )

    fun gruppeAmo() = gjennomføring(typeNavn = "Gruppe AMO", typeKode = "GRUPPEAMO", rettPåTiltakspenger = true)

    fun gjennomføring(
        id: String = "id",
        arrangørnavn: String = "arrangørnavn",
        typeNavn: String = "Gruppe AMO",
        typeKode: String = "GRUPPEAMO",
        rettPåTiltakspenger: Boolean = true,
        tiltakstype: TiltakstypeSomGirRett = TiltakstypeSomGirRett.ARBEIDSFORBEREDENDE_TRENING,
    ): Tiltak.Gjennomføring =
        Tiltak.Gjennomføring(
            id = id,
            arrangørnavn = arrangørnavn,
            typeNavn = typeNavn,
            typeKode = tiltakstype,
            rettPåTiltakspenger = rettPåTiltakspenger,
        )
}

fun TestApplicationContext.nySøknad(
    periode: Periode = ObjectMother.vurderingsperiode(),
    fnr: Fnr = Fnr.random(),
    fornavn: String = "Fornavn",
    etternavn: String = "Etternavn",
    erSkjermet: Boolean = false,
    personopplysningerFraSøknad: Søknad.Personopplysninger =
        personSøknad(
            fnr = fnr,
            fornavn = fornavn,
            etternavn = etternavn,
        ),
    personopplysningerForBrukerFraPdl: Personopplysninger =
        ObjectMother.personopplysningKjedeligFyr(
            fnr = fnr,
            skjermet = erSkjermet,
        ),
    tiltak: Tiltak = ObjectMother.tiltak(fom = periode.fraOgMed, tom = periode.tilOgMed),
    søknad: Søknad =
        ObjectMother.nySøknad(
            fnr = fnr,
            personopplysninger = personopplysningerFraSøknad,
            tiltak = søknadTiltak(
                id = tiltak.eksternId,
                deltakelseFom = periode.fraOgMed,
                deltakelseTom = periode.tilOgMed,
                arrangør = tiltak.gjennomføring.arrangørnavn,
                typeKode = tiltak.gjennomføring.typeKode.toString(),
                typeNavn = tiltak.gjennomføring.typeNavn,
            ),
        ),
): Søknad {
    this.søknadContext.søknadService.nySøknad(søknad)
    this.leggTilPerson(fnr, erSkjermet, personopplysningerForBrukerFraPdl, tiltak)
    return søknad
}

fun TestApplicationContext.førstegangsbehandlingUavklart(
    periode: Periode = ObjectMother.vurderingsperiode(),
    fnr: Fnr = Fnr.random(),
    saksbehandler: Saksbehandler = saksbehandler(),
    erSkjermet: Boolean = false,
    fødselsdato: LocalDate = 1.januar(2000),
    personopplysningerForBrukerFraPdl: Personopplysninger =
        ObjectMother.personopplysningKjedeligFyr(
            fnr = fnr,
            skjermet = erSkjermet,
            fødselsdato = fødselsdato,
        ),
): Sak {
    val søknad =
        this.nySøknad(
            periode = periode,
            fnr = fnr,
            erSkjermet = erSkjermet,
            personopplysningerForBrukerFraPdl = personopplysningerForBrukerFraPdl,
        )
    return this.sakContext.sakService
        .startFørstegangsbehandling(søknad.id, saksbehandler)
        .getOrNull()!!
}

fun TestApplicationContext.førstegangsbehandlingVilkårsvurdert(
    periode: Periode = ObjectMother.vurderingsperiode(),
    fnr: Fnr = Fnr.random(),
    saksbehandler: Saksbehandler = saksbehandler(),
    erSkjermet: Boolean = false,
): Sak {
    val uavklart =
        førstegangsbehandlingUavklart(
            periode = periode,
            fnr = fnr,
            saksbehandler = saksbehandler,
            erSkjermet = erSkjermet,
        )
    this.førstegangsbehandlingContext.livsoppholdVilkårService.leggTilSaksopplysning(
        LeggTilLivsoppholdSaksopplysningCommand(
            behandlingId = uavklart.førstegangsbehandling.id,
            saksbehandler = saksbehandler,
            harYtelseForPeriode =
            LeggTilLivsoppholdSaksopplysningCommand.HarYtelseForPeriode(
                periode = periode,
                harYtelse = false,
            ),
            årsakTilEndring = ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT,
        ),
    )
    return this.sakContext.sakService.hentForSakId(uavklart.id, saksbehandler)!!
}

fun TestApplicationContext.førstegangsbehandlingTilBeslutter(
    periode: Periode = ObjectMother.vurderingsperiode(),
    fnr: Fnr = Fnr.random(),
    saksbehandler: Saksbehandler = saksbehandler(),
    erSkjermet: Boolean = false,
): Sak {
    val vilkårsvurdert =
        førstegangsbehandlingVilkårsvurdert(
            periode = periode,
            fnr = fnr,
            saksbehandler = saksbehandler,
            erSkjermet = erSkjermet,
        )
    this.førstegangsbehandlingContext.behandlingService.sendTilBeslutter(
        vilkårsvurdert.førstegangsbehandling.id,
        saksbehandler,
    )
    return this.sakContext.sakService.hentForSakId(vilkårsvurdert.id, saksbehandler)!!
}

fun TestApplicationContext.førstegangsbehandlingUnderBeslutning(
    periode: Periode = ObjectMother.vurderingsperiode(),
    fnr: Fnr = Fnr.random(),
    saksbehandler: Saksbehandler = saksbehandler(),
    erSkjermet: Boolean = false,
    beslutter: Saksbehandler = beslutter(),
): Sak {
    val vilkårsvurdert =
        førstegangsbehandlingTilBeslutter(
            periode = periode,
            fnr = fnr,
            saksbehandler = saksbehandler,
            erSkjermet = erSkjermet,
        )
    this.førstegangsbehandlingContext.behandlingService.taBehandling(
        vilkårsvurdert.førstegangsbehandling.id,
        beslutter,
    )
    return this.sakContext.sakService.hentForSakId(vilkårsvurdert.id, saksbehandler)!!
}
