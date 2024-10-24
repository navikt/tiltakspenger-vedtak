package no.nav.tiltakspenger.meldekort.domene

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.meldekort.domene.Meldekort.UtfyltMeldekort

/**
 * Består av ingen, én eller flere [Meldeperiode].
 * Vil ingen fram til første innvilgede førstegangsbehandling.
 * Kun den siste vil kunne være ikke-utfylt (åpen).
 * @param tiltakstype I MVP støtter vi kun ett tiltak, men på sikt kan vi ikke garantere at det er én til én mellom meldekortperioder og tiltakstype.
 *
 */
data class Meldeperioder(
    val tiltakstype: TiltakstypeSomGirRett,
    val verdi: List<Meldekort>,
) : List<Meldekort> by verdi {

    /**
     * @throws NullPointerException Dersom det ikke er noen meldekort som kan sendes til beslutter.
     */
    fun sendTilBeslutter(
        kommando: SendMeldekortTilBeslutterKommando,
    ): Either<KanIkkeSendeMeldekortTilBeslutter, Pair<Meldeperioder, UtfyltMeldekort>> {
        val meldekortperiode = kommando.beregnUtbetalingsdager(eksisterendeMeldekort = this)
        val ikkeUtfyltMeldekort = this.ikkeUtfyltMeldekort!!
        require(ikkeUtfyltMeldekort.id == kommando.meldekortId) {
            "MeldekortId i kommando (${kommando.meldekortId}) samsvarer ikke med siste meldekortperiode (${ikkeUtfyltMeldekort.id})"
        }
        return ikkeUtfyltMeldekort.sendTilBeslutter(meldekortperiode, kommando.saksbehandler, kommando.navkontor).map {
            Pair(
                Meldeperioder(
                    tiltakstype = tiltakstype,
                    verdi = (verdi.dropLast(1) + it).toNonEmptyListOrNull()!!,
                ),
                it,
            )
        }
    }

    fun hentMeldekort(meldekortId: MeldekortId): Meldekort? {
        return verdi.find { it.id == meldekortId }
    }

    val periode: Periode by lazy { Periode(verdi.first().fraOgMed, verdi.last().tilOgMed) }

    val utfylteMeldekort: List<UtfyltMeldekort> = verdi.filterIsInstance<UtfyltMeldekort>()

    /** Vil kun returnere hele meldekortperioder som er utfylt. Dersom siste meldekortperiode er delvis utfylt, vil ikke disse komme med. */
    val utfylteDager: List<Meldekortdag.Utfylt> = utfylteMeldekort.flatMap { it.meldeperiode.verdi }

    /** Så lenge saken er aktiv, vil det siste meldekortet være i tilstanden ikke utfylt. Vil også være null fram til første innvilgelse. */
    val ikkeUtfyltMeldekort: Meldekort.IkkeUtfyltMeldekort? =
        verdi.filterIsInstance<Meldekort.IkkeUtfyltMeldekort>().firstOrNull()

    val sakId: SakId by lazy { verdi.first().sakId }

    init {
        verdi.zipWithNext { a, b ->
            require(a.tilOgMed.plusDays(1) == b.fraOgMed) {
                "Meldekortperiodene må være sammenhengende og sortert, men var ${verdi.map { it.periode }}"
            }
        }
        verdi.zipWithNext { a, b ->
            require(a.id == b.forrigeMeldekortId) {
                "Neste meldekort ${b.id} må peke på forrige meldekort ${a.id}, men peker på ${b.forrigeMeldekortId}"
            }
        }
        require(
            verdi.all {
                it.tiltakstype == tiltakstype
            },
        ) {
            "Alle meldekortperioder må ha samme tiltakstype. Meldekortperioder.tiltakstype=$tiltakstype, meldekortperioders tiltakstyper=${
                verdi.map {
                    it.tiltakstype
                }
            }"
        }
        require(verdi.dropLast(1).all { it is UtfyltMeldekort }) {
            "Kun det siste meldekortet kan være i tilstanden 'ikke utfylt', de N første må være 'utfylt'."
        }
        require(verdi.map { it.sakId }.distinct().size <= 1) {
            "Alle meldekortperioder må tilhøre samme sak."
        }
        verdi.map { it.id }.also {
            require(it.size == it.distinct().size) {
                "Meldekort må ha unik id"
            }
        }
    }

    companion object {
        fun empty(tiltakstype: TiltakstypeSomGirRett): Meldeperioder {
            return Meldeperioder(
                tiltakstype = tiltakstype,
                verdi = emptyList(),
            )
        }
    }
}

fun NonEmptyList<Meldekort>.tilMeldekortperioder(): Meldeperioder {
    val tiltakstype = first().tiltakstype
    return Meldeperioder(
        tiltakstype = tiltakstype,
        verdi = this,
    )
}
