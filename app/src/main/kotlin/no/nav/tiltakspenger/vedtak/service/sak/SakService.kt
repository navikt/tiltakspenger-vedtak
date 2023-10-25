package no.nav.tiltakspenger.vedtak.service.sak

import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.Skjerming
import no.nav.tiltakspenger.vedtak.Søknad

interface SakService {
    fun motta(søknad: Søknad): Sak
    fun mottaPersonopplysninger(journalpostId: String, personopplysninger: List<Personopplysninger>): Sak?
    fun mottaSkjerming(journalpostId: String, skjerming: Skjerming): Sak
    fun henteMedBehandlingsId(behandlingId: BehandlingId): Sak?
}
