package no.nav.tiltakspenger.vedtak.repository.felles

import no.nav.tiltakspenger.libs.common.Saksbehandler
import no.nav.tiltakspenger.libs.common.Saksbehandlerrolle
import no.nav.tiltakspenger.libs.common.Saksbehandlerroller

internal data class SaksbehandlerDbJson(
    val navIdent: String,
    val brukernavn: String,
    val epost: String,
    val roller: List<RolleDbJson>,
) {
    enum class RolleDbJson {
        SAKSBEHANDLER,
        BESLUTTER,

        FORTROLIG_ADRESSE,
        STRENGT_FORTROLIG_ADRESSE,
        SKJERMING,

        DRIFT,
        ;

        fun toDomain(): Saksbehandlerrolle =
            when (this) {
                SAKSBEHANDLER -> Saksbehandlerrolle.SAKSBEHANDLER
                FORTROLIG_ADRESSE -> Saksbehandlerrolle.FORTROLIG_ADRESSE
                STRENGT_FORTROLIG_ADRESSE -> Saksbehandlerrolle.STRENGT_FORTROLIG_ADRESSE
                SKJERMING -> Saksbehandlerrolle.SKJERMING
                DRIFT -> Saksbehandlerrolle.DRIFT
                BESLUTTER -> Saksbehandlerrolle.BESLUTTER
            }
    }

    fun toDomain(): Saksbehandler =
        Saksbehandler(
            navIdent = navIdent,
            brukernavn = brukernavn,
            epost = epost,
            roller = Saksbehandlerroller(roller.map { it.toDomain() }),
        )
}

internal fun Saksbehandler.toDbJson(): SaksbehandlerDbJson =
    SaksbehandlerDbJson(
        navIdent = navIdent,
        brukernavn = brukernavn,
        epost = epost,
        roller = roller.map { it.toDbJson() },
    )

internal fun Saksbehandlerrolle.toDbJson(): SaksbehandlerDbJson.RolleDbJson =
    when (this) {
        Saksbehandlerrolle.SAKSBEHANDLER -> SaksbehandlerDbJson.RolleDbJson.SAKSBEHANDLER
        Saksbehandlerrolle.FORTROLIG_ADRESSE -> SaksbehandlerDbJson.RolleDbJson.FORTROLIG_ADRESSE
        Saksbehandlerrolle.STRENGT_FORTROLIG_ADRESSE -> SaksbehandlerDbJson.RolleDbJson.STRENGT_FORTROLIG_ADRESSE
        Saksbehandlerrolle.SKJERMING -> SaksbehandlerDbJson.RolleDbJson.SKJERMING
        Saksbehandlerrolle.DRIFT -> SaksbehandlerDbJson.RolleDbJson.DRIFT
        Saksbehandlerrolle.BESLUTTER -> SaksbehandlerDbJson.RolleDbJson.BESLUTTER
    }
