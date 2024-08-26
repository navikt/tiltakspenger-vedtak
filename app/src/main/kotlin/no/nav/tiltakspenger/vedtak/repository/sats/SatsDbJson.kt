package no.nav.tiltakspenger.vedtak.repository.sats

import no.nav.tiltakspenger.utbetaling.domene.Sats
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson

data class SatsDbJson(
    val periode: PeriodeDbJson,
    val sats: Int,
    val satsDelvis: Int,
    val satsBarnetillegg: Int,
    val satsBarnetilleggDelvis: Int,
) {
    fun toDomain(): Sats = Sats(
        periode = periode.toDomain(),
        sats = sats,
        satsDelvis = satsDelvis,
        satsBarnetillegg = satsBarnetillegg,
        satsBarnetilleggDelvis = satsBarnetilleggDelvis,
    )
}

internal fun Sats.toDbJson(): SatsDbJson =
    SatsDbJson(
        periode = periode.toDbJson(),
        sats = sats,
        satsDelvis = satsDelvis,
        satsBarnetillegg = satsBarnetillegg,
        satsBarnetilleggDelvis = satsBarnetilleggDelvis,
    )
