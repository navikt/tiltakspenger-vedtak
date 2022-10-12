package no.nav.tiltakspenger.vedtak.repository.ytelse

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.felles.UlidBase
import no.nav.tiltakspenger.vedtak.YtelseSak
import org.intellij.lang.annotations.Language

class YtelsesakDAO(
    private val ytelsevedtakDAO: YtelsevedtakDAO = YtelsevedtakDAO(),
) {
    fun hentForSøker(søkerId: SøkerId, txSession: TransactionalSession): List<YtelseSak> {
        return txSession.run(
            queryOf(hentYtelsesak, søkerId.toString())
                .map { row -> row.toYtelsesak(txSession) }
                .asList
        )
    }

    fun lagre(søkerId: SøkerId, ytelsesaker: List<YtelseSak>, txSession: TransactionalSession) {
        slettYtelser(søkerId, txSession)
        ytelsesaker.forEach { ytelseSak ->
            lagreYtelse(søkerId, ytelseSak, txSession)
        }
    }

    private fun lagreYtelse(søkerId: SøkerId, ytelseSak: YtelseSak, txSession: TransactionalSession) {
        val id = UlidBase.new(ULID_PREFIX_YTELSE)
        txSession.run(
            queryOf(
                lagreYtelseSak, mapOf(
                    "id" to id.toString(),
                    "sokerId" to søkerId.toString(),
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

    private fun slettYtelser(søkerId: SøkerId, txSession: TransactionalSession) {
        ytelsevedtakDAO.slettVedtakForSøker(søkerId, txSession)
        txSession.run(queryOf(slettYtelsesak, søkerId.toString()).asUpdate)
    }

    private fun Row.toYtelsesak(txSession: TransactionalSession): YtelseSak {
        return YtelseSak(
            fomGyldighetsperiode = localDateTime("fom_gyldighetsperiode"),
            tomGyldighetsperiode = localDateTimeOrNull("tom_gyldighetsperiode"),
            datoKravMottatt = localDateOrNull("dato_krav_mottatt"),
            dataKravMottatt = stringOrNull("data_krav_mottatt"),
            fagsystemSakId = intOrNull("fagsystem_sak_id"),
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
            søker_id,
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
            :sokerId,
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
        )""".trimIndent()

    @Language("SQL")
    private val slettYtelsesak = "delete from ytelsesak where søker_id = ?"

    @Language("SQL")
    private val hentYtelsesak = "select * from ytelsesak where søker_id = ?"

    companion object {
        private const val ULID_PREFIX_YTELSE = "ayt"
    }
}
