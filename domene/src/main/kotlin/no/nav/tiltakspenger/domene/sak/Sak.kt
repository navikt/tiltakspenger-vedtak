package no.nav.tiltakspenger.domene.sak

import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.domene.behandling.Personopplysninger
import no.nav.tiltakspenger.domene.behandling.Søknad
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId

data class Sak(
    val id: SakId,
    val ident: String,
    val saknummer: Saksnummer,
    val periode: Periode,
    val behandlinger: List<Behandling>,
    val personopplysninger: List<Personopplysninger>,
//    val vedtak: List<Vedtak>,
) {
    fun håndter(søknad: Søknad): Sak {
        val behandlinger =
            behandlinger.filterIsInstance<Søknadsbehandling.Opprettet>().firstOrNull()?.let { behandling ->
                listOf(
                    behandling.copy(
                        søknader = behandling.søknader + søknad,
                    ).let {
                        it.vilkårsvurder()
                    },
                )
            } ?: listOf(
                Søknadsbehandling.Opprettet.opprettBehandling(sakId = id, søknad = søknad)
                    .let {
                        it.vilkårsvurder()
                    },
            )

        return this.copy(
            behandlinger = behandlinger,
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
            )
        }
    }
}
