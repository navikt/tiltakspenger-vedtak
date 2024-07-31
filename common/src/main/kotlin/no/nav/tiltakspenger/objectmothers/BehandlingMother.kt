package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.mars
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
    fun behandling(
        periode: Periode = Periode(1.januar(2023), 31.mars(2023)),
        sakId: SakId = SakId.random(),
        saksnummer: Saksnummer = Saksnummer("202301011001"),
        ident: String = "12345678910",
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
    ): Førstegangsbehandling =
        Førstegangsbehandling.opprettBehandling(
            sakId = sakId,
            saksnummer = saksnummer,
            fnr = ident,
            søknad = søknad,
            fødselsdato = personopplysningFødselsdato,
            saksbehandler = saksbehandler,
            registrerteTiltak = registrerteTiltak,
        )

    fun behandlingPåbegyntInnvilget(
        periode: Periode = Periode(1.januar(2023), 31.mars(2023)),
        sakId: SakId = SakId.random(),
        søknad: Søknad = ObjectMother.nySøknad(periode = periode),
        saksbehandler: Saksbehandler = ObjectMother.saksbehandler(),
    ): Førstegangsbehandling {
        return behandling(
            periode = periode,
            sakId = sakId,
            søknad = søknad,
            saksbehandler = saksbehandler,
        )
    }

    fun behandlingPåbegyntAvslag(
        periode: Periode = Periode(1.januar(2023), 31.mars(2023)),
        sakId: SakId = SakId.random(),
        søknad: Søknad = ObjectMother.nySøknad(periode = periode),
        saksbehandler: Saksbehandler = ObjectMother.saksbehandler(),
    ): Førstegangsbehandling {
        val behandling =
            behandling(
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

    fun behandlingKlarTilAttestering(saksbehandler: Saksbehandler): Førstegangsbehandling {
        val behandling = behandlingPåbegyntInnvilget(saksbehandler = saksbehandler)

        return behandling.leggTilLivsoppholdSaksopplysning(
            command = LeggTilLivsoppholdSaksopplysningCommand(
                behandlingId = behandling.id,
                saksbehandler = saksbehandler,
                harYtelseForPeriode = LeggTilLivsoppholdSaksopplysningCommand.HarYtelseForPeriode(
                    periode = behandling.vurderingsperiode,
                    harYtelse = false,
                ),
                årsakTilEndring = ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT,
            ),
        ).getOrNull()!!
    }

    fun behandlingTilBeslutterInnvilget(saksbehandler: Saksbehandler): Førstegangsbehandling {
        val behandling = behandlingKlarTilAttestering(saksbehandler = saksbehandler)
        return behandling.tilBesluting(saksbehandler)
    }

    fun behandlingTilBeslutterAvslag(): Førstegangsbehandling =
        behandlingPåbegyntAvslag().copy(saksbehandler = saksbehandler123().navIdent)
            .tilBesluting(saksbehandler123())

    fun behandlingInnvilgetIverksatt(): Førstegangsbehandling =
        behandlingTilBeslutterInnvilget(saksbehandler123()).copy(beslutter = beslutter().navIdent).iverksett(beslutter())

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
