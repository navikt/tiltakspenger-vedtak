package no.nav.tiltakspenger.vedtak.repository.statistikk.sak

import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.libs.persistering.infrastruktur.PostgresSessionFactory
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.saksbehandling.service.statistikk.sak.StatistikkSakDTO
import org.intellij.lang.annotations.Language

internal class StatistikkSakRepoImpl(
    private val sessionFactory: PostgresSessionFactory,
) : StatistikkSakRepo, StatistikkSakDAO {
    override fun lagre(dto: StatistikkSakDTO, context: TransactionContext?) {
        sessionFactory.withTransaction(context) { tx ->
            lagre(dto, tx)
        }
    }

    override fun lagre(dto: StatistikkSakDTO, tx: TransactionalSession) {
        tx.run(
            queryOf(
                lagreSql,
                mapOf(
                    "sakId" to dto.sakId,
                    "saksnummer" to dto.saksnummer,
                    "behandlingId" to dto.behandlingId,
                    "relatertBehandlingId" to dto.relatertBehandlingId,
                    "ident" to dto.ident,
                    "mottattTidspunkt" to dto.mottattTidspunkt,
                    "registrertTidspunkt" to dto.registrertTidspunkt,
                    "ferdigBehandletTidspunkt" to dto.ferdigBehandletTidspunkt,
                    "vedtakTidspunkt" to dto.vedtakTidspunkt,
                    "utbetaltTidspunkt" to dto.utbetaltTidspunkt,
                    "endretTidspunkt" to dto.endretTidspunkt,
                    "soknadsformat" to dto.søknadsformat,
                    "forventetOppstartTidspunkt" to dto.forventetOppstartTidspunkt,
                    "tekniskTidspunkt" to dto.tekniskTidspunkt,
                    "sakYtelse" to false,
                    "sakUtland" to false,
                    "behandlingType" to dto.behandlingType.toString(),
                    "behandlingStatus" to dto.behandlingStatus.toString(),
                    "behandlingResultat" to dto.behandlingResultat.toString(),
                    "resultatBegrunnelse" to dto.resultatBegrunnelse,
                    "behandlingMetode" to dto.behandlingMetode,
                    "opprettetAv" to dto.opprettetAv,
                    "saksbehandler" to dto.saksbehandler,
                    "ansvarligBeslutter" to dto.ansvarligBeslutter,
                    "ansvarligEnhet" to dto.ansvarligEnhet,
                    "tilbakekrevingsbelop" to dto.tilbakekrevingsbeløp,
                    "funksjonellPeriodeFom" to dto.funksjonellPeriodeFom,
                    "funksjonellPeriodeTom" to dto.funksjonellPeriodeTom,
                    "avsender" to dto.avsender,
                    "versjon" to dto.versjon,
                ),
            ).asUpdate,
        )
    }

    @Language("SQL")
    private val lagreSql = """
        insert into statistikk_sak (
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
