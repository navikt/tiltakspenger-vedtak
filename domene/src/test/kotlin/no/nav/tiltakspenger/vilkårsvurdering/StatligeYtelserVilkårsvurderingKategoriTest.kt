package no.nav.tiltakspenger.vilkårsvurdering

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.februar
import no.nav.tiltakspenger.felles.februarDateTime
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.objectmothers.ObjectMother.foreldrepengerVedtak
import no.nav.tiltakspenger.objectmothers.ObjectMother.nyAlderspensjonVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.ObjectMother.nyGjenlevendepensjonVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.ObjectMother.nyOvergangsstønadVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySupplerendeStønadAlderVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySupplerendeStønadFlyktningVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySykepengerVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.ObjectMother.nyUføreVilkårsvurdering
import no.nav.tiltakspenger.objectmothers.ObjectMother.uføreVedtak
import no.nav.tiltakspenger.objectmothers.ObjectMother.ytelseSak
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.kategori.StatligeYtelserVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AAPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.DagpengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.ForeldrepengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OmsorgspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.OpplæringspengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PleiepengerNærståendeVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PleiepengerSyktBarnVilkårsvurdering
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
            vurderingsperiode = vurderingsperiode,
        )
        val pleiepengerNærståendeVilkårsvurdering = PleiepengerNærståendeVilkårsvurdering(
            ytelser = listOf(
                foreldrepengerVedtak(
                    ytelse = ForeldrepengerVedtak.Ytelser.PLEIEPENGER_NÆRSTÅENDE,
                    periode = Periode(
                        fra = 1.februar(2022),
                        til = 28.februar(2022),
                    ),
                ),
            ),
            vurderingsperiode = vurderingsperiode,
        )
        val pleiepengerSyktBarnVilkårsvurdering = PleiepengerSyktBarnVilkårsvurdering(
            ytelser = listOf(
                foreldrepengerVedtak(
                    ytelse = ForeldrepengerVedtak.Ytelser.PLEIEPENGER_SYKT_BARN,
                    periode = Periode(
                        fra = 1.februar(2022),
                        til = 28.februar(2022),
                    ),
                ),
            ),
            vurderingsperiode = vurderingsperiode,
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
            vurderingsperiode = vurderingsperiode,
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
            vurderingsperiode = vurderingsperiode,
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
            vurderingsperiode = vurderingsperiode,
        )

        val uføreVilkarsvurdering = nyUføreVilkårsvurdering(
            uføreVedtak = null,
            vurderingsperiode = vurderingsperiode,
        )

        val overgangsstønadVilkarsvurdering = nyOvergangsstønadVilkårsvurdering(
            overgangsstønaderVedtak = emptyList(),
            vurderingsperiode = vurderingsperiode,
        )

        val sykepengerVilkårsvurdering = nySykepengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val alderspensjonVilkårsvurdering = nyAlderspensjonVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val gjenlevendepensjonVilkårsvurdering = nyGjenlevendepensjonVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val supplerendeStønadFlyktningVilkårsvurdering = nySupplerendeStønadFlyktningVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val supplerendeStønadAlderVilkårsvurdering = nySupplerendeStønadAlderVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val statligeYtelserVilkårsvurderingKategori = StatligeYtelserVilkårsvurderingKategori(
            aap = aapVilkårsvurdering,
            dagpenger = dagpengerVilkårsvurdering,
            foreldrepenger = foreldrepengerVilkårsvurdering,
            pleiepengerNærstående = pleiepengerNærståendeVilkårsvurdering,
            pleiepengerSyktBarn = pleiepengerSyktBarnVilkårsvurdering,
            svangerskapspenger = svangerskapspengerVilkårsvurdering,
            opplæringspenger = opplæringspengerVilkårsvurdering,
            omsorgspenger = omsorgspengerVilkårsvurdering,
            uføretrygd = uføreVilkarsvurdering,
            overgangsstønad = overgangsstønadVilkarsvurdering,
            sykepenger = sykepengerVilkårsvurdering,
            alderspensjon = alderspensjonVilkårsvurdering,
            gjenlevendepensjon = gjenlevendepensjonVilkårsvurdering,
            supplerendeStønadFlyktning = supplerendeStønadFlyktningVilkårsvurdering,
            supplerendeStønadAlder = supplerendeStønadAlderVilkårsvurdering,
        )

        statligeYtelserVilkårsvurderingKategori.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
        statligeYtelserVilkårsvurderingKategori.vurderinger().size shouldBe 15
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
        val pleiepengerNærståendeVilkårsvurdering = PleiepengerNærståendeVilkårsvurdering(
            ytelser = listOf(
                foreldrepengerVedtak(
                    ytelse = ForeldrepengerVedtak.Ytelser.PLEIEPENGER_NÆRSTÅENDE,
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
        val pleiepengerSyktBarnVilkårsvurdering = PleiepengerSyktBarnVilkårsvurdering(
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

        val overgangsstønadVilkarsvurdering = nyOvergangsstønadVilkårsvurdering(
            overgangsstønaderVedtak = emptyList(),
            vurderingsperiode = Periode(
                fra = 1.februar(2022),
                til = 28.februar(2022),
            ),
        )

        val sykepengerVilkårsvurdering = nySykepengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val alderspensjonVilkårsvurdering = nyAlderspensjonVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val gjenlevendepensjonVilkårsvurdering = nyGjenlevendepensjonVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val supplerendeStønadFlyktningVilkårsvurdering = nySupplerendeStønadFlyktningVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val supplerendeStønadAlderVilkårsvurdering = nySupplerendeStønadAlderVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val statligeYtelserVilkårsvurderingKategori = StatligeYtelserVilkårsvurderingKategori(
            aap = aapVilkårsvurdering,
            dagpenger = dagpengerVilkårsvurdering,
            foreldrepenger = foreldrepengerVilkårsvurdering,
            pleiepengerNærstående = pleiepengerNærståendeVilkårsvurdering,
            pleiepengerSyktBarn = pleiepengerSyktBarnVilkårsvurdering,
            svangerskapspenger = svangerskapspengerVilkårsvurdering,
            opplæringspenger = opplæringspengerVilkårsvurdering,
            omsorgspenger = omsorgspengerVilkårsvurdering,
            uføretrygd = uføreVilkarsvurdering,
            overgangsstønad = overgangsstønadVilkarsvurdering,
            sykepenger = sykepengerVilkårsvurdering,
            alderspensjon = alderspensjonVilkårsvurdering,
            gjenlevendepensjon = gjenlevendepensjonVilkårsvurdering,
            supplerendeStønadFlyktning = supplerendeStønadFlyktningVilkårsvurdering,
            supplerendeStønadAlder = supplerendeStønadAlderVilkårsvurdering,
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
        val pleiepengerNærståendeVilkårsvurdering = PleiepengerNærståendeVilkårsvurdering(
            ytelser = emptyList(),
            vurderingsperiode = vurderingsperiode,
        )
        val pleiepengerSyktBarnVilkårsvurdering = PleiepengerSyktBarnVilkårsvurdering(
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

        val overgangsstønadVilkarsvurdering = nyOvergangsstønadVilkårsvurdering(
            overgangsstønaderVedtak = emptyList(),
            vurderingsperiode = Periode(
                fra = 1.februar(2022),
                til = 28.februar(2022),
            ),
        )

        val sykepengerVilkårsvurdering = nySykepengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val alderspensjonVilkårsvurdering = nyAlderspensjonVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val gjenlevendepensjonVilkårsvurdering = nyGjenlevendepensjonVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val supplerendeStønadFlyktningVilkårsvurdering = nySupplerendeStønadFlyktningVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val supplerendeStønadAlderVilkårsvurdering = nySupplerendeStønadAlderVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val statligeYtelserVilkårsvurderingKategori = StatligeYtelserVilkårsvurderingKategori(
            aap = aapVilkårsvurdering,
            dagpenger = dagpengerVilkårsvurdering,
            foreldrepenger = foreldrepengerVilkårsvurdering,
            pleiepengerNærstående = pleiepengerNærståendeVilkårsvurdering,
            pleiepengerSyktBarn = pleiepengerSyktBarnVilkårsvurdering,
            svangerskapspenger = svangerskapspengerVilkårsvurdering,
            opplæringspenger = opplæringspengerVilkårsvurdering,
            omsorgspenger = omsorgspengerVilkårsvurdering,
            uføretrygd = uføreVilkarsvurdering,
            overgangsstønad = overgangsstønadVilkarsvurdering,
            sykepenger = sykepengerVilkårsvurdering,
            alderspensjon = alderspensjonVilkårsvurdering,
            gjenlevendepensjon = gjenlevendepensjonVilkårsvurdering,
            supplerendeStønadFlyktning = supplerendeStønadFlyktningVilkårsvurdering,
            supplerendeStønadAlder = supplerendeStønadAlderVilkårsvurdering,
        )

        statligeYtelserVilkårsvurderingKategori.samletUtfall() shouldBe Utfall.OPPFYLT
    }

    @Test
    fun `Samlet utfall for statlige ytelser er ikke godkjent hvis man har uføre vedtak før perioden`() {
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
        val pleiepengerNærståendeVilkårsvurdering = PleiepengerNærståendeVilkårsvurdering(
            ytelser = emptyList(),
            vurderingsperiode = vurderingsperiode,
        )
        val pleiepengerSyktBarnVilkårsvurdering = PleiepengerSyktBarnVilkårsvurdering(
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
        val uføreVilkarsvurdering = nyUføreVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
            uføreVedtak = uføreVedtak(
                harUføregrad = true,
                datoUfør = 1.januar(2020),
                virkDato = 1.januar(2020),
            ),
        )

        val overgangsstønadVilkarsvurdering = nyOvergangsstønadVilkårsvurdering(
            overgangsstønaderVedtak = emptyList(),
            vurderingsperiode = Periode(
                fra = 1.februar(2022),
                til = 28.februar(2022),
            ),
        )

        val sykepengerVilkårsvurdering = nySykepengerVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val alderspensjonVilkårsvurdering = nyAlderspensjonVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val gjenlevendepensjonVilkårsvurdering = nyGjenlevendepensjonVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val supplerendeStønadFlyktningVilkårsvurdering = nySupplerendeStønadFlyktningVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val supplerendeStønadAlderVilkårsvurdering = nySupplerendeStønadAlderVilkårsvurdering(
            vurderingsperiode = vurderingsperiode,
        )

        val statligeYtelserVilkårsvurderingKategori = StatligeYtelserVilkårsvurderingKategori(
            aap = aapVilkårsvurdering,
            dagpenger = dagpengerVilkårsvurdering,
            foreldrepenger = foreldrepengerVilkårsvurdering,
            pleiepengerNærstående = pleiepengerNærståendeVilkårsvurdering,
            pleiepengerSyktBarn = pleiepengerSyktBarnVilkårsvurdering,
            svangerskapspenger = svangerskapspengerVilkårsvurdering,
            opplæringspenger = opplæringspengerVilkårsvurdering,
            omsorgspenger = omsorgspengerVilkårsvurdering,
            uføretrygd = uføreVilkarsvurdering,
            overgangsstønad = overgangsstønadVilkarsvurdering,
            sykepenger = sykepengerVilkårsvurdering,
            alderspensjon = alderspensjonVilkårsvurdering,
            gjenlevendepensjon = gjenlevendepensjonVilkårsvurdering,
            supplerendeStønadFlyktning = supplerendeStønadFlyktningVilkårsvurdering,
            supplerendeStønadAlder = supplerendeStønadAlderVilkårsvurdering,
        )

        statligeYtelserVilkårsvurderingKategori.samletUtfall() shouldBe Utfall.KREVER_MANUELL_VURDERING
    }
}
