package no.nav.tiltakspenger.saksbehandling.domene.behandling.kravdato

data class KravdatoSaksopplysninger(
    val kravdatoSaksopplysningFraSøknad: KravdatoSaksopplysning? = null,
    val kravdatoSaksopplysningFraSaksbehandler: KravdatoSaksopplysning? = null,
    val avklartKravdatoSaksopplysning: KravdatoSaksopplysning? = null,
) {
    fun avklar(): KravdatoSaksopplysninger = this.copy(
        avklartKravdatoSaksopplysning = kravdatoSaksopplysningFraSaksbehandler ?: kravdatoSaksopplysningFraSøknad,
    )

    fun erOpplysningFraSøknadAvklart(): Boolean = this.avklartKravdatoSaksopplysning == kravdatoSaksopplysningFraSøknad

    fun erOpplysningFraSaksbehandlerAvklart(): Boolean = this.avklartKravdatoSaksopplysning == kravdatoSaksopplysningFraSaksbehandler

    fun harOpplysningFraSaksbehandler(): Boolean = this.kravdatoSaksopplysningFraSaksbehandler != null
}
