package no.nav.tiltakspenger.domene.sak

import mu.KotlinLogging
import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.behandling.Personopplysninger
import no.nav.tiltakspenger.domene.behandling.Søknad
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

data class Sak(
    val id: SakId,
    val ident: String,
    val saknummer: Saksnummer,
    val periode: Periode,
    val behandlinger: List<Behandling>,
    val personopplysninger: List<Personopplysninger>,
    val vedtak: List<Vedtak>,
) {
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
        fun lagSak(søknad: Søknad, saksnummerGenerator: SaksnummerGenerator): Sak {
            return Sak(
                id = SakId.random(),
                ident = søknad.personopplysninger.ident,
                saknummer = saksnummerGenerator.genererSaknummer(),
                behandlinger = emptyList(),
                periode = søknad.vurderingsperiode(),
                personopplysninger = emptyList(),
                vedtak = emptyList(),
            )
        }
    }
}
