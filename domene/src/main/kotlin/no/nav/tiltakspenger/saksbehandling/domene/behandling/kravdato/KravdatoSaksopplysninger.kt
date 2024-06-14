package no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato

data class KravdatoSaksopplysninger(
    val kravdatoSaksopplysningFraSøknad: KravdatoSaksopplysning? = null,
    val kravdatoSaksopplysningFraSaksbehandler: KravdatoSaksopplysning? = null,
    val avklartKravdatoSaksopplysning: KravdatoSaksopplysning? = null,
) {
    fun avklar(): KravdatoSaksopplysninger = this.copy(
        avklartKravdatoSaksopplysning = kravdatoSaksopplysningFraSaksbehandler ?: kravdatoSaksopplysningFraSøknad,
    )
}
