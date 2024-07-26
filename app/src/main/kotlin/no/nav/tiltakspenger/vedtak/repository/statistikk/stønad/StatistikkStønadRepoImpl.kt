package no.nav.tiltakspenger.vedtak.repository.statistikk.stønad

import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkStønadRepo
import no.nav.tiltakspenger.saksbehandling.service.statistikk.stønad.StatistikkStønadDTO
import org.intellij.lang.annotations.Language

internal class StatistikkStønadRepoImpl(
    private val sessionFactory: PostgresSessionFactory,
) : StatistikkStønadRepo, StatistikkStønadDAO {
    override fun lagre(dto: StatistikkStønadDTO, context: TransactionContext?) {
        sessionFactory.withTransaction(context) { tx ->
            lagre(dto, tx)
        }
    }

    override fun lagre(dto: StatistikkStønadDTO, tx: TransactionalSession) {
        tx.run(
            queryOf(
                lagreSql,
                mapOf(
                    "id" to dto.id,
                    "sakId" to dto.sakId,
                    "brukerId" to dto.brukerId,
                    "sakDato" to dto.sakDato,
                    "gyldigFraDato" to dto.sakFraDato,
                    "gyldigTilDato" to dto.sakTilDato,
                ),
            ).asUpdate,
        )
    }

    @Language("SQL")
    private val lagreSql = """
        insert into statistikk_stønad (
            id,
            sak_id,
            saksnummer,
            behandlingId,
            relatertBehandlingId,
            ident,
            mottattTidspunkt,
            registrertTidspunkt,
            ferdigBehandletTidspunkt,
            vedtakTidspunkt,
            utbetaltTidspunkt,
            endretTidspunkt,
            søknadsformat,
            forventetOppstartTidspunkt,
            tekniskTidspunkt,
            sakYtelse,
            sakUtland,
            behandlingType,
            behandlingStatus,
            behandlingResultat,
            resultatBegrunnelse,
            behandlingMetode,
            opprettetAv,
            saksbehandler,
            ansvarligBeslutter,
            ansvarligEnhet,
            tilbakekrevingsbeløp,
            funksjonellPeriodeFom,
            funksjonellPeriodeTom,
            avsender,
            versjon        
        ) values (
            :id,
            :sakId,
            :saksnummer,
            :behandlingId,
            :relatertBehandlingId,
            :ident,
            :mottattTidspunkt,
            :registrertTidspunkt,
            :ferdigBehandletTidspunkt,
            :vedtakTidspunkt,
            :utbetaltTidspunkt,
            :endretTidspunkt,
            :søknadsformat,
            :forventetOppstartTidspunkt,
            :tekniskTidspunkt,
            :sakYtelse,
            :sakUtland,
            :behandlingType,
            :behandlingStatus,
            :behandlingResultat,
            :resultatBegrunnelse,
            :behandlingMetode,
            :opprettetAv,
            :saksbehandler,
            :ansvarligBeslutter,
            :ansvarligEnhet,
            :tilbakekrevingsbeløp,
            :funksjonellPeriodeFom,
            :funksjonellPeriodeTom,
            :avsender,
            :versjon
        )
    """.trimIndent()
}
