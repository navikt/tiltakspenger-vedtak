package no.nav.tiltakspenger.vedtak.service.sak

import no.nav.tiltakspenger.domene.sak.Sak
import no.nav.tiltakspenger.domene.sak.SaksnummerGenerator
import no.nav.tiltakspenger.domene.saksopplysning.AapTolker
import no.nav.tiltakspenger.domene.saksopplysning.DagpengerTolker
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepo
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo

class SakServiceImpl(
    val sakRepo: SakRepo,
    val behandlingRepo: BehandlingRepo,
) : SakService {
    override fun motta(søknad: Søknad): Sak {
        val sak: Sak =
            sakRepo.hentForIdentMedPeriode(
                fnr = søknad.personopplysninger.ident,
                periode = søknad.vurderingsperiode(),
            ).singleOrNull() ?: Sak.lagSak(
                søknad = søknad,
                saksnummerGenerator = SaksnummerGenerator(),
            )

        val håndtertSak = sak.håndter(søknad = søknad)

        // TODO kanskje man her skal hente saksopplysninger via sak eller behandling?

        return sakRepo.lagre(håndtertSak)
    }

    override fun mottaPersonopplysninger(personopplysninger: List<Personopplysninger>): Sak {
        TODO("Not yet implemented")
    }

    // TODO Her må vi finne på noe lurt... Denne er midlertidig til vi finner ut av hvordan vi skal hente Saksopplysninger
    override fun mottaInnsending(innsending: Innsending): Sak {
        val sak = sakRepo.hentForIdentMedPeriode(
            fnr = innsending.ident,
            periode = innsending.vurderingsperiodeForSøknad()!!,
        ).singleOrNull() ?: Sak.lagSak(
            søknad = innsending.søknad!!,
            saksnummerGenerator = SaksnummerGenerator(),
        )

        val sakMedSøknad = sak.håndter(innsending.søknad!!)
        val sakVilkårsvurdert = sakMedSøknad.mottaFakta(lagFaktaAvInnsending(innsending))

        return sakRepo.lagre(sakVilkårsvurdert)
    }

    override fun henteEllerOppretteSak(periode: Periode, fnr: String): Sak {
        TODO()
    }

    override fun henteMedBehandlingsId(behandlingId: BehandlingId): Sak? {
        val behandling = behandlingRepo.hent(behandlingId) ?: return null
        return sakRepo.hent(behandling.sakId)
    }

    private fun lagFaktaAvInnsending(innsending: Innsending): List<Saksopplysning> {
        val saksopplysningDagpenger =
            AapTolker.tolkeData(innsending.ytelser?.ytelserliste, innsending.filtreringsperiode())
        val saksopplysningAap =
            DagpengerTolker.tolkeData(innsending.ytelser?.ytelserliste, innsending.filtreringsperiode())
        return saksopplysningAap + saksopplysningDagpenger
    }
}
