package no.nav.tiltakspenger.vedtak.repository.felles

import no.nav.tiltakspenger.libs.common.Saksbehandler

internal data class SaksbehandlerDbJson(
    val navIdent: String,
)

internal fun Saksbehandler.toDbJson(): SaksbehandlerDbJson = SaksbehandlerDbJson(navIdent = navIdent)
