package no.nav.tiltakspenger.saksbehandling.domene.saksopplysning

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.VilkårData
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.YtelseVilkårData
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import java.time.LocalDate
import kotlin.test.Test

class YtelseSaksopplysningTest {

    fun mockPeriode(fra: LocalDate = LocalDate.now(), til: LocalDate = LocalDate.now()) =
        Periode(fra = fra, til = til)

    private val vurderingsperiode = mockPeriode(
        fra = 1.januar(2025),
        til = 31.januar(2025),
    )

    fun mockHarYtelsePeriode(
        periode: Periode = mockPeriode(),
        harYtelse: Boolean = false,
    ): HarYtelsePeriode {
        return HarYtelsePeriode(
            periode = periode,
            harYtelse = harYtelse,
        )
    }

    fun mockYtelseSaksopplysning(
        kilde: Kilde = Kilde.SAKSB,
        vilkår: Vilkår = Vilkår.AAP,
        detaljer: String = "test",
        saksbehandler: String? = null,
        subPerioder: List<HarYtelsePeriode> = listOf(HarYtelsePeriode(periode = mockPeriode(), harYtelse = false)),
    ): YtelseSaksopplysning = YtelseSaksopplysning(
        kilde = kilde,
        vilkår = vilkår,
        detaljer = detaljer,
        saksbehandler = saksbehandler,
        subperioder = subPerioder,
    )

    fun mockVilkårDataYtelser(
        vilkår: Vilkår = Vilkår.AAP,
        vurderingsperiode: Periode = mockPeriode(),
        saksopplysningerSaksbehandler: YtelseSaksopplysning? = null,
        saksopplysningerAnnet: YtelseSaksopplysning? = null,
        avklarteSaksopplysninger: YtelseSaksopplysning? = null,
        vurderinger: List<Vurdering> = emptyList(),
    ): YtelseVilkårData = YtelseVilkårData(
        vilkår = vilkår,
        vurderingsperiode = vurderingsperiode,
        saksopplysningerSaksbehandler = saksopplysningerSaksbehandler,
        saksopplysningerAnnet = saksopplysningerAnnet,
        avklarteSaksopplysninger = avklarteSaksopplysninger,
        vurderinger = vurderinger,
    )

    @Test
    fun `vilkårsvurderingen skal kaste en feil dersom noen saksopplysninger overlapper`() {

        val harYtelsePeriode = listOf(
            mockHarYtelsePeriode(periode = vurderingsperiode),
            mockHarYtelsePeriode(
                periode =
                mockPeriode(
                    fra = vurderingsperiode.fra,
                    til = vurderingsperiode.til.minusDays(1),
                ),
            ),
        )

        val saksopplysningerMedOverlappendePerioder =
            mockYtelseSaksopplysning(subPerioder = harYtelsePeriode)

        shouldThrowWithMessage<IllegalArgumentException>("Ulike saksopplysninger for samme vilkår kan ikke ha overlappende perioder") {
            VilkårData.tempKompileringsDemp().leggTilSaksopplysning(saksopplysningerMedOverlappendePerioder)
                .vilkårsvurder()
        }
    }

    @Test
    fun `vilkårsvurderingen skal kaste en feil dersom saksopplysningene ikke dekker hele vurderingsperioden`() {
        val harYtelsePeriode = listOf(
            mockHarYtelsePeriode(
                periode =
                mockPeriode(
                    fra = vurderingsperiode.fra,
                    til = vurderingsperiode.til.minusDays(1),
                ),
            ),
        )

        val saksopplysningerSomIkkeDekkerVurderingsperioden =
            mockYtelseSaksopplysning(
                subPerioder = harYtelsePeriode,
            )

        shouldThrowWithMessage<IllegalArgumentException>("Vi må ha saksopplysninger for hele vurderingsperioden for å kunne vurdere vilkåret") {
            VilkårData.tempKompileringsDemp().leggTilSaksopplysning(saksopplysningerSomIkkeDekkerVurderingsperioden)
                .vilkårsvurder()
        }
    }

    @Test
    fun `vilkårsvurderingen skal kaste en feil dersom saksopplysningene går utenfor vurderingsperioden`() {
        val harYtelsePeriode = listOf(
            mockHarYtelsePeriode(
                periode =
                mockPeriode(
                    fra = vurderingsperiode.fra,
                    til = vurderingsperiode.til.plusDays(1),
                ),
            ),
        )

        val saksopplysningerSomGårUtenforVurderingsperioden =
            mockYtelseSaksopplysning(
                subPerioder = harYtelsePeriode,
            )

        shouldThrowWithMessage<IllegalArgumentException>("Vi må ha saksopplysninger for hele vurderingsperioden for å kunne vurdere vilkåret") {
            VilkårData.tempKompileringsDemp().leggTilSaksopplysning(saksopplysningerSomGårUtenforVurderingsperioden)
                .vilkårsvurder()
        }
    }

    @Test
    fun `happycase for saksopplysninger som dekker alle krav`() {
        val harYtelsePeriode = listOf(
            mockHarYtelsePeriode(
                periode =
                mockPeriode(
                    fra = vurderingsperiode.fra,
                    til = vurderingsperiode.til.minusDays(6),
                ),
            ),
            mockHarYtelsePeriode(
                periode =
                mockPeriode(
                    fra = vurderingsperiode.til.minusDays(5),
                    til = vurderingsperiode.til,
                ),
            ),
        )

        val saksopplysningerOppfyllerAlleKrav =
            mockYtelseSaksopplysning(
                subPerioder = harYtelsePeriode,
            )

        val vilkårDataYtelser = mockVilkårDataYtelser(
            saksopplysningerSaksbehandler = saksopplysningerOppfyllerAlleKrav,
            vurderingsperiode = vurderingsperiode,
        )

        shouldNotThrowAny {
            vilkårDataYtelser.vilkårsvurder()
        }
    }

    @Test
    fun `sjekk at utfallet av en vilkårsvurdering for en ytelse man ikke har gir utfall OPPFYLT for vilkåret`() {
        val harYtelsePeriode = listOf(
            mockHarYtelsePeriode(periode = vurderingsperiode),
        )

        val vurderingsperiode = mockPeriode(
            fra = vurderingsperiode.fra,
            til = vurderingsperiode.til,
        )

        val gyldigeSaksopplysninger =
            mockYtelseSaksopplysning(
                subPerioder = harYtelsePeriode,
            )

        mockVilkårDataYtelser(
            saksopplysningerSaksbehandler = gyldigeSaksopplysninger,
            vurderingsperiode = vurderingsperiode,
        ).vilkårsvurder().also { vilkårDataYtelser ->
            vilkårDataYtelser.vurderinger.size shouldBe 1
            vilkårDataYtelser.vurderinger.all { it.utfall == Utfall.OPPFYLT }
        }
    }


    @Test
    fun `sjekk at utfallet av en vilkårsvurdering for en ytelse man har gir utfall IKKE_OPPFYLT for vilkåret`() {
        val harYtelsePeriode = listOf(
            mockHarYtelsePeriode(
                periode =
                mockPeriode(
                    fra = vurderingsperiode.fra,
                    til = vurderingsperiode.til,
                ),
                harYtelse = true,
            ),
        )

        val vilkår = Vilkår.SYKEPENGER
        val gyldigeSaksopplysninger =
            mockYtelseSaksopplysning(
                vilkår = vilkår,
                subPerioder = harYtelsePeriode,
            )

        mockVilkårDataYtelser(
            vilkår = vilkår,
            saksopplysningerSaksbehandler = gyldigeSaksopplysninger,
            vurderingsperiode = vurderingsperiode,
        ).vilkårsvurder()
            .also { vilkårDataYtelser ->
                vilkårDataYtelser.vurderinger.size shouldBe 1
                vilkårDataYtelser.vurderinger.all { it.utfall == Utfall.IKKE_OPPFYLT }
            }
    }

    @Test
    fun `sjekk at utfallet av at man har AAP, DAGPENGER og TILTAKSPENGER gir utfall KREVER_MANUELL_VURDERING `() {
        val harYtelsePeriode = listOf(
            mockHarYtelsePeriode(
                periode =
                mockPeriode(
                    fra = vurderingsperiode.fra,
                    til = vurderingsperiode.til,
                ),
                harYtelse = true,
            ),
        )

        val vilkårListe = listOf(Vilkår.AAP, Vilkår.DAGPENGER, Vilkår.TILTAKSPENGER)

        vilkårListe.forEach { vilkår ->
            val saksopplysninger =
                mockYtelseSaksopplysning(
                    vilkår = vilkår,
                    subPerioder = harYtelsePeriode,
                )

            mockVilkårDataYtelser(
                vilkår = vilkår,
                saksopplysningerSaksbehandler = saksopplysninger,
                vurderingsperiode = vurderingsperiode,
            ).vilkårsvurder()
                .also { vilkårDataYtelser ->
                    vilkårDataYtelser.vurderinger.size shouldBe 1
                    vilkårDataYtelser.vurderinger.all { it.utfall == Utfall.KREVER_MANUELL_VURDERING }
                }
        }
    }
}
