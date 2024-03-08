package no.nav.tiltakspenger.saksbehandling.sak

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.saksbehandling.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.behandling.Søknadsbehandling
import no.nav.tiltakspenger.saksbehandling.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.vedtak.Vedtak

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

data class Sak(
    val sakDetaljer: SakDetaljer,
    val behandlinger: List<no.nav.tiltakspenger.saksbehandling.behandling.Behandling>,
    val personopplysninger: List<Personopplysninger>,
    val vedtak: List<Vedtak>,
) : SakDetaljer by sakDetaljer {
    fun håndter(søknad: Søknad): Sak {
        val iverksatteBehandlinger = behandlinger.filterIsInstance<BehandlingIverksatt>()
        val behandlinger = behandlinger
            .filterNot { it is BehandlingIverksatt }
            .map {
                try {
                    it.leggTilSøknad(søknad)
                } catch (e: IllegalStateException) {
                    if (e.message?.contains("Kan ikke legge til søknad på denne behandlingen") == true) {
                        it
                    } else {
                        throw e
                    }
                }
            }.ifEmpty {
                listOf(Søknadsbehandling.Opprettet.opprettBehandling(sakId = id, søknad = søknad).vilkårsvurder())
            }

        return this.copy(
            behandlinger = behandlinger + iverksatteBehandlinger,
        )
    }

    companion object {
        operator fun invoke(
            id: SakId,
            ident: String,
            saknummer: Saksnummer,
            periode: Periode,
            behandlinger: List<no.nav.tiltakspenger.saksbehandling.behandling.Behandling>,
            personopplysninger: List<Personopplysninger>,
            vedtak: List<Vedtak>,
        ): Sak =
            Sak(
                sakDetaljer = TynnSak(
                    id = id,
                    ident = ident,
                    saknummer = saknummer,
                    periode = periode,
                ),
                behandlinger = behandlinger,
                personopplysninger = personopplysninger,
                vedtak = vedtak,
            )

        fun lagSak(søknad: Søknad, saksnummerGenerator: SaksnummerGenerator): Sak {
            return Sak(
                sakDetaljer = TynnSak(
                    id = SakId.random(),
                    ident = søknad.personopplysninger.ident,
                    saknummer = saksnummerGenerator.genererSaknummer(),
                    periode = søknad.vurderingsperiode(),
                ),
                behandlinger = emptyList(),
                personopplysninger = emptyList(),
                vedtak = emptyList(),
            )
        }
    }
}
