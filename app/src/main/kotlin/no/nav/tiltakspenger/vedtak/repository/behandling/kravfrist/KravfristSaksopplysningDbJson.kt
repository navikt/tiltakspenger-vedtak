package no.nav.tiltakspenger.vedtak.repository.behandling.kravfrist

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist.KravfristSaksopplysning
import no.nav.tiltakspenger.vedtak.repository.behandling.felles.ÅrsakTilEndringDbType
import no.nav.tiltakspenger.vedtak.repository.felles.SaksbehandlerDbJson
import java.time.LocalDateTime

internal data class KravfristSaksopplysningDbJson(
    val kravdato: LocalDateTime,
    val årsakTilEndring: ÅrsakTilEndringDbType?,
    val saksbehandler: SaksbehandlerDbJson?,
    val tidsstempel: String,
) {
    fun toDomain(): KravfristSaksopplysning =
        when {
            saksbehandler != null -> {
                checkNotNull(årsakTilEndring) { "Årsak til endring er ikke satt for KravfristSaksopplysning fra saksbehandler." }

                KravfristSaksopplysning.Saksbehandler(
                    kravdato = kravdato,
                    årsakTilEndring = årsakTilEndring.toDomain(),
                    navIdent = saksbehandler.navIdent,
                    tidsstempel = LocalDateTime.parse(tidsstempel),
                )
            }

            else -> {
                require(årsakTilEndring == null) { "Støtter ikke årsak til endring for KravfristSaksopplysning.Søknad." }
                KravfristSaksopplysning.Søknad(
                    kravdato = kravdato,
                    tidsstempel = LocalDateTime.parse(tidsstempel),
                )
            }
        }
}

internal fun KravfristSaksopplysning.toDbJson(): KravfristSaksopplysningDbJson =
    KravfristSaksopplysningDbJson(
        kravdato = kravdato,
        årsakTilEndring =
        when (årsakTilEndring) {
            ÅrsakTilEndring.FEIL_I_INNHENTET_DATA -> ÅrsakTilEndringDbType.FEIL_I_INNHENTET_DATA
            ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT -> ÅrsakTilEndringDbType.ENDRING_ETTER_SØKNADSTIDSPUNKT
            null -> null
        },
        saksbehandler = navIdent?.let { SaksbehandlerDbJson(it) },
        tidsstempel = tidsstempel.toString(),
    )
