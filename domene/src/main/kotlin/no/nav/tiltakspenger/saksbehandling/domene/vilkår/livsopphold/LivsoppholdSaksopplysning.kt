package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import no.nav.tiltakspenger.felles.exceptions.IkkeImplementertException
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

sealed interface LivsoppholdSaksopplysning {
    val harLivsoppholdYtelser: Boolean
    val tidsstempel: LocalDateTime
    val periode: Periode

    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler?
    fun vurderMaskinelt(): Periodisering<Utfall2>

    data class Søknad(
        override val harLivsoppholdYtelser: Boolean,
        override val tidsstempel: LocalDateTime,
        override val periode: Periode,
    ) : LivsoppholdSaksopplysning {
        override val årsakTilEndring = null
        override val saksbehandler = null

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return when (harLivsoppholdYtelser) {
                true -> Periodisering(
                    Utfall2.IKKE_OPPFYLT,
                    periode
                )
                false -> Periodisering(
                    Utfall2.OPPFYLT,
                    periode
                )
            }
        }
    }

    data class Saksbehandler(
        override val harLivsoppholdYtelser: Boolean,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
        override val periode: Periode,
    ) : LivsoppholdSaksopplysning {
        init {
            if (harLivsoppholdYtelser) {
                throw IkkeImplementertException("Støtter ikke avslag enda.")
            }
        }

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return when (harLivsoppholdYtelser) {
                true -> throw IkkeImplementertException("Støtter ikke avslag enda.")
                false -> Periodisering(
                    Utfall2.OPPFYLT,
                    periode
                )
            }
        }
    }
}
