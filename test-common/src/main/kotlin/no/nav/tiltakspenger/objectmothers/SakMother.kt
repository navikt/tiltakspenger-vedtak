package no.nav.tiltakspenger.objectmothers

import arrow.core.nonEmptyListOf
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.random
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.objectmothers.ObjectMother.beslutter
import no.nav.tiltakspenger.objectmothers.ObjectMother.godkjentAttestering
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler
import no.nav.tiltakspenger.objectmothers.ObjectMother.søknadTiltak
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.opprettVedtak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LeggTilLivsoppholdSaksopplysningCommand
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LeggTilLivsoppholdSaksopplysningCommand.HarYtelseForPeriode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.leggTilLivsoppholdSaksopplysning
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
        saksbehandler: Saksbehandler = saksbehandler(),
        søknad: Søknad =
            nySøknad(
                tiltak =
                søknadTiltak(
                    deltakelseFom = vurderingsperiode.fraOgMed,
                    deltakelseTom = vurderingsperiode.tilOgMed,
                ),
            ),
        registrerteTiltak: List<Tiltak> =
            listOf(
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
            fødselsdato = fødselsdato,
            saksbehandler = saksbehandler,
            registrerteTiltak = registrerteTiltak,
        ).getOrNull()!!
    }

    fun sakMedInnvilgetVilkårssett(
        sakId: SakId = SakId.random(),
        iDag: LocalDate = LocalDate.of(2023, 1, 1),
        løpenummer: Int = 1001,
        saksnummer: Saksnummer = Saksnummer(iDag, løpenummer),
        saksbehandler: Saksbehandler = saksbehandler(),
        vedtak: List<Rammevedtak> = emptyList(),
    ): Sak {
        return sakMedOpprettetBehandling(
            sakId = sakId,
            saksnummer = saksnummer,
            saksbehandler = saksbehandler,
        ).let {
            val oppdatertFørstegangsbehandling = it.førstegangsbehandling.leggTilLivsoppholdSaksopplysning(
                LeggTilLivsoppholdSaksopplysningCommand(
                    behandlingId = it.førstegangsbehandling.id,
                    saksbehandler = saksbehandler,
                    harYtelseForPeriode = HarYtelseForPeriode(
                        periode = it.førstegangsbehandling.vurderingsperiode,
                        harYtelse = false,
                    ),
                    årsakTilEndring = null,

                ),
            ).getOrNull()!!
            require(it.behandlinger.size == 1)
            it.copy(behandlinger = nonEmptyListOf(oppdatertFørstegangsbehandling))
        }
    }

    fun sakTilBeslutter(
        sakId: SakId = SakId.random(),
        iDag: LocalDate = LocalDate.of(2023, 1, 1),
        løpenummer: Int = 1001,
        saksnummer: Saksnummer = Saksnummer(iDag, løpenummer),
        saksbehandler: Saksbehandler = saksbehandler(),
        beslutter: Saksbehandler = beslutter(),
        vedtak: List<Rammevedtak> = emptyList(),
    ): Sak {
        return sakMedInnvilgetVilkårssett(
            sakId = sakId,
            saksnummer = saksnummer,
            saksbehandler = saksbehandler,
        ).let {
            require(it.behandlinger.size == 1)
            it.copy(behandlinger = nonEmptyListOf(it.førstegangsbehandling.tilBeslutning(saksbehandler).taBehandling(beslutter)))
        }
    }

    fun sakMedRammevedtak(
        sakId: SakId = SakId.random(),
        iDag: LocalDate = LocalDate.of(2023, 1, 1),
        løpenummer: Int = 1001,
        saksnummer: Saksnummer = Saksnummer(iDag, løpenummer),
        saksbehandler: Saksbehandler = saksbehandler(),
        beslutter: Saksbehandler = beslutter(),
    ): Sak {
        return sakTilBeslutter(
            sakId = sakId,
            saksnummer = saksnummer,
            saksbehandler = saksbehandler,
        ).let {
            require(it.behandlinger.size == 1)
            val iverksattBehandling = it.førstegangsbehandling.iverksett(beslutter, godkjentAttestering())
            it.copy(
                behandlinger = nonEmptyListOf(iverksattBehandling),
                vedtak = listOf(iverksattBehandling.opprettVedtak()),
            )
        }
    }
}
