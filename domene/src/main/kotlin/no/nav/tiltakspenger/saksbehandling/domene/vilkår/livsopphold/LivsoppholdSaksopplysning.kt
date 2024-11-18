package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import no.nav.tiltakspenger.felles.exceptions.IkkeImplementertException
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

sealed interface LivsoppholdSaksopplysning {
    val harLivsoppholdYtelser: Boolean
    val tidsstempel: LocalDateTime
    val periode: Periode

    val årsakTilEndring: ÅrsakTilEndring?
    val navIdent: String?

    data class Søknad(
        override val harLivsoppholdYtelser: Boolean,
        override val tidsstempel: LocalDateTime,
        override val periode: Periode,
    ) : LivsoppholdSaksopplysning {
        override val navIdent = null
        override val årsakTilEndring = null
    }

    data class Saksbehandler(
        override val harLivsoppholdYtelser: Boolean,
        override val årsakTilEndring: ÅrsakTilEndring?,
        override val tidsstempel: LocalDateTime,
        override val navIdent: String,
        override val periode: Periode,
    ) : LivsoppholdSaksopplysning {
        init {
            if (harLivsoppholdYtelser) {
                throw IkkeImplementertException("Støtter ikke avslag enda.")
            }
        }
    }
}
