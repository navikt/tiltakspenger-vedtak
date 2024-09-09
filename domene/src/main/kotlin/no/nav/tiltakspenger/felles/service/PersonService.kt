package no.nav.tiltakspenger.felles.service

import no.nav.tiltakspenger.felles.exceptions.IkkeFunnetException
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.meldekort.ports.MeldekortRepo
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo
import no.nav.tiltakspenger.saksbehandling.ports.SakRepo
import no.nav.tiltakspenger.saksbehandling.ports.SøknadRepo
import no.nav.tiltakspenger.utbetaling.ports.UtbetalingsvedtakRepo

class PersonService(
    private val meldekortRepo: MeldekortRepo,
    private val sakRepo: SakRepo,
    private val behandlingRepo: BehandlingRepo,
    private val utbetalingsvedtakRepo: UtbetalingsvedtakRepo,
    private val søknadRepo: SøknadRepo,
) {

    fun hentFnrForBehandlingId(behandlingId: BehandlingId): Fnr {
        return behandlingRepo.hentFnrForBehandlingId(behandlingId)
            ?: throw IkkeFunnetException("Fant ikke fnr på behandlingId: $behandlingId")
    }

    fun hentFnrForSakId(sakId: SakId): Fnr {
        return sakRepo.hentFnrForSakId(sakId) ?: throw IkkeFunnetException("Fant ikke fnr på sakId: $sakId")
    }

    fun hentFnrForSaksnummer(saksnummer: Saksnummer): Fnr {
        return sakRepo.hentFnrForSaksnummer(saksnummer)
            ?: throw IkkeFunnetException("Fant ikke fnr for saksnummer: $saksnummer")
    }

    fun hentFnrForVedtakId(vedtakId: VedtakId): Fnr {
        val vedtak = utbetalingsvedtakRepo.hentForVedtakId(vedtakId)
            ?: throw IkkeFunnetException("Fant ikke vedtak for vedtakId: $vedtakId")

        return sakRepo.hentFnrForSaksnummer(vedtak.saksnummer)
            ?: throw IkkeFunnetException("Fant ikke fnr for saksnummer: ${vedtak.saksnummer}")
    }

    fun hentFnrForMeldekortId(meldekortId: MeldekortId): Fnr {
        return meldekortRepo.hentFnrForMeldekortId(meldekortId)
            ?: throw IkkeFunnetException("Fant ikke fnr på meldekortId: $meldekortId")
    }

    fun hentFnrForSøknadId(søknadId: SøknadId): Fnr {
        val søknad = søknadRepo.hentSøknad(søknadId)

        return søknad.personopplysninger.fnr
    }
}
