package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personSøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.søknadTiltak
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.sak.SaksnummerGenerator
import java.time.LocalDate
import java.util.Random

interface SakMother {

    companion object {
        private val random = Random()
    }

    fun sakMedOpprettetBehandling(
        id: SakId = SakId.random(),
        ident: String = random.nextInt().toString(),
        saksnummer: Saksnummer = Saksnummer("saksnr"),
        periode: Periode = Periode(fraOgMed = 1.januar(2023), tilOgMed = 31.januar(2023)),
        personopplysningFødselsdato: LocalDate = 1.januar(2000),
        behandlinger: List<Førstegangsbehandling> = listOf(
            Førstegangsbehandling.opprettBehandling(
                id,
                nySøknad(
                    personopplysninger = personSøknad(ident = ident),
                    tiltak = søknadTiltak(
                        deltakelseFom = periode.fraOgMed,
                        deltakelseTom = periode.tilOgMed,
                    ),
                ),
                fødselsdato = personopplysningFødselsdato,
            ),
        ),
        personopplysninger: SakPersonopplysninger = SakPersonopplysninger(listOf(personopplysningKjedeligFyr(ident = ident))),
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
            saksnummer = saksnummerGenerator.genererSaknummer("TODO"),
            sakPersonopplysninger = SakPersonopplysninger(listOf(personopplysningKjedeligFyr())),
        )
    }

    fun tomSak(
        id: SakId = SakId.random(),
        ident: String = random.nextInt().toString(),
        saksnummer: Saksnummer = Saksnummer("saksnr"),
        periode: Periode = Periode(fraOgMed = 1.januar(2022), tilOgMed = 31.januar(2022)),
        behandlinger: List<Førstegangsbehandling> = emptyList(),
        personopplysninger: SakPersonopplysninger = SakPersonopplysninger(),
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
