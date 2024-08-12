package no.nav.tiltakspenger.vedtak.repository.felles

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.Rolle

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

        // Systemadministrator (oss)
        DRIFT,
        BESLUTTER,

        // Saksbehandlers administrator (superbruker)
        ADMINISTRATOR,
        ;

        fun toDomain(): Rolle =
            when (this) {
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

    fun toDomain(): Saksbehandler =
        Saksbehandler(
            navIdent = navIdent,
            brukernavn = brukernavn,
            epost = epost,
            roller = roller.map { it.toDomain() },
        )
}

internal fun Saksbehandler.toDbJson(): SaksbehandlerDbJson =
    SaksbehandlerDbJson(
        navIdent = navIdent,
        brukernavn = brukernavn,
        epost = epost,
        roller = roller.map { it.toDbJson() },
    )

internal fun Rolle.toDbJson(): SaksbehandlerDbJson.RolleDbJson =
    when (this) {
        Rolle.SAKSBEHANDLER -> SaksbehandlerDbJson.RolleDbJson.SAKSBEHANDLER
        Rolle.FORTROLIG_ADRESSE -> SaksbehandlerDbJson.RolleDbJson.FORTROLIG_ADRESSE
        Rolle.STRENGT_FORTROLIG_ADRESSE -> SaksbehandlerDbJson.RolleDbJson.STRENGT_FORTROLIG_ADRESSE
        Rolle.SKJERMING -> SaksbehandlerDbJson.RolleDbJson.SKJERMING
        Rolle.LAGE_HENDELSER -> SaksbehandlerDbJson.RolleDbJson.LAGE_HENDELSER
        Rolle.DRIFT -> SaksbehandlerDbJson.RolleDbJson.DRIFT
        Rolle.BESLUTTER -> SaksbehandlerDbJson.RolleDbJson.BESLUTTER
        Rolle.ADMINISTRATOR -> SaksbehandlerDbJson.RolleDbJson.ADMINISTRATOR
    }
