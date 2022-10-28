package no.nav.tiltakspenger.vedtak.service

import no.nav.tiltakspenger.vedtak.repository.SøkerRepository
import no.nav.tiltakspenger.vedtak.routes.person.BehandlingDTO
import no.nav.tiltakspenger.vedtak.routes.person.PersonMapper

class PersonServiceImpl(
    val søkerRepository: SøkerRepository
) : PersonService {

    override fun hentSøkerOgSøknader(ident: String): SøkerDTO? {
        val søker = søkerRepository.hent(ident) ?: return null
        return SøkerDTO(
            ident = søker.ident,
            søknader = søker.søknader.map {
                SøknadDTO(
                    søknadId = it.søknadId,
                    arrangoernavn = it.tiltak.arrangoernavn ?: "Ukjent",
                    tiltakskode = it.tiltak.tiltakskode?.navn ?: "Ukjent",
                    startdato = it.tiltak.startdato,
                    sluttdato = it.tiltak.sluttdato,
                )
            }
        )
    }

    override fun hentSøknad(søknadId: String): BehandlingDTO? {
        val mapper = PersonMapper()
        return søkerRepository.findBySøknadId(søknadId)?.let { mapper.mapPerson(it, søknadId) }
    }
}
