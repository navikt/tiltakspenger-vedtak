package no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.IntroSaksopplysning.Søknad
import java.time.LocalDateTime

sealed interface KravfristSaksopplysning {
    fun oppdaterPeriode(periode: Periode): KravfristSaksopplysning

    val kravdato: LocalDateTime
    val tidsstempel: LocalDateTime

    val årsakTilEndring: ÅrsakTilEndring?
    val navIdent: String?

    data class Søknad(
        override val kravdato: LocalDateTime,
        override val tidsstempel: LocalDateTime,
    ) : KravfristSaksopplysning {
        override val årsakTilEndring = null
        override val navIdent = null

        /** NOOP - men åpner for muligheten å periodisere denne */
        override fun oppdaterPeriode(periode: Periode): Søknad {
            return this
        }
    }

    data class Saksbehandler(
        override val kravdato: LocalDateTime,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val navIdent: String,
    ) : KravfristSaksopplysning {

        /** NOOP - men åpner for muligheten å periodisere denne */
        override fun oppdaterPeriode(periode: Periode): Saksbehandler {
            return this
        }
    }
}
