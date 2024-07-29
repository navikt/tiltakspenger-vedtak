package no.nav.tiltakspenger.vedtak.repository.søknad

import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.ports.SøknadRepo

internal class PostgresSøknadRepo(
    private val sessionFactory: PostgresSessionFactory,
    private val søknadDAO: SøknadDAO,
) : SøknadRepo {

    override fun hentSøknad(søknadId: SøknadId): Søknad {
        return sessionFactory.withSession {
            søknadDAO.hentForSøknadId(søknadId, it)
        }
    }

    override fun lagre(søknad: Søknad, txContext: TransactionContext?) {
        sessionFactory.withTransaction(txContext) {
            søknadDAO.lagreHeleSøknaden(søknad, it)
        }
    }

    /**
     * TODO jah: Denne kan potensielt hente veldig mye data, bør kun hente akkurat det vi trenger i frontend.
     */
    override fun hentAlleSøknader(): List<Søknad> {
        return sessionFactory.withSession {
            søknadDAO.hentAlleSøknader(it)
        }
    }
}
