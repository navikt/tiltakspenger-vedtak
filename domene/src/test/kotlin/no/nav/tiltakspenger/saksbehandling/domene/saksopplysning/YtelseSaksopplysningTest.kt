package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
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

    @Test
    fun `happycase for saksopplysninger som dekker alle krav`() {
        val saksopplysningerOppfyllerAlleKrav = listOf(
            mockYtelseSaksopplysning(
                periode =
                mockPeriode(
                    fra = vurderingsperiode.fra,
                    til = vurderingsperiode.til.minusDays(6),
                ),
            ),
            mockYtelseSaksopplysning(
                periode =
                mockPeriode(
                    fra = vurderingsperiode.til.minusDays(5),
                    til = vurderingsperiode.til,
                ),
            ),
        )

        shouldNotThrowAny {
            saksopplysningerOppfyllerAlleKrav.vilkårsvurder(vurderingsperiode = vurderingsperiode)
        }
    }

    @Test
    fun `sjekk at utfallet av en vilkårsvurdering for en ytelse man ikke har gir utfall OPPFYLT for vilkåret`() {
        val gyldigeSaksopplysninger = listOf(
            mockYtelseSaksopplysning(
                periode =
                mockPeriode(
                    fra = vurderingsperiode.fra,
                    til = vurderingsperiode.til,
                ),
                harYtelse = false,
            ),
        )

        val vurdering = gyldigeSaksopplysninger.vilkårsvurder(vurderingsperiode = vurderingsperiode)

        vurdering.size shouldBe 1
        vurdering.first().utfall shouldBe Utfall.OPPFYLT
    }

    @Test
    fun `sjekk at utfallet av en vilkårsvurdering for en ytelse man har gir utfall IKKE_OPPFYLT for vilkåret`() {
        val gyldigeSaksopplysninger = listOf(
            mockYtelseSaksopplysning(
                vilkår = Vilkår.SYKEPENGER,
                periode =
                mockPeriode(
                    fra = vurderingsperiode.fra,
                    til = vurderingsperiode.til,
                ),
                harYtelse = true,
            ),
        )

        val vurdering = gyldigeSaksopplysninger.vilkårsvurder(vurderingsperiode = vurderingsperiode)

        vurdering.size shouldBe 1
        vurdering.first().utfall shouldBe Utfall.IKKE_OPPFYLT
    }

    @Test
    fun `sjekk at utfallet av at man har AAP, DAGPENGER og TILTAKSPENGER gir utfall KREVER_MANUELL_VURDERING `() {
        val vilkårListe = listOf(Vilkår.AAP, Vilkår.DAGPENGER, Vilkår.TILTAKSPENGER)

        vilkårListe.forEach { vilkår ->
            listOf(
                mockYtelseSaksopplysning(
                    vilkår = vilkår,
                    periode =
                    mockPeriode(
                        fra = vurderingsperiode.fra,
                        til = vurderingsperiode.til,
                    ),
                    harYtelse = true,
                ),
            ).vilkårsvurder(vurderingsperiode = vurderingsperiode).also { vurdering ->
                vurdering.size shouldBe 1
                vurdering.first().utfall shouldBe Utfall.KREVER_MANUELL_VURDERING
            }
        }
    }
}
