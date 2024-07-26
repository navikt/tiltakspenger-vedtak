package no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDate
import java.time.LocalDateTime

sealed interface AlderSaksopplysning {
    val fødselsdato: LocalDate
    val tidsstempel: LocalDateTime

    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler?
    fun vurderMaskinelt(vurderingsperiode: Periode): Periodisering<Utfall2>

    data class Personopplysning(
        override val fødselsdato: LocalDate,
        override val tidsstempel: LocalDateTime,
    ) : AlderSaksopplysning {
        override val årsakTilEndring = null
        override val saksbehandler = null

        companion object {
            fun opprett(fødselsdato: LocalDate): AlderSaksopplysning {
                return Personopplysning(fødselsdato = fødselsdato, tidsstempel = LocalDateTime.now())
            }
        }
        init {
            require(fødselsdato.isBefore(LocalDate.now())) { "Kan ikke ha fødselsdag frem i tid" }
        }

        override fun vurderMaskinelt(vurderingsperiode: Periode): Periodisering<Utfall2> {
            // Om noen har bursdag 29. mars (skuddår) og de akuratt har fylt 18 vil fødselsdagen bli satt til 28. mars, og de vil få krav på tiltakspenger én dag før de er 18.
            // Dette er så cornercase at vi per nå ikke bruker tid på å skrive en egen håndtering av 'plusYears()' for å støtte dette caset.
            val dagenBrukerFyller18År = fødselsdato.plusYears(18)
            return when {
                dagenBrukerFyller18År.isBefore(vurderingsperiode.fraOgMed) -> Periodisering(Utfall2.OPPFYLT, vurderingsperiode)
                dagenBrukerFyller18År.isAfter(vurderingsperiode.tilOgMed) -> Periodisering(Utfall2.IKKE_OPPFYLT, vurderingsperiode)
                else -> {
                    Periodisering(Utfall2.IKKE_OPPFYLT, vurderingsperiode)
                }
            }
        }
    }

    data class Saksbehandler(
        override val fødselsdato: LocalDate,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : AlderSaksopplysning {
        init {
            require(fødselsdato.isBefore(LocalDate.now())) { "Kan ikke ha fødselsdag frem i tid" }
        }

        override fun vurderMaskinelt(vurderingsperiode: Periode): Periodisering<Utfall2> {
            // Om noen har bursdag 29. mars (skuddår) og de akuratt har fylt 18 vil fødselsdagen bli satt til 28. mars, og de vil få krav på tiltakspenger én dag før de er 18.
            // Dette er så cornercase at vi per nå ikke bruker tid på å skrive en egen håndtering av 'plusYears()' for å støtte dette caset.
            val dagenBrukerFyller18År = fødselsdato.plusYears(18)
            return when {
                dagenBrukerFyller18År.isBefore(vurderingsperiode.fraOgMed) -> Periodisering(Utfall2.OPPFYLT, vurderingsperiode)
                dagenBrukerFyller18År.isAfter(vurderingsperiode.tilOgMed) -> Periodisering(Utfall2.IKKE_OPPFYLT, vurderingsperiode)
                else -> {
                    Periodisering(Utfall2.IKKE_OPPFYLT, vurderingsperiode)
                }
            }
        }
    }
}
