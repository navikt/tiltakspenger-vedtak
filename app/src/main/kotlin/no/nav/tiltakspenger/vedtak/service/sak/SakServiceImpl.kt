package no.nav.tiltakspenger.vedtak.service.sak

import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.domene.sak.SaksnummerGenerator
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo

class SakServiceImpl(
    val sakRepo: SakRepo,
) : SakService {
    override fun motta(søknad: Søknad): Sak {
        val sak: Sak =
            sakRepo.findByFnrAndPeriode(
                fnr = søknad.personopplysninger.ident,
                periode = søknad.vurderingsperiode(),
            ).singleOrNull() ?: Sak.lagSak(
                søknad = søknad,
                saksnummerGenerator = SaksnummerGenerator(),
            )

        val håndtertSak = sak.håndter(søknad = søknad)

        // TODO kanskje man her skal hente saksopplysninger via sak eller behandling?

        return sakRepo.save(håndtertSak)
    }

    // TODO Her må vi finne på noe lurt... Denne er midlertidig til vi finner ut av hvordan vi skal hente Saksopplysninger
    override fun mottaInnsending(innsending: Innsending): Sak {
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

    private fun lagFaktaAvInnsending(innsending: Innsending): List<Saksopplysning> {
        val saksopplysningDagpenger =
            Saksopplysning.Dagpenger.lagFakta(innsending.ytelser?.ytelserliste, innsending.filtreringsperiode())
        val saksopplysningAap =
            Saksopplysning.Aap.lagSaksopplysninger(innsending.ytelser?.ytelserliste, innsending.filtreringsperiode())
        return saksopplysningAap + saksopplysningDagpenger
    }
}
