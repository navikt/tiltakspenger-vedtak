package no.nav.tiltakspenger.vedtak.repository.behandling.tiltakDeltagelse

import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde

enum class TiltakskildeDb {
    Arena,
    Komet,
}

internal fun String.toTiltakskilde(): Tiltakskilde {
    return when (TiltakskildeDb.valueOf(this)) {
        TiltakskildeDb.Arena -> Tiltakskilde.Arena
        TiltakskildeDb.Komet -> Tiltakskilde.Komet
    }
}

internal fun Tiltakskilde.toDb(): String {
    return when (this) {
        Tiltakskilde.Arena -> TiltakskildeDb.Arena
        Tiltakskilde.Komet -> TiltakskildeDb.Komet
    }.toString()
}
