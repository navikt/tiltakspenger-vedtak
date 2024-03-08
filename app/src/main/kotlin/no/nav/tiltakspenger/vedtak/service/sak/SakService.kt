package no.nav.tiltakspenger.vedtak.service.sak

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.innsending.Skjerming
import no.nav.tiltakspenger.saksbehandling.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.sak.Sak

interface SakService {
    fun motta(søknad: Søknad): Sak
    fun mottaPersonopplysninger(journalpostId: String, personopplysninger: List<Personopplysninger>): Sak?
    fun mottaSkjerming(journalpostId: String, skjerming: Skjerming): Sak
    fun henteMedBehandlingsId(behandlingId: BehandlingId): Sak?
}
