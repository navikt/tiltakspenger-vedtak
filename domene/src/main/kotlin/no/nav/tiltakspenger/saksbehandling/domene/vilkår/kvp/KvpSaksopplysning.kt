package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.Deltagelse
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

sealed interface KvpSaksopplysning {

    val deltar: Periodisering<Deltagelse>
    val tidsstempel: LocalDateTime
    val totalePeriode: Periode

    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler?
    fun vurderMaskinelt(): Periodisering<Utfall2>

    data class Søknad(
        override val deltar: Periodisering<Deltagelse>,
        override val tidsstempel: LocalDateTime,
    ) : KvpSaksopplysning {
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            require(deltar.perioder().isNotEmpty()) { "KvpSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = deltar.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return deltar.map { it.vurderMaskinelt() }
        }
    }

    data class Saksbehandler(
        override val deltar: Periodisering<Deltagelse>,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : KvpSaksopplysning {
        init {
            require(deltar.perioder().isNotEmpty()) { "KvpSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = deltar.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return deltar.map { it.vurderMaskinelt() }
        }
    }
}
