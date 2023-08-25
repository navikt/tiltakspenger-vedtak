package no.nav.tiltakspenger.vedtak.service.sak

import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.domene.sak.SaksnummerGenerator
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo

class SakServiceImpl(
    val sakRepo: SakRepo,
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

        return sakRepo.save(sak)
    }

    override fun henteEllerOppretteSak(periode: Periode, fnr: String): Sak {
        TODO()
    }
}
