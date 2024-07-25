package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltak

import no.nav.tiltakspenger.felles.exceptions.IkkeImplementertException
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

sealed interface TiltakSaksopplysning {
    val tiltak: LocalDateTime // TODO KEW
    val tidsstempel: LocalDateTime

    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler?
    fun vurderMaskinelt(): Periodisering<Utfall2>

    data class Søknad(
        override val tiltak: LocalDateTime,
        override val tidsstempel: LocalDateTime,
    ) : TiltakSaksopplysning {
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            // TODO kew passende init
        }

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            TODO()
        }
    }

    data class Saksbehandler(
        override val tiltak: LocalDateTime,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : TiltakSaksopplysning {
        init {
            // TODO kew passende init
        }

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            TODO()
        }
    }
}
