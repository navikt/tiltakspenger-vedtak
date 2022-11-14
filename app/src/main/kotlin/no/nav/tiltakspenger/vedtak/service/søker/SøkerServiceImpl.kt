package no.nav.tiltakspenger.vedtak.service.søker

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.repository.InnsendingRepository

class SøkerServiceImpl(
    val innsendingRepository: InnsendingRepository
) : SøkerService {

    override fun hentSøkerOgSøknader(ident: String, saksbehandler: Saksbehandler): SøkerDTO? {
        val innsendinger = innsendingRepository.findByIdent(ident)
        innsendinger.forEach { it.sjekkOmSaksbehandlerHarTilgang(saksbehandler) }

        return SøkerDTO(
            ident = ident,
            søknader = innsendinger
                .mapNotNull { it.søknad }
                .map {
                    ListeSøknadDTO(
                        søknadId = it.søknadId,
                        arrangoernavn = it.tiltak.arrangoernavn,
                        tiltakskode = it.tiltak.tiltakskode?.navn,
                        startdato = it.tiltak.startdato,
                        sluttdato = it.tiltak.sluttdato,
                    )
                }
        )
    }
}
