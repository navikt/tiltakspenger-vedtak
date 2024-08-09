package no.nav.tiltakspenger.vedtak.repository.behandling.stønadsdager

import no.nav.tiltakspenger.saksbehandling.domene.stønadsdager.Stønadsdager
import no.nav.tiltakspenger.saksbehandling.domene.stønadsdager.StønadsdagerSaksopplysning
import no.nav.tiltakspenger.vedtak.db.deserialize
import no.nav.tiltakspenger.vedtak.db.serialize
import no.nav.tiltakspenger.vedtak.repository.felles.PeriodeDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson
import java.security.InvalidParameterException

/**
 * Har ansvar for å serialisere/deserialisere Stønadsdager til og fra json for lagring i database.
 */
private data class StønadsdagerDbJson(
    val registerSaksopplysning: StønadsdagerSaksopplysningDbJson,
    val vurderingsperiode: PeriodeDbJson,
)

internal fun String.toStønadsdager(): Stønadsdager {
    try {
        val stønadsdagerDbJson = deserialize<StønadsdagerDbJson>(this)
        return Stønadsdager(
            registerSaksopplysning = stønadsdagerDbJson.registerSaksopplysning.toDomain() as StønadsdagerSaksopplysning.Register,
            vurderingsperiode = stønadsdagerDbJson.vurderingsperiode.toDomain(),
        )
    } catch (exception: Exception) {
        throw InvalidParameterException("Det oppstod en feil ved parsing av json: " + exception.message)
    }
}

internal fun Stønadsdager.toDbJson(): String =
    serialize(
        StønadsdagerDbJson(
            vurderingsperiode = vurderingsperiode.toDbJson(),
            registerSaksopplysning = registerSaksopplysning.toDbJson(),
        ),
    )
