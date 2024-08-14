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
                    "id" to dto.id.toString(),
                    "sakId" to dto.sakId,
                    "saksnummer" to null,
                    "behandlingId" to null,
                    "relatertBehandlingId" to null,
                    "ident" to null,
                    "mottattTidspunkt" to dto.søknadDato,
                    "registrertTidspunkt" to null,
                    "ferdigBehandletTidspunkt" to null,
                    "vedtakTidspunkt" to null,
                    "utbetaltTidspunkt" to null,
                    "endretTidspunkt" to null,
                    "soknadsformat" to null,
                    "forventetOppstartTidspunkt" to dto.sakFraDato,
                    "tekniskTidspunkt" to null,
                    "sakYtelse" to dto.ytelse,
                    "sakUtland" to null,
                    "behandlingType" to null,
                    "behandlingStatus" to null,
                    "behandlingResultat" to null,
                    "resultatBegrunnelse" to null,
                    "behandlingMetode" to null,
                    "opprettetAv" to null,
                    "saksbehandler" to null,
                    "ansvarligBeslutter" to null,
                    "ansvarligEnhet" to null,
                    "tilbakekrevingsbelop" to null,
                    "funksjonellPeriodeFom" to null,
                    "funksjonellPeriodeTom" to null,
                    "avsender" to null,
                    "versjon" to null,
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
            :soknadsformat,
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
            :tilbakekrevingsbelop,
            :funksjonellPeriodeFom,
            :funksjonellPeriodeTom,
            :avsender,
            :versjon
        )
    """.trimIndent()
}
