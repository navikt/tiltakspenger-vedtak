package no.nav.tiltakspenger.saksbehandling.domene.sak

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.saksbehandling.domene.behandling.BehandlingOpprettet
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.endringslogg.Endringslogg
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.vilkårsvurder

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

data class Sak(
    val sakDetaljer: SakDetaljer,
    val behandlinger: List<Behandling>,
    val personopplysninger: SakPersonopplysninger,
    val vedtak: List<Vedtak>,
    val endringslogg: Endringslogg = Endringslogg(sakDetaljer.id),
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
                listOf(BehandlingOpprettet.opprettBehandling(sakId = id, søknad = søknad).vilkårsvurder())
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
            behandlinger: List<Behandling>,
            personopplysninger: SakPersonopplysninger,
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

        fun lagSak(søknad: Søknad, saksnummer: Saksnummer): Sak {
            return Sak(
                sakDetaljer = TynnSak(
                    id = SakId.random(),
                    ident = søknad.personopplysninger.ident,
                    saknummer = saksnummer,
                    periode = søknad.vurderingsperiode(),
                ),
                behandlinger = emptyList(),
                personopplysninger = SakPersonopplysninger(),
                vedtak = emptyList(),
            )
        }
    }
}
