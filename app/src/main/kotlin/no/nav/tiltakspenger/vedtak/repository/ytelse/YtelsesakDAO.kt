package no.nav.tiltakspenger.vedtak.repository.ytelse

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.InnsendingId
import no.nav.tiltakspenger.felles.UlidBase
import no.nav.tiltakspenger.felles.UlidBase.Companion.random
import no.nav.tiltakspenger.vedtak.YtelseSak
import org.intellij.lang.annotations.Language

class YtelsesakDAO(
    private val ytelsevedtakDAO: YtelsevedtakDAO = YtelsevedtakDAO(),
) {
    fun hentForInnsending(innsendingId: InnsendingId, txSession: TransactionalSession): List<YtelseSak> {
        return txSession.run(
            queryOf(hentYtelsesak, innsendingId.toString())
                .map { row -> row.toYtelsesak(txSession) }
                .asList
        )
    }

    fun lagre(innsendingId: InnsendingId, ytelsesaker: List<YtelseSak>, txSession: TransactionalSession) {
        slettYtelser(innsendingId, txSession)
        ytelsesaker.forEach { ytelseSak ->
            lagreYtelse(innsendingId, ytelseSak, txSession)
        }
    }

    private fun lagreYtelse(innsendingId: InnsendingId, ytelseSak: YtelseSak, txSession: TransactionalSession) {
        val id = random(ULID_PREFIX_YTELSE)
        txSession.run(
            queryOf(
                lagreYtelseSak,
                mapOf(
                    "id" to id.toString(),
                    "innsendingId" to innsendingId.toString(),
                    "fomGyldighetsperiode" to ytelseSak.fomGyldighetsperiode,
                    "tomGyldighetsperiode" to ytelseSak.tomGyldighetsperiode,
                    "datoKravMottatt" to ytelseSak.datoKravMottatt,
                    "dataKravMottatt" to ytelseSak.dataKravMottatt,
                    "fagsystemSakId" to ytelseSak.fagsystemSakId,
                    "status" to ytelseSak.status?.name,
                    "ytelsestype" to ytelseSak.ytelsestype?.name,
                    "antallDagerIgjen" to ytelseSak.antallDagerIgjen,
                    "antallUkerIgjen" to ytelseSak.antallUkerIgjen,
                    "tidsstempelHosOss" to ytelseSak.tidsstempelHosOss,
                )
            ).asUpdate
        )
        ytelsevedtakDAO.lagre(id, ytelseSak.vedtak, txSession)
    }

    private fun slettYtelser(innsendingId: InnsendingId, txSession: TransactionalSession) {
        ytelsevedtakDAO.slettVedtakForInnsending(innsendingId, txSession)
        txSession.run(queryOf(slettYtelsesak, innsendingId.toString()).asUpdate)
    }

    private fun Row.toYtelsesak(txSession: TransactionalSession): YtelseSak {
        return YtelseSak(
            fomGyldighetsperiode = localDateTime("fom_gyldighetsperiode"),
            tomGyldighetsperiode = localDateTimeOrNull("tom_gyldighetsperiode"),
            datoKravMottatt = localDateOrNull("dato_krav_mottatt"),
            dataKravMottatt = stringOrNull("data_krav_mottatt"),
            fagsystemSakId = stringOrNull("fagsystem_sak_id"),
            vedtak = ytelsevedtakDAO.hentForVedtak(UlidBase.fromDb(string("id")), txSession),
            status = stringOrNull("status")?.let { YtelseSak.YtelseSakStatus.valueOf(it) },
            ytelsestype = stringOrNull("ytelsestype")?.let { YtelseSak.YtelseSakYtelsetype.valueOf(it) },
            antallDagerIgjen = intOrNull("antall_dager_igjen"),
            antallUkerIgjen = intOrNull("antall_uker_igjen"),
            tidsstempelHosOss = localDateTime("tidsstempel_hos_oss")
        )
    }

    @Language("SQL")
    private val lagreYtelseSak = """
        insert into ytelsesak (
            id,
            innsending_id,
            fom_gyldighetsperiode,
            tom_gyldighetsperiode,
            dato_krav_mottatt,
            data_krav_mottatt,
            fagsystem_sak_id,
            status,
            ytelsestype,
            antall_dager_igjen,
            antall_uker_igjen,
            tidsstempel_hos_oss
        ) values (
            :id, 
            :innsendingId,
            :fomGyldighetsperiode,
            :tomGyldighetsperiode,
            :datoKravMottatt,
            :dataKravMottatt,
            :fagsystemSakId,
            :status,
            :ytelsestype,
            :antallDagerIgjen,
            :antallUkerIgjen,
            :tidsstempelHosOss
        )
    """.trimIndent()

    @Language("SQL")
    private val slettYtelsesak = "delete from ytelsesak where innsending_id = ?"

    @Language("SQL")
    private val hentYtelsesak = "select * from ytelsesak where innsending_id = ?"

    companion object {
        private const val ULID_PREFIX_YTELSE = "ayt"
    }
}
