package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import io.kotest.assertions.throwables.shouldThrowWithMessage
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import java.time.LocalDate
import kotlin.test.Test

class YtelseSaksopplysningTest {

    fun mockPeriode(fra: LocalDate = LocalDate.now(), til: LocalDate = LocalDate.now()) =
        Periode(fra = fra, til = til)

    private val vurderingsperiode = mockPeriode(
        fra = 1.januar(2025),
        til = 31.januar(2025),
    )

    fun mockYtelseSaksopplysning(
        kilde: Kilde = Kilde.SAKSB,
        vilkår: Vilkår = Vilkår.AAP,
        detaljer: String = "test",
        saksbehandler: String? = null,
        periode: Periode = mockPeriode(),
        harYtelse: Boolean = false,
    ): YtelseSaksopplysning = YtelseSaksopplysning(
        kilde = kilde,
        vilkår = vilkår,
        detaljer = detaljer,
        saksbehandler = saksbehandler,
        periode = periode,
        harYtelse = harYtelse,
    )

    @Test
    fun `vilkårsvurderingen skal kaste en feil hvis det kommer inn saksopplysninger om ulike vilkår`() {
        val saksopplysningerMedUlikeVilkår = listOf(
            mockYtelseSaksopplysning(),
            mockYtelseSaksopplysning(),
        )
        shouldThrowWithMessage<IllegalStateException>("Kan ikke vilkårsvurdere saksopplysninger med forskjellige vilkår") {
            saksopplysningerMedUlikeVilkår.vilkårsvurder(vurderingsperiode = vurderingsperiode)
        }
    }

    @Test
    fun `vilkårsvurderingen skal kaste en feil dersom noen saksopplysninger overlapper`() {
        val saksopplysningerMedOverlappendePerioder = listOf(
            mockYtelseSaksopplysning(periode = vurderingsperiode),
            mockYtelseSaksopplysning(
                periode =
                mockPeriode(
                    fra = vurderingsperiode.fra,
                    til = vurderingsperiode.til.minusDays(1),
                ),
            ),
        )
        shouldThrowWithMessage<IllegalStateException>("Ulike saksopplysninger for samme vilkår kan ikke ha overlappende perioder") {
            saksopplysningerMedOverlappendePerioder.vilkårsvurder(vurderingsperiode = vurderingsperiode)
        }
    }

    @Test
    fun `vilkårsvurderingen skal kaste en feil dersom saksopplysningene ikke dekker hele vurderingsperioden`() {
        val saksopplysningerSomIkkeDekkerVurderingsperioden = listOf(
            mockYtelseSaksopplysning(
                periode =
                mockPeriode(
                    fra = vurderingsperiode.fra,
                    til = vurderingsperiode.til.minusDays(1),
                ),
            ),
        )
        shouldThrowWithMessage<IllegalStateException>("Vi må ha saksopplysninger for hele vurderingsperioden for å kunne vurdere vilkåret") {
            saksopplysningerSomIkkeDekkerVurderingsperioden.vilkårsvurder(vurderingsperiode = vurderingsperiode)
        }
    }

    @Test
    fun `vilkårsvurderingen skal kaste en feil dersom saksopplysningene går utenfor vurderingsperioden`() {
        val saksopplysningerSomGårUtenforVurderingsperioden = listOf(
            mockYtelseSaksopplysning(
                periode =
                mockPeriode(
                    fra = vurderingsperiode.fra,
                    til = vurderingsperiode.til.plusDays(1),
                ),
            ),
        )
        shouldThrowWithMessage<IllegalStateException>("Vi kan ikke vilkårsvurdere saksopplysninger som går utenfor vurderingsperioden") {
            saksopplysningerSomGårUtenforVurderingsperioden.vilkårsvurder(vurderingsperiode = vurderingsperiode)
        }
    }
}
