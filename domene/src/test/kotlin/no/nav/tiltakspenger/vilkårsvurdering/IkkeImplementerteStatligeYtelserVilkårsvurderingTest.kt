package no.nav.tiltakspenger.vilkårsvurdering

// TODO: Må skrive tester for PeriodeSpmVurdering ol i stedet for disse.
// Evt for Sykepenger, Gjenlevendepensjon etc
internal class IkkeImplementerteStatligeYtelserVilkårsvurderingTest {

    /*
    val vurderingsperiode = Periode(
        1.januar(2020),
        1.februar(2020),
    )

    @Test
    fun `En vilkårsvurdering uten manuell vurdering skal ha utfall IKKE_IMPLEMENTERT`() {
        val vv = SykepengerVilkårsvurdering(vurderingsperiode)
        val utfall = vv.samletUtfall()
        utfall shouldBe Utfall.IKKE_IMPLEMENTERT
    }

    @Test
    fun `En vilkårsvurdering med manuell vurdering skal ha utfall gitt av den manuelle vurderingen`() {
        val vv = SykepengerVilkårsvurdering(vurderingsperiode)
        vv.settManuellVurdering(LocalDate.MIN, LocalDate.MAX, Utfall.IKKE_OPPFYLT, "")
        val utfall = vv.samletUtfall()
        utfall shouldBe Utfall.IKKE_OPPFYLT
    }

    @Test
    fun `En vilkårsvurdering uten manuell vurdering skal ha to vurderinger med kilder`() {
        val vv = SykepengerVilkårsvurdering(vurderingsperiode)
        val vurderinger = vv.vurderinger()
        vurderinger.size shouldBe 1
        vurderinger.first() shouldBe Vurdering.IkkeImplementert(
            vilkår = Vilkår.SYKEPENGER,
            kilde = "Infotrygd/Speil",
            fom = vurderingsperiode.fra,
            tom = vurderingsperiode.til,
            detaljer = "",
        )
    }

     */
}
