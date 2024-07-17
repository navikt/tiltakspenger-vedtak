package no.nav.tiltakspenger.vedtak.repository.statistikk.sak

import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.tiltakspenger.saksbehandling.ports.StatistikkSakRepo
import no.nav.tiltakspenger.saksbehandling.service.statistikk.SakStatistikkDTO
import no.nav.tiltakspenger.vedtak.db.DataSource
import org.intellij.lang.annotations.Language

internal class SakStatistikkSakRepoImpl() : StatistikkSakRepo, SakStatistikkRepoIntern {
    override fun lagre(dto: SakStatistikkDTO) {
        sessionOf(DataSource.hikariDataSource).use {
            it.transaction { txSession ->
                lagre(dto, txSession)
            }
        }
    }

    override fun lagre(dto: SakStatistikkDTO, tx: TransactionalSession) {
        tx.run(
            queryOf(
                lagreSql,
                mapOf(
                    "id" to dto.id,
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
                    "søknadsformat" to dto.søknadsformat,
                    "forventetOppstartTidspunkt" to dto.forventetOppstartTidspunkt,
                    "tekniskTidspunkt" to dto.tekniskTidspunkt, // dette er tidspunkt for når raden lages i bigQuery og må settes der
                    "sakYtelse" to dto.sakYtelse,
                    "sakUtland" to dto.sakUtland,
                    "behandlingType" to dto.behandlingType,
                    "behandlingStatus" to dto.behandlingStatus,
                    "behandlingResultat" to dto.behandlingResultat,
                    "resultatBegrunnelse" to dto.resultatBegrunnelse,
                    "behandlingMetode" to dto.behandlingMetode,
                    "opprettetAv" to dto.opprettetAv,
                    "saksbehandler" to dto.saksbehandler,
                    "ansvarligBeslutter" to dto.ansvarligBeslutter,
                    "ansvarligEnhet" to dto.ansvarligEnhet,
                    "tilbakekrevingsbeløp" to dto.tilbakekrevingsbeløp,
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
