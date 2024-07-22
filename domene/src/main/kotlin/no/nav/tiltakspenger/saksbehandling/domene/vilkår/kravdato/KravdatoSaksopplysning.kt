package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravdato

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

sealed interface KravdatoSaksopplysning {
    val kravdato: LocalDateTime
    val tidsstempel: LocalDateTime

    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler?
    fun vurderMaskinelt(vurderingsperiode: Periode): Periodisering<Utfall2>

    data class Søknad(
        override val kravdato: LocalDateTime,
        override val tidsstempel: LocalDateTime,
    ) : KravdatoSaksopplysning {
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            // TODO kew passende init
        }

        override fun vurderMaskinelt(vurderingsperiode: Periode): Periodisering<Utfall2> {
            TODO()
            // kew
//            return when {
//                dagenBrukerFyller18År.isBefore(vurderingsperiode.fraOgMed) -> Periodisering(Utfall2.OPPFYLT, vurderingsperiode)
//                dagenBrukerFyller18År.isAfter(vurderingsperiode.tilOgMed) -> Periodisering(Utfall2.IKKE_OPPFYLT, vurderingsperiode)
//                else -> {
//                    Periodisering(Utfall2.IKKE_OPPFYLT, vurderingsperiode)
//                }
//            }
        }
    }

    data class Saksbehandler(
        override val kravdato: LocalDateTime,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : KravdatoSaksopplysning {
        init {
            // TODO kew passende init
        }

        override fun vurderMaskinelt(vurderingsperiode: Periode): Periodisering<Utfall2> {
            TODO()
            // kew
//            return when {
//                dagenBrukerFyller18År.isBefore(vurderingsperiode.fraOgMed) -> Periodisering(Utfall2.OPPFYLT, vurderingsperiode)
//                dagenBrukerFyller18År.isAfter(vurderingsperiode.tilOgMed) -> Periodisering(Utfall2.IKKE_OPPFYLT, vurderingsperiode)
//                else -> {
//                    Periodisering(Utfall2.IKKE_OPPFYLT, vurderingsperiode)
//                }
//            }
        }
    }
}
