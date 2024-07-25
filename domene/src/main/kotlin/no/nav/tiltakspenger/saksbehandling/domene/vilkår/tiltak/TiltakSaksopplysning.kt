package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltak

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

sealed interface TiltakSaksopplysning {
    val tiltak: String
    val kilde: String
    val deltagelsePeriode: Periode
    val girRett: Boolean
    val status: String
    val tidsstempel: LocalDateTime

    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler?
    fun vurderMaskinelt(): Periodisering<Utfall2>

    data class Tiltak(
        override val tiltak: String,
        override val tidsstempel: LocalDateTime,
        override val deltagelsePeriode: Periode,
        override val girRett: Boolean,
        override val status: String,
        override val kilde: String,
    ) : TiltakSaksopplysning {
        override val årsakTilEndring = null
        override val saksbehandler = null

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return when {
                girRett -> Periodisering(Utfall2.OPPFYLT, deltagelsePeriode)
                !girRett -> Periodisering(Utfall2.IKKE_OPPFYLT, deltagelsePeriode)
                else -> {
                    Periodisering(Utfall2.IKKE_OPPFYLT, deltagelsePeriode)
                }
            }
        }
    }

    data class Saksbehandler(
        override val tiltak: String,
        override val kilde: String,
        override val deltagelsePeriode: Periode,
        override val girRett: Boolean,
        override val status: String,
        override val tidsstempel: LocalDateTime,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : TiltakSaksopplysning {
        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return when {
                girRett -> Periodisering(Utfall2.OPPFYLT, deltagelsePeriode)
                !girRett -> Periodisering(Utfall2.IKKE_OPPFYLT, deltagelsePeriode)
                else -> {
                    Periodisering(Utfall2.IKKE_OPPFYLT, deltagelsePeriode)
                }
            }
        }
    }
}
