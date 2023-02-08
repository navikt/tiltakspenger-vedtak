package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.domene.februar
import no.nav.tiltakspenger.domene.februarDateTime
import no.nav.tiltakspenger.domene.januarDateTime
import no.nav.tiltakspenger.objectmothers.ObjectMother.foreldrepengerVedtak
import no.nav.tiltakspenger.objectmothers.ObjectMother.nyUføreVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.ObjectMother.ytelseSak
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.kategori.StatligeYtelserVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AAPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.DagpengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.ForeldrepengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OmsorgspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OpplæringspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PleiepengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.SvangerskapspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.UføreVilkarsvurdering
import org.junit.jupiter.api.Test

internal class StatligeYtelserVilkårsvurderingKategoriTest {

    @Test
    fun `skal ha med alle`() {
        val vurderingsperiode = Periode(1.februar(2022), 20.februar(2022))
        val aapVilkårsvurdering = AAPVilkårsvurdering(
            ytelser = listOf(
                ytelseSak(
                    fomGyldighetsperiode = 1.januarDateTime(2022),
                    tomGyldighetsperiode = 31.januarDateTime(2022),
                    ytelsestype = YtelseSak.YtelseSakYtelsetype.AA,
                ),
            ),
            vurderingsperiode = vurderingsperiode,
        )
        val dagpengerVilkårsvurdering = DagpengerVilkårsvurdering(
            ytelser = listOf(
                ytelseSak(
                    fomGyldighetsperiode = 1.februarDateTime(2022),
                    tomGyldighetsperiode = 28.februarDateTime(2022),
                    ytelsestype = YtelseSak.YtelseSakYtelsetype.DAGP,
                ),
            ),
            vurderingsperiode = vurderingsperiode,
        )
        val foreldrepengerVilkårsvurdering = ForeldrepengerVilkårsvurdering(
            ytelser = listOf(
                foreldrepengerVedtak(
                    periode = Periode(
                        fra = 1.februar(2022),
                        til = 28.februar(2022),
                    ),
                ),
            ),
            vurderingsperiode = Periode(
                fra = 1.februar(2022),
                til = 28.februar(2022),
            ),
        )
        val pleiepengerVilkårsvurdering = PleiepengerVilkårsvurdering(
            ytelser = listOf(
                foreldrepengerVedtak(
                    ytelse = ForeldrepengerVedtak.Ytelser.PLEIEPENGER_SYKT_BARN,
                    periode = Periode(
                        fra = 1.februar(2022),
                        til = 28.februar(2022),
                    ),
                ),
            ),
            vurderingsperiode = Periode(
                fra = 1.februar(2022),
                til = 28.februar(2022),
            ),
        )
        val omsorgspengerVilkårsvurdering = OmsorgspengerVilkårsvurdering(
            ytelser = listOf(
                foreldrepengerVedtak(
                    ytelse = ForeldrepengerVedtak.Ytelser.OMSORGSPENGER,
                    periode = Periode(
                        fra = 1.februar(2022),
                        til = 28.februar(2022),
                    ),
                ),
            ),
            vurderingsperiode = Periode(
                fra = 1.februar(2022),
                til = 28.februar(2022),
            ),
        )
        val opplæringspengerVilkårsvurdering = OpplæringspengerVilkårsvurdering(
            ytelser = listOf(
                foreldrepengerVedtak(
                    ytelse = ForeldrepengerVedtak.Ytelser.OPPLÆRINGSPENGER,
                    periode = Periode(
                        fra = 1.februar(2022),
                        til = 28.februar(2022),
                    ),
                ),
            ),
            vurderingsperiode = Periode(
                fra = 1.februar(2022),
                til = 28.februar(2022),
            ),
        )
        val svangerskapspengerVilkårsvurdering = SvangerskapspengerVilkårsvurdering(
            ytelser = listOf(
                foreldrepengerVedtak(
                    ytelse = ForeldrepengerVedtak.Ytelser.SVANGERSKAPSPENGER,
                    periode = Periode(
                        fra = 1.februar(2022),
                        til = 28.februar(2022),
                    ),
                ),
            ),
            vurderingsperiode = Periode(
                fra = 1.februar(2022),
                til = 28.februar(2022),
            ),
        )

        val uføreVilkarsvurdering = nyUføreVilkårsvurdering(
            uføreVedtak = null,
            vurderingsperiode = Periode(
                fra = 1.februar(2022),
                til = 28.februar(2022),
            ),
        )

        val statligeYtelserVilkårsvurderingKategori = StatligeYtelserVilkårsvurderingKategori(
            aap = aapVilkårsvurdering,
            dagpenger = dagpengerVilkårsvurdering,
            foreldrepenger = foreldrepengerVilkårsvurdering,
            pleiepenger = pleiepengerVilkårsvurdering,
            svangerskapspenger = svangerskapspengerVilkårsvurdering,
            opplæringspenger = opplæringspengerVilkårsvurdering,
            omsorgspenger = omsorgspengerVilkårsvurdering,
            uføretrygd = uføreVilkarsvurdering,
        )

        statligeYtelserVilkårsvurderingKategori.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
        statligeYtelserVilkårsvurderingKategori.vurderinger().size shouldBe 13
    }

    @Test
    fun `Samlet utfall for statlige ytelser, hvis 1 er ikke godkjent er ingen godkjent`() {
        val vurderingsperiode = Periode(1.februar(2022), 20.februar(2022))
        val aapVilkårsvurdering = AAPVilkårsvurdering(
            ytelser = listOf(
                ytelseSak(
                    fomGyldighetsperiode = 1.januarDateTime(2022),
                    tomGyldighetsperiode = 31.januarDateTime(2022),
                    ytelsestype = YtelseSak.YtelseSakYtelsetype.AA,
                ),
            ),
            vurderingsperiode = vurderingsperiode,
        )
        val dagpengerVilkårsvurdering = DagpengerVilkårsvurdering(
            ytelser = listOf(
                ytelseSak(
                    fomGyldighetsperiode = 1.februarDateTime(2022),
                    tomGyldighetsperiode = 28.februarDateTime(2022),
                    ytelsestype = YtelseSak.YtelseSakYtelsetype.DAGP,
                ),
            ),
            vurderingsperiode = vurderingsperiode,
        )
        val foreldrepengerVilkårsvurdering = ForeldrepengerVilkårsvurdering(
            ytelser = listOf(
                foreldrepengerVedtak(
                    periode = Periode(
                        fra = 1.februar(2022),
                        til = 28.februar(2022),
                    ),
                ),
            ),
            vurderingsperiode = Periode(
                fra = 1.februar(2022),
                til = 28.februar(2022),
            ),
        )
        val pleiepengerVilkårsvurdering = PleiepengerVilkårsvurdering(
            ytelser = listOf(
                foreldrepengerVedtak(
                    ytelse = ForeldrepengerVedtak.Ytelser.PLEIEPENGER_SYKT_BARN,
                    periode = Periode(
                        fra = 1.februar(2022),
                        til = 28.februar(2022),
                    ),
                ),
            ),
            vurderingsperiode = Periode(
                fra = 1.februar(2022),
                til = 28.februar(2022),
            ),
        )
        val omsorgspengerVilkårsvurdering = OmsorgspengerVilkårsvurdering(
            ytelser = listOf(
                foreldrepengerVedtak(
                    ytelse = ForeldrepengerVedtak.Ytelser.OMSORGSPENGER,
                    periode = Periode(
                        fra = 1.februar(2022),
                        til = 28.februar(2022),
                    ),
                ),
            ),
            vurderingsperiode = Periode(
                fra = 1.februar(2022),
                til = 28.februar(2022),
            ),
        )
        val opplæringspengerVilkårsvurdering = OpplæringspengerVilkårsvurdering(
            ytelser = listOf(
                foreldrepengerVedtak(
                    ytelse = ForeldrepengerVedtak.Ytelser.OPPLÆRINGSPENGER,
                    periode = Periode(
                        fra = 1.februar(2022),
                        til = 28.februar(2022),
                    ),
                ),
            ),
            vurderingsperiode = Periode(
                fra = 1.februar(2022),
                til = 28.februar(2022),
            ),
        )
        val svangerskapspengerVilkårsvurdering = SvangerskapspengerVilkårsvurdering(
            ytelser = listOf(
                foreldrepengerVedtak(
                    ytelse = ForeldrepengerVedtak.Ytelser.SVANGERSKAPSPENGER,
                    periode = Periode(
                        fra = 1.februar(2022),
                        til = 28.februar(2022),
                    ),
                ),
            ),
            vurderingsperiode = Periode(
                fra = 1.februar(2022),
                til = 28.februar(2022),
            ),
        )
        val uføreVilkarsvurdering = nyUføreVilkårsvurdering(
            vurderingsperiode = Periode(
                fra = 1.februar(2022),
                til = 28.februar(2022),
            ),
        )

        val statligeYtelserVilkårsvurderingKategori = StatligeYtelserVilkårsvurderingKategori(
            aap = aapVilkårsvurdering,
            dagpenger = dagpengerVilkårsvurdering,
            foreldrepenger = foreldrepengerVilkårsvurdering,
            pleiepenger = pleiepengerVilkårsvurdering,
            svangerskapspenger = svangerskapspengerVilkårsvurdering,
            opplæringspenger = opplæringspengerVilkårsvurdering,
            omsorgspenger = omsorgspengerVilkårsvurdering,
            uføretrygd = uføreVilkarsvurdering,
        )

        statligeYtelserVilkårsvurderingKategori.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }

    @Test
    fun `Samlet utfall for statlige ytelser, hvis begge er godkjent er alle godkjent`() {
        val vurderingsperiode = Periode(1.februar(2022), 20.februar(2022))
        val aapVilkårsvurdering = AAPVilkårsvurdering(
            ytelser = emptyList(),
            vurderingsperiode = vurderingsperiode,
        )
        val dagpengerVilkårsvurdering = DagpengerVilkårsvurdering(
            ytelser = emptyList(),
            vurderingsperiode = vurderingsperiode,
        )
        val foreldrepengerVilkårsvurdering = ForeldrepengerVilkårsvurdering(
            ytelser = emptyList(),
            vurderingsperiode = vurderingsperiode,
        )
        val pleiepengerVilkårsvurdering = PleiepengerVilkårsvurdering(
            ytelser = emptyList(),
            vurderingsperiode = vurderingsperiode,
        )
        val svangerskapspengerVilkårsvurdering = SvangerskapspengerVilkårsvurdering(
            ytelser = emptyList(),
            vurderingsperiode = vurderingsperiode,
        )
        val opplæringspengerVilkårsvurdering = OpplæringspengerVilkårsvurdering(
            ytelser = emptyList(),
            vurderingsperiode = vurderingsperiode,
        )
        val omsorgspengerVilkårsvurdering = OmsorgspengerVilkårsvurdering(
            ytelser = emptyList(),
            vurderingsperiode = vurderingsperiode,
        )
        val uføreVilkarsvurdering = UføreVilkarsvurdering(
            uføreVedtak = null,
            vurderingsperiode = vurderingsperiode,
        )

        val statligeYtelserVilkårsvurderingKategori = StatligeYtelserVilkårsvurderingKategori(
            aap = aapVilkårsvurdering,
            dagpenger = dagpengerVilkårsvurdering,
            foreldrepenger = foreldrepengerVilkårsvurdering,
            pleiepenger = pleiepengerVilkårsvurdering,
            svangerskapspenger = svangerskapspengerVilkårsvurdering,
            opplæringspenger = opplæringspengerVilkårsvurdering,
            omsorgspenger = omsorgspengerVilkårsvurdering,
            uføretrygd = uføreVilkarsvurdering,
        )

        statligeYtelserVilkårsvurderingKategori.samletUtfall() shouldBe Utfall.OPPFYLT
    }
}
