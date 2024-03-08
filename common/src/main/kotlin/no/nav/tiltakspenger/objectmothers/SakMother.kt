package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personSøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.søknadTiltak
import no.nav.tiltakspenger.saksbehandling.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.behandling.Søknadsbehandling
import no.nav.tiltakspenger.saksbehandling.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.sak.Sak
import no.nav.tiltakspenger.saksbehandling.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.sak.SaksnummerGenerator
import java.util.Random

interface SakMother {
    fun sakMedOpprettetBehandling(
        id: SakId = SakId.random(),
        ident: String = Random().nextInt().toString(),
        saksnummer: Saksnummer = Saksnummer("saksnr"),
        periode: Periode = Periode(fra = 1.januar(2023), til = 31.januar(2023)),
        behandlinger: List<Søknadsbehandling> = listOf(
            Søknadsbehandling.Opprettet.opprettBehandling(
                id,
                nySøknad(
                    personopplysninger = personSøknad(ident = ident),
                    tiltak = søknadTiltak(
                        deltakelseFom = periode.fra,
                        deltakelseTom = periode.til,
                    ),
                ),
            ),
        ),
        personopplysninger: List<Personopplysninger> = listOf(personopplysningKjedeligFyr(ident = ident)),
    ): Sak =
        Sak(
            id = id,
            ident = ident,
            saknummer = saksnummer,
            periode = periode,
            behandlinger = behandlinger,
            personopplysninger = personopplysninger,
            vedtak = emptyList(),
        )

    fun nySakFraSøknad(
        søknad: Søknad,
        saksnummerGenerator: SaksnummerGenerator,
    ): Sak {
        return Sak.lagSak(
            søknad = søknad,
            saksnummerGenerator = saksnummerGenerator,
        )
    }

    fun tomSak(
        id: SakId = SakId.random(),
        ident: String = Random().nextInt().toString(),
        saksnummer: Saksnummer = Saksnummer("saksnr"),
        periode: Periode = Periode(fra = 1.januar(2022), til = 31.januar(2022)),
        behandlinger: List<Søknadsbehandling> = emptyList(),
        personopplysninger: List<Personopplysninger> = emptyList(),
    ): Sak =
        Sak(
            id = id,
            ident = ident,
            saknummer = saksnummer,
            periode = periode,
            behandlinger = behandlinger,
            personopplysninger = personopplysninger,
            vedtak = emptyList(),
        )
}
