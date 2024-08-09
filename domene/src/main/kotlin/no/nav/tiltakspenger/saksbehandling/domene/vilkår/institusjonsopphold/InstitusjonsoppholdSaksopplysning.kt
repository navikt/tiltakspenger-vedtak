package no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

sealed interface InstitusjonsoppholdSaksopplysning {
    val opphold: Periodisering<Opphold>
    val tidsstempel: LocalDateTime
    val totalePeriode: Periode

    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler?

    data class Søknad(
        override val opphold: Periodisering<Opphold>,
        override val tidsstempel: LocalDateTime,
    ) : InstitusjonsoppholdSaksopplysning {
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            require(opphold.perioder().isNotEmpty()) { "InstitusjonsoppholdSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = opphold.totalePeriode
    }

    data class Saksbehandler(
        override val opphold: Periodisering<Opphold>,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : InstitusjonsoppholdSaksopplysning {
        init {
            require(opphold.perioder().isNotEmpty()) { "InstitusjonsoppholdSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = opphold.totalePeriode
    }
}
