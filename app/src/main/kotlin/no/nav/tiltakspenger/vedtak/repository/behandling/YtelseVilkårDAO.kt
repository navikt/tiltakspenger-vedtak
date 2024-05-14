package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelsePeriode
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.YtelseSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.YtelseVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.YtelseVilkårData
import org.intellij.lang.annotations.Language
import java.time.LocalDateTime
import java.util.UUID

class YtelseVilkårDAO {
    fun lagre(behandlingId: BehandlingId, ytelseVilkår: YtelseVilkår, txSession: TransactionalSession) {
        ytelseVilkår.aap.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.alderspensjon.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.dagpenger.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.foreldrepenger.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.gjenlevendepensjon.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.institusjonsopphold.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.introprogrammet.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.jobbsjansen.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.kvp.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.omsorgspenger.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.opplæringspenger.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.overgangsstønad.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.pensjonsinntekt.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.pleiepengerNærstående.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.pleiepengerSyktBarn.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.supplerendestønadalder.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.supplerendestønadflyktning.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.svangerskapspenger.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.sykepenger.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.tiltakspenger.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.uføretrygd.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
        ytelseVilkår.etterlønn.let { lagreYtelseVilkår(behandlingId = behandlingId, ytelseVilkårData = it, txSession = txSession) }
    }

    private fun lagreYtelseVilkår(behandlingId: BehandlingId, ytelseVilkårData: YtelseVilkårData, txSession: TransactionalSession) {
        ytelseVilkårData.saksopplysningerSaksbehandler?.let { lagreYtelseVilkårData(behandlingId = behandlingId, ytelseSaksopplysning = it, txSession = txSession) }
        ytelseVilkårData.saksopplysningerAnnet?.let { lagreYtelseVilkårData(behandlingId = behandlingId, ytelseSaksopplysning = it, txSession = txSession) }
        ytelseVilkårData.avklarteSaksopplysninger?.let { lagreYtelseVilkårData(behandlingId = behandlingId, ytelseSaksopplysning = it, txSession = txSession) }

    }

    private fun lagreYtelseVilkårData(behandlingId: BehandlingId, ytelseSaksopplysning: YtelseSaksopplysning, txSession: TransactionalSession) {
        val ytelseSaksopplysningId = UUID.randomUUID()

        txSession.run(
            queryOf(
                lagreYtelseSaksopplysning,
                mapOf(
                    "id" to ytelseSaksopplysningId.toString(),
                    "behandlingId" to behandlingId.toString(),
                    "vilkar" to ytelseSaksopplysning.vilkår.tittel,
                    "kilde" to ytelseSaksopplysning.kilde.name,
                    "detaljer" to ytelseSaksopplysning.detaljer,
                    "saksbehandler" to ytelseSaksopplysning.saksbehandler,
                    "opprettet" to LocalDateTime.now(),
                ),
            ).asUpdate,
        )
        ytelseSaksopplysning.subperioder.forEach { periode ->
            txSession.run(
                queryOf(
                    lagreHarYtelsePeriode,
                    mapOf(
                        "ytelseSaksopplysningId" to ytelseSaksopplysningId.toString(),
                        "fom" to periode.periode.fra,
                        "tom" to periode.periode.til,
                        "harYtelse" to periode.harYtelse,
                    ),
                ).asUpdate,
            )
        }
    }


    @Language("SQL")
    private val lagreYtelseSaksopplysning = """
        insert into ytelse_saksopplysning (id, behandling_id, vilkår, kilde, detaljer, saksbehandler, opprettet) 
        values (:id, :behandlingId, :vilkar, :kilde, :detaljer, :saksbehandler, :opprettet)
    """.trimIndent()

    @Language("SQL")
    private val lagreHarYtelsePeriode = """
        insert into ytelsessaksopplysning_har_ytelse_periode (ytelse_saksopplysning_id, fom, tom, har_ytelse)
        values (:ytelseSaksopplysningId, :fom, :tom, :harYtelse)
    """.trimIndent()

    fun hentYtelseVilkår(behandlingId: BehandlingId, vurderingsperiode: Periode, txSession: TransactionalSession): YtelseVilkår {
        return YtelseVilkår(
            aap = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.AAP, vurderingsperiode = vurderingsperiode),
            alderspensjon = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.ALDERSPENSJON, vurderingsperiode = vurderingsperiode),
            dagpenger = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.DAGPENGER, vurderingsperiode = vurderingsperiode),
            foreldrepenger = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.FORELDREPENGER, vurderingsperiode = vurderingsperiode),
            gjenlevendepensjon = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.GJENLEVENDEPENSJON, vurderingsperiode = vurderingsperiode),
            institusjonsopphold = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.INSTITUSJONSOPPHOLD, vurderingsperiode = vurderingsperiode),
            introprogrammet = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.INTROPROGRAMMET, vurderingsperiode = vurderingsperiode),
            jobbsjansen = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.JOBBSJANSEN, vurderingsperiode = vurderingsperiode),
            kvp = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.KVP, vurderingsperiode = vurderingsperiode),
            omsorgspenger = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.OMSORGSPENGER, vurderingsperiode = vurderingsperiode),
            opplæringspenger = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.OPPLÆRINGSPENGER, vurderingsperiode = vurderingsperiode),
            overgangsstønad = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.OVERGANGSSTØNAD, vurderingsperiode = vurderingsperiode),
            pensjonsinntekt = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.PENSJONSINNTEKT, vurderingsperiode = vurderingsperiode),
            pleiepengerNærstående = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.PLEIEPENGER_NÆRSTÅENDE, vurderingsperiode = vurderingsperiode),
            pleiepengerSyktBarn = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.PLEIEPENGER_SYKT_BARN, vurderingsperiode = vurderingsperiode),
            supplerendestønadalder = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.SUPPLERENDESTØNADALDER, vurderingsperiode = vurderingsperiode),
            supplerendestønadflyktning = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.SUPPLERENDESTØNADFLYKTNING, vurderingsperiode = vurderingsperiode),
            svangerskapspenger = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.SVANGERSKAPSPENGER, vurderingsperiode = vurderingsperiode),
            sykepenger = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.SYKEPENGER, vurderingsperiode = vurderingsperiode),
            tiltakspenger = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.TILTAKSPENGER, vurderingsperiode = vurderingsperiode),
            uføretrygd = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.UFØRETRYGD, vurderingsperiode = vurderingsperiode),
            etterlønn = hentYtelseVilkårData(behandlingId = behandlingId, txSession = txSession, vilkår = Vilkår.ETTERLØNN, vurderingsperiode = vurderingsperiode),
        )
    }

    private fun hentYtelseVilkårData(
        behandlingId: BehandlingId,
        vilkår: Vilkår,
        vurderingsperiode: Periode,
        txSession: TransactionalSession,
    ): YtelseVilkårData {
        return YtelseVilkårData(
            vilkår = vilkår,
            vurderingsperiode = vurderingsperiode,
            saksopplysningerSaksbehandler = hentSaksbehandler(
                behandlingId = behandlingId,
                txSession = txSession,
                vilkår = vilkår,
            ),
            saksopplysningerAnnet = hentAnnet(
                behandlingId = behandlingId,
                txSession = txSession,
                vilkår = vilkår,
            ),
            avklarteSaksopplysninger = hentAvklart(
                behandlingId = behandlingId,
                txSession = txSession,
                vilkår = vilkår,
            ),
            vurderinger = emptyList(), // TODOOOO
        )
    }

    private fun hentSaksbehandler(
        behandlingId: BehandlingId,
        vilkår: Vilkår,
        txSession: TransactionalSession,
    ): YtelseSaksopplysning? {
        return txSession.run(
            queryOf(
                hentYtelseVilkårDataSaksbehandler,
                mapOf(
                    "behandlingId" to behandlingId.toString(),
                    "vilkar" to vilkår.tittel,
                    "kilde" to Kilde.SAKSB.name,
                ),
            ).map { row -> row.toYtelseSaksopplysning(txSession) }
                .asSingle,
        )
    }

    private fun hentAnnet(
        behandlingId: BehandlingId,
        vilkår: Vilkår,
        txSession: TransactionalSession,
    ): YtelseSaksopplysning? {
        return txSession.run(
            queryOf(
                hentYtelseVilkårDataAnnet,
                mapOf(
                    "behandlingId" to behandlingId.toString(),
                    "vilkar" to vilkår.tittel,
                    "kilde" to Kilde.SAKSB.name,
                ),
            ).map { row -> row.toYtelseSaksopplysning(txSession) }
                .asSingle,
        )
    }

    private fun hentAvklart(
        behandlingId: BehandlingId,
        vilkår: Vilkår,
        txSession: TransactionalSession,
    ): YtelseSaksopplysning? {
        return txSession.run(
            queryOf(
                hentYtelseVilkårDataAvklart,
                mapOf(
                    "behandlingId" to behandlingId.toString(),
                    "vilkar" to vilkår.tittel,
                ),
            ).map { row -> row.toYtelseSaksopplysning(txSession) }
                .asSingle,
        )
    }

    private fun Row.toYtelseSaksopplysning(txSession: TransactionalSession): YtelseSaksopplysning {
        return YtelseSaksopplysning(
            kilde = string("kilde").let { Kilde.valueOf(it) },
            vilkår = hentVilkår(string("vilkår")),
            detaljer = string("detaljer"),
            saksbehandler = stringOrNull("saksbehandler"),
            subperioder = hentHarYtelsePerioder(txSession = txSession, ytelseSaksopplysningId = string("id")),
        )
    }

    private fun hentHarYtelsePerioder(txSession: TransactionalSession, ytelseSaksopplysningId: String): List<HarYtelsePeriode> {
        return txSession.run(
            queryOf(hentHarYtelsePeriode, ytelseSaksopplysningId)
                .map { row -> row.toHarYtelsePeriode() }
                .asList,
        )
    }

    private fun Row.toHarYtelsePeriode(): HarYtelsePeriode {
        return HarYtelsePeriode(
            periode = Periode(localDate("fom"), localDate("tom")),
            harYtelse = boolean("har_ytelse"),
        )
    }

    @Language("SQL")
    private val hentHarYtelsePeriode = "select * from ytelsessaksopplysning_har_ytelse_periode where ytelse_saksopplysning_id = ?"

    @Language("SQL")
    private val hentYtelseVilkårDataSaksbehandler = """
        select * from ytelse_saksopplysning 
            where behandling_id = :behandlingId 
            and vilkår = :vilkar
            and kilde = :kilde
    """.trimIndent()

    @Language("SQL")
    private val hentYtelseVilkårDataAnnet = """
        select * from ytelse_saksopplysning 
            where behandling_id = :behandlingId 
            and vilkår = :vilkar
            and kilde <> :kilde
    """.trimIndent()

    @Language("SQL")
    private val hentYtelseVilkårDataAvklart = """
        select * from ytelse_saksopplysning 
            where behandling_id = :behandlingId 
            and vilkår = :vilkar
            and avklart_tidspunkt IS NOT NULL
    """.trimIndent()
}
