package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.domene.behandling.BehandlingOpprettet
import no.nav.tiltakspenger.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.domene.behandling.Søknad
import no.nav.tiltakspenger.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.domene.sak.Saksnummer
import no.nav.tiltakspenger.domene.sak.SaksnummerGenerator
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personSøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.søknadTiltak
import java.util.Random

interface SakMother {
    fun sakMedOpprettetBehandling(
        id: SakId = SakId.random(),
        ident: String = Random().nextInt().toString(),
        saksnummer: Saksnummer = Saksnummer("saksnr"),
        periode: Periode = Periode(fra = 1.januar(2023), til = 31.januar(2023)),
        behandlinger: List<Førstegangsbehandling> = listOf(
            BehandlingOpprettet.opprettBehandling(
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
            saksnummerGenerator = saksnummerGenerator,
        )
    }

    fun tomSak(
        id: SakId = SakId.random(),
        ident: String = Random().nextInt().toString(),
        saksnummer: Saksnummer = Saksnummer("saksnr"),
        periode: Periode = Periode(fra = 1.januar(2022), til = 31.januar(2022)),
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
