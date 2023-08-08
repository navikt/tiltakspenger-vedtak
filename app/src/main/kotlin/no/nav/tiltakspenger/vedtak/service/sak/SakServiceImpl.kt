package no.nav.tiltakspenger.vedtak.service.sak

import no.nav.tiltakspenger.domene.Sak
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.repository.sak.SakRepo


class SakServiceImpl(
    val sakRepo: SakRepo
): SakService {
    override fun motta(søknad: Søknad) {
        if(søknad.tiltak == null || søknad.tiltak?.sluttdato == null){
            throw RuntimeException("Tiltak er null, og det ække lov")
        }

        val periode = Periode(søknad.tiltak!!.startdato, søknad.tiltak!!.sluttdato!!)

        val sak = sakRepo.findByFnrAndPeriode(søknad.personopplysninger.ident, periode).singleOrNull()
            ?.let {
                //lag sak
            }

            // finnes behandling fra før?
            // legg søknad på behandling om den ikke er lukket
            // eller legg

    }

    override fun henteEllerOppretteSak(periode: Periode, fnr: String): Sak {
        return TODO()
    }

}
