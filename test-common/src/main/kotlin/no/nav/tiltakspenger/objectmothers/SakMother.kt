package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.random
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.personopplysningKjedeligFyr
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler
import no.nav.tiltakspenger.objectmothers.ObjectMother.søknadTiltak
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import java.time.LocalDate

interface SakMother {

    fun sakMedOpprettetBehandling(
        sakId: SakId = SakId.random(),
        fnr: Fnr = Fnr.random(),
        iDag: LocalDate = LocalDate.of(2023, 1, 1),
        løpenummer: Int = 1001,
        saksnummer: Saksnummer = Saksnummer(iDag, løpenummer),
        vurderingsperiode: Periode = Periode(fraOgMed = 1.januar(2023), tilOgMed = 31.januar(2023)),
        fødselsdato: LocalDate = ObjectMother.fødselsdato(),
        sakPersonopplysninger: SakPersonopplysninger = SakPersonopplysninger(
            listOf(
                personopplysningKjedeligFyr(
                    fnr = fnr,
                    fødselsdato = fødselsdato,
                ),
            ),
        ),
        søknadPersonopplysninger: Søknad.Personopplysninger = Søknad.Personopplysninger(
            fnr = fnr,
            fornavn = sakPersonopplysninger.søker().fornavn,
            etternavn = sakPersonopplysninger.søker().etternavn,
        ),

        saksbehandler: Saksbehandler = saksbehandler(),
        søknad: Søknad = nySøknad(
            personopplysninger = søknadPersonopplysninger,
            tiltak = søknadTiltak(
                deltakelseFom = vurderingsperiode.fraOgMed,
                deltakelseTom = vurderingsperiode.tilOgMed,
            ),
        ),
        registrerteTiltak: List<Tiltak> = listOf(
            ObjectMother.tiltak(
                eksternId = søknad.tiltak.id,
                deltakelseFom = vurderingsperiode.fraOgMed,
                deltakelseTom = vurderingsperiode.tilOgMed,
            ),
        ),
    ): Sak {
        return Sak.lagSak(
            sakId = sakId,
            søknad = søknad,
            saksnummer = saksnummer,
            sakPersonopplysninger = sakPersonopplysninger,
            saksbehandler = saksbehandler,
            registrerteTiltak = registrerteTiltak,
        ).getOrNull()!!
    }
}
