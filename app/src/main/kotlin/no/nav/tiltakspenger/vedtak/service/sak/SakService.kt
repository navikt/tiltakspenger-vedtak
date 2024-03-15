package no.nav.tiltakspenger.vedtak.service.sak

import no.nav.tiltakspenger.domene.behandling.Søknad
import no.nav.tiltakspenger.domene.personopplysninger.SakPersonopplysninger
import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.innsending.Skjerming

interface SakService {
    fun motta(søknad: Søknad): Sak
    fun mottaPersonopplysninger(journalpostId: String, nyePersonopplysninger: SakPersonopplysninger): Sak?
    fun mottaSkjerming(journalpostId: String, skjerming: Skjerming): Sak
    fun hentMedBehandlingIdOrNull(behandlingId: BehandlingId): Sak?
    fun hentMedBehandlingId(behandlingId: BehandlingId, saksbehandler: Saksbehandler): Sak
}
