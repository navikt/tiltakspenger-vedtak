package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.domene.sak.Saksnummer
import no.nav.tiltakspenger.domene.sak.SaksnummerGenerator
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.objectmothers.ObjectMother.arenaTiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknadMedTiltak
import no.nav.tiltakspenger.objectmothers.ObjectMother.personSøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.Søknad
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
                nySøknadMedTiltak(
                    personopplysninger = personSøknad(ident = ident),
                    tiltak = arenaTiltak(
                        opprinneligStartdato = periode.fra,
                        opprinneligSluttdato = periode.til,
                        startdato = periode.fra,
                        sluttdato = periode.til,
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
        )
}