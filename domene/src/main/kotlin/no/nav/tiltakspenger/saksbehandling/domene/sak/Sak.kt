package no.nav.tiltakspenger.saksbehandling.domene.sak

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.exceptions.TilgangException
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.Rolle
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.domene.Meldeperioder
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.KanIkkeOppretteBehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak
import java.time.LocalDate

data class Sak(
    val id: SakId,
    val fnr: Fnr,
    val saksnummer: Saksnummer,
    val behandlinger: NonEmptyList<Behandling>,
    // TODO pre-mvp: Endre til val rammevedtak: Rammevedtak? siden vi kun har et rammevedtak per sak.
    val vedtak: List<Rammevedtak>,
    val meldekort: Meldeperioder,
) {
    init {
        if (behandlinger.isNotEmpty()) {
            require(behandlinger.first() is Førstegangsbehandling) { "Første behandlingen må være en førstegangsbehandling" }
        }
        require(behandlinger.filterIsInstance<Førstegangsbehandling>().size <= 1) { "Kan ikke ha flere enn en førstegangsbehandling" }
    }

    /**
     * En sak kan kun ha en førstegangsbehandling, dersom perioden til den vedtatte førstegangsbehandlingen skal utvides eller minskes (den må fortsatt være sammenhengende) må vi revurdere/omgjøre, ikke førstegangsbehandle på nytt.
     * Dersom den nye søknaden ikke overlapper eller tilstøter den gamle perioden, må vi opprette en ny sak som får en ny førstegangsbehandling.
     */
    val førstegangsbehandling: Førstegangsbehandling =
        behandlinger.filterIsInstance<Førstegangsbehandling>().single()

    fun hentMeldekort(meldekortId: MeldekortId): Meldekort? {
        return meldekort.hentMeldekort(meldekortId)
    }

    fun hentRammevedtak(): Rammevedtak? {
        return vedtak.lastOrNull()
    }

    fun hentVedtaksperiode(): Periode {
        return vedtak.first().periode
    }

    companion object {
        fun lagSak(
            sakId: SakId = SakId.random(),
            saksnummer: Saksnummer,
            søknad: Søknad,
            fødselsdato: LocalDate,
            saksbehandler: Saksbehandler,
            registrerteTiltak: List<Tiltak>,
        ): Either<KanIkkeOppretteBehandling, Sak> {
            if (!saksbehandler.roller.harRolle(Rolle.SAKSBEHANDLER)) {
                throw TilgangException("Saksbehandler ${saksbehandler.navIdent} må ha rollen SAKSBEHANDLER/BESLUTTER. søknadId: ${søknad.id} roller: ${saksbehandler.roller}")
            }
            val fnr = søknad.fnr
            val førstegangsbehandling =
                Førstegangsbehandling
                    .opprettBehandling(
                        sakId = sakId,
                        saksnummer = saksnummer,
                        fnr = fnr,
                        søknad = søknad,
                        fødselsdato = fødselsdato,
                        saksbehandler = saksbehandler,
                        registrerteTiltak = registrerteTiltak,
                    ).getOrElse { return it.left() }
            return Sak(
                id = sakId,
                fnr = fnr,
                saksnummer = saksnummer,
                behandlinger = nonEmptyListOf(førstegangsbehandling),
                vedtak = emptyList(),
                meldekort = Meldeperioder.empty(førstegangsbehandling.tiltakstype),
            ).right()
        }
    }
}
