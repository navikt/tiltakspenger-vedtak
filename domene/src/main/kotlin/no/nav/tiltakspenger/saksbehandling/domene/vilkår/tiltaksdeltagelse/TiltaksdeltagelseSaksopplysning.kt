package no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltaksdeltagelse

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import java.time.LocalDateTime

sealed interface TiltaksdeltagelseSaksopplysning {
    val tiltaksnavn: String
    val eksternDeltagelseId: String
    val gjennomføringId: String?
    val kilde: Tiltakskilde

    // TODO jah: Bør endres til en periodisering som de andre saksopplysningene på de andre vilkårene. Man kan gjenbruke den periodiserte datatypen på tvers av saksopplysningene.
    val deltagelsePeriode: Periode
    val girRett: Boolean
    val status: TiltakDeltakerstatus
    val tidsstempel: LocalDateTime
    val tiltakstype: TiltakstypeSomGirRett
    val årsakTilEndring: ÅrsakTilEndring?
    val navIdent: String?

    fun oppdaterPeriode(periode: Periode): TiltaksdeltagelseSaksopplysning

    data class Register(
        override val tiltaksnavn: String,
        override val eksternDeltagelseId: String,
        override val gjennomføringId: String?,
        override val tidsstempel: LocalDateTime,
        override val deltagelsePeriode: Periode,
        override val girRett: Boolean,
        override val status: TiltakDeltakerstatus,
        override val kilde: Tiltakskilde,
        override val tiltakstype: TiltakstypeSomGirRett,
    ) : TiltaksdeltagelseSaksopplysning {
        override val årsakTilEndring = null
        override val navIdent = null

        /** Støtter i førsteomgang kun å krympe perioden. Dersom man skulle utvidet den, måtte man gjort en ny vurdering og ville derfor hatt en ny saksopplysning. */
        override fun oppdaterPeriode(periode: Periode): Register {
            return copy(deltagelsePeriode = periode)
        }
    }

    data class Saksbehandler(
        override val tiltaksnavn: String,
        override val eksternDeltagelseId: String,
        override val gjennomføringId: String?,
        override val tidsstempel: LocalDateTime,
        override val deltagelsePeriode: Periode,
        override val girRett: Boolean,
        override val status: TiltakDeltakerstatus,
        override val kilde: Tiltakskilde,
        override val tiltakstype: TiltakstypeSomGirRett,
        override val navIdent: String,
        override val årsakTilEndring: ÅrsakTilEndring,
    ) : TiltaksdeltagelseSaksopplysning {
        override fun oppdaterPeriode(periode: Periode): Saksbehandler {
            return copy(deltagelsePeriode = periode)
        }
    }
}
