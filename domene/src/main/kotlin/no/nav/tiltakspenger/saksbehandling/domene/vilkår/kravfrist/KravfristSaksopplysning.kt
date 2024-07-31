package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

sealed interface KravfristSaksopplysning {
    val kravdato: LocalDateTime
    val tidsstempel: LocalDateTime

    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler?

    data class Søknad(
        override val kravdato: LocalDateTime,
        override val tidsstempel: LocalDateTime,
    ) : KravfristSaksopplysning {
        override val årsakTilEndring = null
        override val saksbehandler = null
    }

    data class Saksbehandler(
        override val kravdato: LocalDateTime,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : KravfristSaksopplysning
}
