package no.nav.tiltakspenger.vedtak.routes.dto

import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler

internal data class SaksbehandlerDTO(
    val navIdent: String,
    val brukernavn: String,
    val epost: String,
    val roller: List<RolleDTO>,
) {
    enum class RolleDTO {
        SAKSBEHANDLER,
        FORTROLIG_ADRESSE,
        STRENGT_FORTROLIG_ADRESSE,
        SKJERMING,
        LAGE_HENDELSER,

        // Systemadministrator (oss)
        DRIFT,
        BESLUTTER,
    }
}

internal fun Saksbehandler.toDTO(): SaksbehandlerDTO =
    SaksbehandlerDTO(
        navIdent = navIdent,
        brukernavn = brukernavn,
        epost = epost,
        roller = roller.map { it.toDTO() },
    )

internal fun Rolle.toDTO(): SaksbehandlerDTO.RolleDTO =
    when (this) {
        Rolle.SAKSBEHANDLER -> SaksbehandlerDTO.RolleDTO.SAKSBEHANDLER
        Rolle.FORTROLIG_ADRESSE -> SaksbehandlerDTO.RolleDTO.FORTROLIG_ADRESSE
        Rolle.STRENGT_FORTROLIG_ADRESSE -> SaksbehandlerDTO.RolleDTO.STRENGT_FORTROLIG_ADRESSE
        Rolle.SKJERMING -> SaksbehandlerDTO.RolleDTO.SKJERMING
        Rolle.LAGE_HENDELSER -> SaksbehandlerDTO.RolleDTO.LAGE_HENDELSER
        Rolle.DRIFT -> SaksbehandlerDTO.RolleDTO.DRIFT
        Rolle.BESLUTTER -> SaksbehandlerDTO.RolleDTO.BESLUTTER
    }
