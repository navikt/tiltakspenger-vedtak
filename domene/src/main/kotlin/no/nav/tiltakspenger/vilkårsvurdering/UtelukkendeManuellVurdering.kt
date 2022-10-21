package no.nav.tiltakspenger.vilkårsvurdering

abstract class UtelukkendeManuellVurdering : Vilkårsvurdering() {
    override var manuellVurdering: Vurdering? = null
    override fun vurderinger(): List<Vurdering> = listOfNotNull(manuellVurdering)
        .ifEmpty {
            listOf(
                Vurdering(
                    kilde = "Infotrygd",
                    fom = null,
                    tom = null,
                    utfall = Utfall.KREVER_MANUELL_VURDERING,
                    detaljer = ""
                )
            )
        }

    override fun detIkkeManuelleUtfallet(): Utfall = Utfall.KREVER_MANUELL_VURDERING
}
