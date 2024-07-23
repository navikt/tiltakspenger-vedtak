package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravdato

import no.nav.tiltakspenger.felles.exceptions.IkkeImplementertException
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

sealed interface KravdatoSaksopplysning {
    val kravdato: LocalDateTime
    val vurderingsperiode: Periode
    val tidsstempel: LocalDateTime

    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler?
    fun vurderMaskinelt(): Periodisering<Utfall2>

    data class Søknad(
        override val kravdato: LocalDateTime,
        override val tidsstempel: LocalDateTime,
        override val vurderingsperiode: Periode,
    ) : KravdatoSaksopplysning {
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            // TODO kew passende init
        }

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            val datoDetKanInnvilgesFra = kravdato.withDayOfMonth(1).minusMonths(3).toLocalDate()

            return when {
                datoDetKanInnvilgesFra <= vurderingsperiode.fraOgMed -> Periodisering(Utfall2.OPPFYLT, vurderingsperiode)
                datoDetKanInnvilgesFra > vurderingsperiode.tilOgMed -> Periodisering(Utfall2.IKKE_OPPFYLT, vurderingsperiode)
                else -> throw IkkeImplementertException("Støtter ikke at kravdatoen er midt i vurderingsperioden.")
            }
        }
    }

    data class Saksbehandler(
        override val kravdato: LocalDateTime,
        override val vurderingsperiode: Periode,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : KravdatoSaksopplysning {
        init {
            // TODO kew passende init
        }

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            val datoDetKanInnvilgesFra = kravdato.withDayOfMonth(1).minusMonths(3).toLocalDate()

            return when {
                datoDetKanInnvilgesFra <= vurderingsperiode.fraOgMed -> Periodisering(Utfall2.OPPFYLT, vurderingsperiode)
                datoDetKanInnvilgesFra > vurderingsperiode.tilOgMed -> Periodisering(Utfall2.IKKE_OPPFYLT, vurderingsperiode)
                else -> throw IkkeImplementertException("Støtter ikke at kravdatoen er midt i vurderingsperioden.")
            }
        }
    }
}
