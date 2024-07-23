package no.nav.tiltakspenger.saksbehandling.domene.sak

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.Tiltak

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

data class Sak(
    val sakDetaljer: SakDetaljer,
    val behandlinger: List<Behandling>,
    val personopplysninger: SakPersonopplysninger,
    val vedtak: List<Vedtak>,
) : SakDetaljer by sakDetaljer {

    init {
        if (behandlinger.isNotEmpty()) {
            require(behandlinger.first() is Førstegangsbehandling) { "Første behandlingen må være en førstegangsbehandling" }
        }
        require(behandlinger.filterIsInstance<Førstegangsbehandling>().size <= 1) { "Kan ikke ha flere enn en førstegangsbehandling" }
    }

    val førstegangsbehandling: Førstegangsbehandling? =
        behandlinger.filterIsInstance<Førstegangsbehandling>().firstOrNull()

    /**
     * Saksbehandler vil også ta behandlingen
     */
    fun nyFørstegangsbehandling(
        søknad: Søknad,
        saksbehandler: Saksbehandler,
        registrerteTiltak: List<Tiltak>,
    ): Sak {
        require(behandlinger.isEmpty()) { "Sak har allerede en behandling" }
        require(saksbehandler.isSaksbehandler())
        require(personopplysninger.harTilgang(saksbehandler))
        return this.copy(
            behandlinger = listOf(
                Førstegangsbehandling.opprettBehandling(
                    sakId = id,
                    saksnummer = saksnummer,
                    ident = ident,
                    søknad = søknad,
                    fødselsdato = personopplysninger.søker().fødselsdato,
                    saksbehandler = saksbehandler,
                    registrerteTiltak = registrerteTiltak,
                ),
            ),
        )
    }

    /**
     * Sjekker kode 6, 7 og skjermet
     */
    fun harTilgang(saksbehandler: Saksbehandler): Boolean {
        return personopplysninger.harTilgang(saksbehandler)
    }

    companion object {
        operator fun invoke(
            id: SakId,
            ident: String,
            saknummer: Saksnummer,
            periode: Periode,
            behandlinger: List<Behandling>,
            personopplysninger: SakPersonopplysninger,
            vedtak: List<Vedtak>,
        ): Sak =
            Sak(
                sakDetaljer = TynnSak(
                    id = id,
                    ident = ident,
                    saksnummer = saknummer,
                    periode = periode,
                ),
                behandlinger = behandlinger,
                personopplysninger = personopplysninger,
                vedtak = vedtak,
            )

        fun lagSak(
            søknad: Søknad,
            saksnummer: Saksnummer,
            sakPersonopplysninger: SakPersonopplysninger,
        ): Sak {
            return Sak(
                sakDetaljer = TynnSak(
                    id = SakId.random(),
                    ident = søknad.personopplysninger.ident,
                    saksnummer = saksnummer,
                    periode = søknad.vurderingsperiode(),
                ),
                behandlinger = emptyList(),
                personopplysninger = sakPersonopplysninger,
                vedtak = emptyList(),
            )
        }
    }
}
