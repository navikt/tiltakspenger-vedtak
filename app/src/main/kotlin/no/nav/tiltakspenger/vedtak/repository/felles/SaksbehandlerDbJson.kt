package no.nav.tiltakspenger.vedtak.repository.felles

import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler

internal data class SaksbehandlerDbJson(
    val navIdent: String,
    val brukernavn: String,
    val epost: String,
    val roller: List<RolleDbJson>,
) {
    enum class RolleDbJson {
        SAKSBEHANDLER,
        FORTROLIG_ADRESSE,
        STRENGT_FORTROLIG_ADRESSE,
        SKJERMING,
        LAGE_HENDELSER,
        DRIFT, // Systemadministrator (oss)
        BESLUTTER,
        ADMINISTRATOR, // Saksbehandlers administrator (superbruker)
        ;

        fun toDomain(): Rolle {
            return when (this) {
                SAKSBEHANDLER -> Rolle.SAKSBEHANDLER
                FORTROLIG_ADRESSE -> Rolle.FORTROLIG_ADRESSE
                STRENGT_FORTROLIG_ADRESSE -> Rolle.STRENGT_FORTROLIG_ADRESSE
                SKJERMING -> Rolle.SKJERMING
                LAGE_HENDELSER -> Rolle.LAGE_HENDELSER
                DRIFT -> Rolle.DRIFT
                BESLUTTER -> Rolle.BESLUTTER
                ADMINISTRATOR -> Rolle.ADMINISTRATOR
            }
        }
    }

    fun toDomain(): Saksbehandler {
        return Saksbehandler(
            navIdent = navIdent,
            brukernavn = brukernavn,
            epost = epost,
            roller = roller.map { it.toDomain() },
        )
    }
}

internal fun Saksbehandler.toDbJson(): SaksbehandlerDbJson {
    return SaksbehandlerDbJson(
        navIdent = navIdent,
        brukernavn = brukernavn,
        epost = epost,
        roller = roller.map { it.toDbJson() },
    )
}

internal fun Rolle.toDbJson(): SaksbehandlerDbJson.RolleDbJson {
    return when (this) {
        Rolle.SAKSBEHANDLER -> SaksbehandlerDbJson.RolleDbJson.SAKSBEHANDLER
        Rolle.FORTROLIG_ADRESSE -> SaksbehandlerDbJson.RolleDbJson.FORTROLIG_ADRESSE
        Rolle.STRENGT_FORTROLIG_ADRESSE -> SaksbehandlerDbJson.RolleDbJson.STRENGT_FORTROLIG_ADRESSE
        Rolle.SKJERMING -> SaksbehandlerDbJson.RolleDbJson.SKJERMING
        Rolle.LAGE_HENDELSER -> SaksbehandlerDbJson.RolleDbJson.LAGE_HENDELSER
        Rolle.DRIFT -> SaksbehandlerDbJson.RolleDbJson.DRIFT
        Rolle.BESLUTTER -> SaksbehandlerDbJson.RolleDbJson.BESLUTTER
        Rolle.ADMINISTRATOR -> SaksbehandlerDbJson.RolleDbJson.ADMINISTRATOR
    }
}
