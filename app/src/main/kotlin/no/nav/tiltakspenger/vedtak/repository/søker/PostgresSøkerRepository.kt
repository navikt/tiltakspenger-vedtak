package no.nav.tiltakspenger.vedtak.repository.søker

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.db.DataSource
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository
import no.nav.tiltakspenger.vedtak.repository.aktivitetslogg.AktivitetsloggDAO
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingDAO
import no.nav.tiltakspenger.vedtak.repository.personopplysninger.PersonopplysningerDAO
import no.nav.tiltakspenger.vedtak.repository.søknad.SøknadDAO
import no.nav.tiltakspenger.vedtak.repository.tiltaksaktivitet.TiltaksaktivitetDAO
import no.nav.tiltakspenger.vedtak.repository.ytelse.YtelsesakDAO
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal class PostgresSøkerRepository(
    private val søknadDAO: SøknadDAO = SøknadDAO(),
    private val tiltaksaktivitetDAO: TiltaksaktivitetDAO = TiltaksaktivitetDAO(),
    private val personopplysningerDAO: PersonopplysningerDAO = PersonopplysningerDAO(),
    private val ytelsesakDAO: YtelsesakDAO = YtelsesakDAO(),
    private val behandlingDAO: BehandlingDAO = BehandlingDAO(),
    private val aktivitetsloggDAO: AktivitetsloggDAO = AktivitetsloggDAO(),
) : SøkerRepository {

    override fun hent(ident: String): Søker? {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                return hentMedTxSession(ident, txSession)
            }
        }
    }

    private fun hentMedTxSession(
        ident: String,
        txSession: TransactionalSession
    ): Søker? {
        return txSession.run(
            queryOf(hent, ident).map { row ->
                row.toSøker(txSession)
            }.asSingle
        )
    }

    override fun lagre(søker: Søker) {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                if (søkerFinnes(ident = søker.ident, txSession = txSession)) {
                    oppdaterTilstand(søker = søker, txSession = txSession)
                } else {
                    insert(søker = søker, txSession = txSession)
                }
                søknadDAO.lagre(søkerId = søker.id, søknader = søker.søknader, txSession = txSession)
                tiltaksaktivitetDAO.lagre(søkerId = søker.id, tiltaksaktiviteter = søker.tiltak, txSession = txSession)
                ytelsesakDAO.lagre(søkerId = søker.id, ytelsesaker = søker.ytelser, txSession = txSession)
                personopplysningerDAO.lagre(
                    søkerId = søker.id,
                    personopplysninger = søker.personopplysninger,
                    txSession = txSession
                )
                behandlingDAO.lagre(
                    søkerId = søker.id,
                    behandlinger = søker.behandlinger,
                    txSession = txSession,
                )
                aktivitetsloggDAO.lagre(
                    søkerId = søker.id,
                    aktivitetslogg = søker.aktivitetslogg,
                    txSession = txSession
                )
            }
        }
    }

    override fun findBySøknadId(søknadId: String): Søker? {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                return søknadDAO.finnIdent(søknadId, txSession)
                    ?.let { ident -> this.hentMedTxSession(ident, txSession) }
            }
        }
    }

    private fun Row.toSøker(txSession: TransactionalSession): Søker {
        val id = SøkerId.fromDb(string("id"))
        return Søker.fromDb(
            id = id,
            ident = string("ident"),
            tilstand = string("tilstand"),
            søknader = søknadDAO.hentAlle(id, txSession),
            tiltak = tiltaksaktivitetDAO.hentForSøker(id, txSession),
            ytelser = ytelsesakDAO.hentForSøker(id, txSession),
            personopplysninger = personopplysningerDAO.hent(id, txSession),
            behandlinger = behandlingDAO.hentForSøker(id, txSession),
            aktivitetslogg = aktivitetsloggDAO.hent(id, txSession)
        )
    }

    private fun søkerFinnes(ident: String, txSession: TransactionalSession): Boolean = txSession.run(
        queryOf(finnes, ident).map { row -> row.boolean("exists") }.asSingle
    ) ?: throw RuntimeException("Failed to check if person exists")

    private fun insert(søker: Søker, txSession: TransactionalSession) {
        LOG.info { "Insert user" }
        SECURELOG.info { "Insert user ${søker.id}" }
        txSession.run(
            queryOf(
                lagre,
                mapOf(
                    "id" to søker.id.toString(),
                    "ident" to søker.ident,
                    "tilstand" to søker.tilstand.type.name,
                    "sist_endret" to LocalDateTime.now(),
                    "opprettet" to LocalDateTime.now(),
                )
            ).asUpdate
        )
    }

    private fun oppdaterTilstand(søker: Søker, txSession: TransactionalSession) {
        LOG.info { "Update user" }
        SECURELOG.info { "Update user ${søker.id} tilstand ${søker.tilstand}" }
        txSession.run(
            queryOf(
                oppdater,
                mapOf(
                    "id" to søker.id.toString(),
                    "tilstand" to søker.tilstand.type.name,
                    "sistEndret" to LocalDateTime.now()
                )
            ).asUpdate
        )
    }

    @Language("SQL")
    private val lagre =
        "insert into søker (id, ident, tilstand, sist_endret, opprettet) values (:id, :ident, :tilstand, :sist_endret, :opprettet)"

    @Language("SQL")
    private val oppdater =
        """update søker set 
              tilstand = :tilstand, 
              sist_endret = :sistEndret
           where id = :id
        """.trimMargin()

    @Language("SQL")
    private val finnes = "select exists(select 1 from søker where ident = ?)"

    @Language("SQL")
    private val hent = "select * from søker where ident = ?"
}
