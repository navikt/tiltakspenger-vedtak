package no.nav.tiltakspenger.vedtak.service.sak

import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.domene.sak.SaksnummerGenerator
import no.nav.tiltakspenger.domene.saksopplysning.Fakta
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService

class SakServiceImpl(
    val sakRepo: SakRepo,
) : SakService {
    override fun motta(søknad: Søknad): Sak {
        val sak: Sak =
            sakRepo.findByFnrAndPeriode(
                fnr = søknad.personopplysninger.ident,
                periode = søknad.vurderingsperiodeInklKarenstid(),
            ).singleOrNull() ?: Sak.lagSak(
                søknad = søknad,
                saksnummerGenerator = SaksnummerGenerator(),
                )

        val håndtertSak = sak.håndter(søknad = søknad)

        return sakRepo.save(håndtertSak)
    }

    // TODO Her må vi finne på noe lurt...
    fun mottaInnsending(innsending: Innsending): Sak {
        val sak = sakRepo.findByFnrAndPeriode(
            fnr = innsending.ident,
            periode = innsending.vurderingsperiodeForSøknad()!!,
        ).singleOrNull() ?: Sak.lagSak(
            søknad = innsending.søknad!!,
            saksnummerGenerator = SaksnummerGenerator(),
        )

        val sakMedSøknad = sak.håndter(innsending.søknad!!)
        val sakVilkårsvurdert = sakMedSøknad.mottaFakta(lagFaktaAvInnsending(innsending))

        return sakRepo.save(sakVilkårsvurdert)
    }

    override fun henteEllerOppretteSak(periode: Periode, fnr: String): Sak {
        TODO()
    }

    private fun lagFaktaAvInnsending(innsending: Innsending): List<Fakta> {
        val faktaDagpenger = Fakta.Dagpenger.lagFakta(innsending.ytelser?.ytelserliste, innsending.filtreringsperiode())
        val faktaAap = Fakta.Aap.lagFakta(innsending.ytelser?.ytelserliste, innsending.filtreringsperiode())
        return faktaAap + faktaDagpenger
    }
}
