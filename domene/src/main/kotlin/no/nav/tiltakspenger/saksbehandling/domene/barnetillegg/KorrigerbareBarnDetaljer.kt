package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

data class KorrigerbareBarnDetaljer private constructor(
    val opprinneligSaksopplysning: BarnetilleggBarnDetaljer,
    val korrigertSaksopplysning: BarnetilleggBarnDetaljer?,
    val avklartSaksopplysning: BarnetilleggBarnDetaljer,
) {
    companion object {
        operator fun invoke(saksopplysning: BarnetilleggBarnDetaljer): KorrigerbareBarnDetaljer {
            return KorrigerbareBarnDetaljer(
                opprinneligSaksopplysning = saksopplysning,
                korrigertSaksopplysning = null,
                avklartSaksopplysning = saksopplysning,
            )
        }
    }

    fun avklarFakta(): KorrigerbareBarnDetaljer = this.copy(
        avklartSaksopplysning = korrigertSaksopplysning ?: opprinneligSaksopplysning,
    )
}
