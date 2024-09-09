package no.nav.tiltakspenger.meldekort.domene

import arrow.core.nonEmptyListOf
import no.nav.tiltakspenger.felles.april
import no.nav.tiltakspenger.felles.februar
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.meldekort.domene.SendMeldekortTilBeslutterKommando.Dag
import no.nav.tiltakspenger.meldekort.domene.SendMeldekortTilBeslutterKommando.Status.DELTATT_UTEN_LØNN_I_TILTAKET
import no.nav.tiltakspenger.meldekort.domene.SendMeldekortTilBeslutterKommando.Status.FRAVÆR_SYK
import no.nav.tiltakspenger.meldekort.domene.SendMeldekortTilBeslutterKommando.Status.FRAVÆR_SYKT_BARN
import no.nav.tiltakspenger.meldekort.domene.SendMeldekortTilBeslutterKommando.Status.IKKE_DELTATT
import no.nav.tiltakspenger.objectmothers.ObjectMother
import org.junit.jupiter.api.Test

internal class MeldekortberegningManuellTest {
    private val meldekort1 = nonEmptyListOf(
        Dag(29.januar(2024), IKKE_DELTATT),
        Dag(30.januar(2024), IKKE_DELTATT),
        Dag(31.januar(2024), IKKE_DELTATT),
        Dag(1.februar(2024), FRAVÆR_SYK),
        Dag(2.februar(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(3.februar(2024), IKKE_DELTATT),
        Dag(4.februar(2024), IKKE_DELTATT),

        Dag(5.februar(2024), FRAVÆR_SYK),
        Dag(6.februar(2024), FRAVÆR_SYK),
        Dag(7.februar(2024), FRAVÆR_SYK),
        Dag(8.februar(2024), FRAVÆR_SYK),
        Dag(9.februar(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(10.februar(2024), IKKE_DELTATT),
        Dag(11.februar(2024), IKKE_DELTATT),
    )
    private val meldekort2 = nonEmptyListOf(
        Dag(12.februar(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(13.februar(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(14.februar(2024), FRAVÆR_SYK),
        Dag(15.februar(2024), FRAVÆR_SYK),
        Dag(16.februar(2024), FRAVÆR_SYK),
        Dag(17.februar(2024), IKKE_DELTATT),
        Dag(18.februar(2024), IKKE_DELTATT),

        Dag(19.februar(2024), FRAVÆR_SYK),
        Dag(20.februar(2024), FRAVÆR_SYK),
        Dag(21.februar(2024), FRAVÆR_SYK),
        Dag(22.februar(2024), FRAVÆR_SYK),
        Dag(23.februar(2024), FRAVÆR_SYK),
        Dag(24.februar(2024), IKKE_DELTATT),
        Dag(25.februar(2024), IKKE_DELTATT),
    )
    private val meldekort3 = nonEmptyListOf(
        Dag(26.februar(2024), FRAVÆR_SYK),
        Dag(27.februar(2024), FRAVÆR_SYK),
        Dag(28.februar(2024), FRAVÆR_SYK),
        Dag(29.februar(2024), FRAVÆR_SYK),
        Dag(1.mars(2024), FRAVÆR_SYK),
        Dag(2.mars(2024), IKKE_DELTATT),
        Dag(3.mars(2024), IKKE_DELTATT),

        Dag(4.mars(2024), FRAVÆR_SYK),
        Dag(5.mars(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(6.mars(2024), FRAVÆR_SYK),
        Dag(7.mars(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(8.mars(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(9.mars(2024), IKKE_DELTATT),
        Dag(10.mars(2024), IKKE_DELTATT),
    )
    private val meldekort4 = nonEmptyListOf(
        Dag(11.mars(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(12.mars(2024), FRAVÆR_SYKT_BARN),
        Dag(13.mars(2024), FRAVÆR_SYKT_BARN),
        Dag(14.mars(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(15.mars(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(16.mars(2024), IKKE_DELTATT),
        Dag(17.mars(2024), IKKE_DELTATT),

        Dag(18.mars(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(19.mars(2024), FRAVÆR_SYKT_BARN),
        Dag(20.mars(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(21.mars(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(22.mars(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(23.mars(2024), IKKE_DELTATT),
        Dag(24.mars(2024), IKKE_DELTATT),
    )
    private val meldekort5 = nonEmptyListOf(
        Dag(25.mars(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(26.mars(2024), FRAVÆR_SYK),
        Dag(27.mars(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(28.mars(2024), FRAVÆR_SYK),
        Dag(29.mars(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(30.mars(2024), IKKE_DELTATT),
        Dag(31.mars(2024), IKKE_DELTATT),

        Dag(1.april(2024), FRAVÆR_SYK),
        Dag(2.april(2024), FRAVÆR_SYK),
        Dag(3.april(2024), FRAVÆR_SYKT_BARN),
        Dag(4.april(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(5.april(2024), DELTATT_UTEN_LØNN_I_TILTAKET),
        Dag(6.april(2024), IKKE_DELTATT),
        Dag(7.april(2024), IKKE_DELTATT),
    )

    @Test
    fun `manuell test av meldekortberegning`() {
        val meldekortBeregning = ObjectMother.beregnMeldekortperioder(
            perioder = nonEmptyListOf(meldekort1, meldekort2, meldekort3, meldekort4, meldekort5),
        )
        for (dag in meldekortBeregning) {
            // TODO post-mvp jah: Vurder og legge til en prettyprint på denne hvis det er nødvendig. Hør med Richard om vi har noe fra før.
            println(dag)
        }
    }
}