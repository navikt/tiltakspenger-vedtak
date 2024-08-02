package no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder

import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDate
import java.time.LocalDateTime

sealed interface AlderSaksopplysning {
    val fødselsdato: LocalDate
    val tidsstempel: LocalDateTime

    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler?

    data class Register(
        override val fødselsdato: LocalDate,
        override val tidsstempel: LocalDateTime,
    ) : AlderSaksopplysning {
        override val årsakTilEndring = null
        override val saksbehandler = null

        companion object {
            fun opprett(fødselsdato: LocalDate): AlderSaksopplysning.Register {
                return Register(fødselsdato = fødselsdato, tidsstempel = LocalDateTime.now())
            }
        }
        init {
            require(fødselsdato.isBefore(LocalDate.now())) { "Kan ikke ha fødselsdag frem i tid" }
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
    }
}
