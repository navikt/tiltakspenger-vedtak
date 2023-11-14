package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.vedtak.Søknad

interface BehandlingMother {
    fun behandling(
        periode: Periode = Periode(1.januar(2023), 31.mars(2023)),
        sakId: SakId = SakId.random(),
        søknad: Søknad = ObjectMother.nySøknad(periode = periode),
    ): Søknadsbehandling.Opprettet {
        return Søknadsbehandling.Opprettet.opprettBehandling(
            sakId = sakId,
            søknad = søknad,
        )
    }

    fun behandlingVilkårsvurdert(
        periode: Periode = Periode(1.januar(2023), 31.mars(2023)),
        sakId: SakId = SakId.random(),
        søknad: Søknad = ObjectMother.nySøknad(periode = periode),
    ): BehandlingVilkårsvurdert {
        return behandling(periode, sakId, søknad).vilkårsvurder()
    }
}
