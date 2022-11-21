package no.nav.tiltakspenger.vedtak.service.søker

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository

class SøkerServiceImpl(
    val søkerRepository: SøkerRepository,
    private val behandlingMapper: BehandlingMapper = BehandlingMapper()
) : SøkerService {
    override fun hentSøkerId(ident: String, saksbehandler: Saksbehandler): SøkerIdDTO? {
        val søker = søkerRepository.hent(ident) ?: return null
        søker.sjekkOmSaksbehandlerHarTilgang(saksbehandler)

        return SøkerIdDTO(
            id = søker.id.toString(),
        )
    }

    override fun hentSøkerOgSøknader(søkerId: SøkerId, saksbehandler: Saksbehandler): SøkerDTO? {
        val søker = søkerRepository.hentBySøkerId(søkerId) ?: return null
        søker.sjekkOmSaksbehandlerHarTilgang(saksbehandler)

        return SøkerDTO(
            ident = søker.ident,
            behandlinger = behandlingMapper.mapSøkerMedSøknad(søker),
            personopplysninger = søker.personopplysningerSøker()?.let {
                PersonopplysningerDTO(
                    fornavn = it.fornavn,
                    etternavn = it.etternavn,
                    ident = it.ident,
                    barn = listOf()
                )
            }
        )
    }
}
