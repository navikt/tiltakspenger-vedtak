package no.nav.tiltakspenger.vilkårsvurdering

abstract class UtelukkendeManuellVurdering : Vilkårsvurdering() {
    override var manuellVurdering: Vurdering? = null
    override fun vurderinger(): List<Vurdering> = listOfNotNull(manuellVurdering)
        .ifEmpty {
            listOf(
                Vurdering(
                    lovreferanse = lovreferanse(),
                    kilde = "N/A",
                    fom = null,
                    tom = null,
                    utfall = Utfall.IKKE_IMPLEMENTERT,
                    detaljer = ""
                )
            )
        }

    override fun detIkkeManuelleUtfallet(): Utfall = Utfall.IKKE_IMPLEMENTERT
}
