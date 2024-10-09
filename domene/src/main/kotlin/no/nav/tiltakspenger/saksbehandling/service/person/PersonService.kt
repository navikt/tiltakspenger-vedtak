package no.nav.tiltakspenger.saksbehandling.service.person

import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.EnkelPerson
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Navn
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.ports.PersonGateway
import no.nav.tiltakspenger.saksbehandling.ports.PersonRepo

class PersonService(
    private val personRepo: PersonRepo,
    private val personClient: PersonGateway,
) {

    fun hentFnrForBehandlingId(behandlingId: BehandlingId): Fnr =
        personRepo.hentFnrForBehandlingId(behandlingId)
            ?: throw IkkeFunnetException("Fant ikke fnr på behandlingId: $behandlingId")

    fun hentFnrForSakId(sakId: SakId): Fnr =
        personRepo.hentFnrForSakId(sakId)
            ?: throw IkkeFunnetException("Fant ikke fnr på sakId: $sakId")

    fun hentFnrForSaksnummer(saksnummer: Saksnummer): Fnr =
        personRepo.hentFnrForSaksnummer(saksnummer)
            ?: throw IkkeFunnetException("Fant ikke fnr for saksnummer: $saksnummer")

    fun hentFnrForVedtakId(vedtakId: VedtakId): Fnr =
        personRepo.hentFnrForVedtakId(vedtakId)
            ?: throw IkkeFunnetException("Fant ikke fnr for vedtakId: $vedtakId")

    fun hentFnrForMeldekortId(meldekortId: MeldekortId): Fnr =
        personRepo.hentFnrForMeldekortId(meldekortId)
            ?: throw IkkeFunnetException("Fant ikke fnr på meldekortId: $meldekortId")

    fun hentFnrForSøknadId(søknadId: SøknadId): Fnr =
        personRepo.hentFnrForSøknadId(søknadId)
            ?: throw IkkeFunnetException("Fant ikke fnr på søknadId: søknadId")

    suspend fun hentEnkelPersonForFnr(fnr: Fnr): EnkelPerson = personClient.hentEnkelPerson(fnr)

    suspend fun hentNavn(fnr: Fnr): Navn {
        personClient.hentEnkelPerson(fnr).let {
            return Navn(it.fornavn, it.mellomnavn, it.etternavn)
        }
    }

    suspend fun hentPersonopplysninger(fnr: Fnr): PersonopplysningerSøker {
        return personClient.hentPerson(fnr)
    }
}
