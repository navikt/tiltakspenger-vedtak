package no.nav.tiltakspenger.vedtak.service.sak

import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.domene.sak.SaksnummerGenerator
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService

class SakServiceImpl(
    val sakRepo: SakRepo,
    val behandlingService: BehandlingService,
) : SakService {
    override fun motta(søknad: Søknad): Sak {
        val sak: Sak =
            sakRepo.findByFnrAndPeriode(
                fnr = søknad.personopplysninger.ident,
                periode = søknad.vurderingsperiodeInklKarenstid(),
            ).singleOrNull()?.håndter(søknad = søknad) // Hvis det finnes en sak fra før
                ?: Sak.lagSak(
                    søknad = søknad,
                    saksnummerGenerator = SaksnummerGenerator(),
                )


        // Burde vi gjøre håndter logikken her i stedet for i Sak og bruke behandlingService for å lage ny behandling?

        behandlingService.automatiskSaksbehandle(
            søknad = søknad,
            fakta = emptyList(),
            saksbehandler = Saksbehandler(
                navIdent = "Automatisk",
                brukernavn = "Automatisk",
                epost = "nav@nav.no",
                roller = emptyList(),
            ),
        )

        return sakRepo.save(sak)
    }

    override fun henteEllerOppretteSak(periode: Periode, fnr: String): Sak {
        TODO()
    }
}
