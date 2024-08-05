package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.random
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.objectmothers.ObjectMother.beslutter
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler123
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.behandling.stønadsdager.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.stønadsdager.AntallDagerSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LeggTilLivsoppholdSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.leggTilLivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.Tiltak
import java.time.LocalDate

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
        registrerteTiltak: List<Tiltak> = listOf(
            ObjectMother.tiltak(
                eksternId = søknad.tiltak.id,
                deltakelseFom = periode.fraOgMed,
                deltakelseTom = periode.tilOgMed,
            ),
        ),
        saksbehandler: Saksbehandler = ObjectMother.saksbehandler(),
    ): Førstegangsbehandling {
        return Førstegangsbehandling.opprettBehandling(
            sakId = sakId,
            saksnummer = saksnummer,
            fnr = fnr,
            søknad = søknad,
            fødselsdato = personopplysningFødselsdato,
            saksbehandler = saksbehandler,
            registrerteTiltak = registrerteTiltak,
        ).getOrNull()!!
    }

    fun behandlingUnderBehandlingInnvilget(
        vurderingsperiode: Periode = ObjectMother.vurderingsperiode(),
        sakId: SakId = SakId.random(),
        søknad: Søknad = ObjectMother.nySøknad(periode = vurderingsperiode),
        saksbehandler: Saksbehandler = ObjectMother.saksbehandler(),
        årsakTilEndring: ÅrsakTilEndring = ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT,
        behandling: Førstegangsbehandling = behandlingUnderBehandlingUavklart(
            periode = vurderingsperiode,
            sakId = sakId,
            søknad = søknad,
            saksbehandler = saksbehandler,
        ),
        livsoppholdCommand: LeggTilLivsoppholdSaksopplysningCommand = LeggTilLivsoppholdSaksopplysningCommand(
            behandlingId = behandling.id,
            saksbehandler = saksbehandler,
            harYtelseForPeriode = LeggTilLivsoppholdSaksopplysningCommand.HarYtelseForPeriode(
                periode = vurderingsperiode,
                harYtelse = false,
            ),
            årsakTilEndring = årsakTilEndring,
        ),
    ): Førstegangsbehandling {
        return behandlingUnderBehandlingUavklart(
            periode = vurderingsperiode,
            sakId = sakId,
            søknad = søknad,
            saksbehandler = saksbehandler,
        ).leggTilLivsoppholdSaksopplysning(
            command = livsoppholdCommand,
        ).getOrNull()!!
    }

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
                harYtelseForPeriode = LeggTilLivsoppholdSaksopplysningCommand.HarYtelseForPeriode(
                    periode = behandling.vurderingsperiode,
                    harYtelse = true,
                ),
            ),
        )

        return behandling
    }

    fun behandlingTilBeslutterInnvilget(saksbehandler: Saksbehandler): Førstegangsbehandling {
        val behandling = behandlingUnderBehandlingInnvilget(saksbehandler = saksbehandler)
        return behandling.tilBeslutning(saksbehandler)
    }

    fun behandlingTilBeslutterAvslag(): Førstegangsbehandling =
        behandlingUnderBehandlingAvslag().copy(saksbehandler = saksbehandler123().navIdent)
            .tilBeslutning(saksbehandler123())

    fun behandlingInnvilgetIverksatt(): Førstegangsbehandling =
        behandlingTilBeslutterInnvilget(saksbehandler123()).copy(beslutter = beslutter().navIdent)
            .iverksett(beslutter())

    fun tiltak(
        id: TiltakId = TiltakId.random(),
        eksternId: String = "arenaId",
        gjennomføring: Tiltak.Gjennomføring = gruppeAmo(),
        fom: LocalDate = 1.januar(2023),
        tom: LocalDate = 31.mars(2023),
        status: String = "DELTAR",
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

    fun gruppeAmo() = gjennomføring(typeNavn = "Gruppe AMO", typeKode = "GRUPPEAMO", rettPåTiltakspenger = true)

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
