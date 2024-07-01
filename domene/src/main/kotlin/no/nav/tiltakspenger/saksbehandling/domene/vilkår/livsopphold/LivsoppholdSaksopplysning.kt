package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2
import java.time.LocalDateTime

sealed interface LivsoppholdSaksopplysning {

    val livsoppholdsytelse: Livsoppholdsytelse
    val harYtelse: Periodisering<HarYtelse>
    val tidsstempel: LocalDateTime
    val totalePeriode: Periode

    val årsakTilEndring: AarsakTilEndring?
    val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler?
    fun vurderMaskinelt(): Periodisering<Utfall2>
}

sealed interface AlderspensjonSaksopplysning : LivsoppholdSaksopplysning {

    data class Søknad(
        override val harYtelse: Periodisering<HarYtelse>,
        override val tidsstempel: LocalDateTime,
    ) : AlderspensjonSaksopplysning {
        override val livsoppholdsytelse: Livsoppholdsytelse = Livsoppholdsytelse.ALDERSPENSJON
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            require(harYtelse.perioder().isNotEmpty()) { "KvpSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: AarsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : AlderspensjonSaksopplysning {
        override val livsoppholdsytelse: Livsoppholdsytelse = Livsoppholdsytelse.ALDERSPENSJON

        init {
            require(harYtelse.perioder().isNotEmpty()) { "KvpSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}

sealed interface GjenlevendepensjonSaksopplysning : LivsoppholdSaksopplysning {

    data class Søknad(
        override val harYtelse: Periodisering<HarYtelse>,
        override val tidsstempel: LocalDateTime,
    ) : GjenlevendepensjonSaksopplysning {
        override val livsoppholdsytelse: Livsoppholdsytelse = Livsoppholdsytelse.GJENLEVENDEPENSJON
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            require(harYtelse.perioder().isNotEmpty()) { "KvpSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: AarsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : GjenlevendepensjonSaksopplysning {
        override val livsoppholdsytelse: Livsoppholdsytelse = Livsoppholdsytelse.GJENLEVENDEPENSJON

        init {
            require(harYtelse.perioder().isNotEmpty()) { "KvpSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}

sealed interface SykepengerSaksopplysning : LivsoppholdSaksopplysning {

    data class Søknad(
        override val harYtelse: Periodisering<HarYtelse>,
        override val tidsstempel: LocalDateTime,
    ) : SykepengerSaksopplysning {
        override val livsoppholdsytelse: Livsoppholdsytelse = Livsoppholdsytelse.SYKEPENGER
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            require(harYtelse.perioder().isNotEmpty()) { "KvpSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: AarsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : SykepengerSaksopplysning {
        override val livsoppholdsytelse: Livsoppholdsytelse = Livsoppholdsytelse.SYKEPENGER

        init {
            require(harYtelse.perioder().isNotEmpty()) { "KvpSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}

sealed interface JobbsjansenSaksopplysning : LivsoppholdSaksopplysning {

    data class Søknad(
        override val harYtelse: Periodisering<HarYtelse>,
        override val tidsstempel: LocalDateTime,
    ) : JobbsjansenSaksopplysning {
        override val livsoppholdsytelse: Livsoppholdsytelse = Livsoppholdsytelse.JOBBSJANSEN
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            require(harYtelse.perioder().isNotEmpty()) { "KvpSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: AarsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : JobbsjansenSaksopplysning {
        override val livsoppholdsytelse: Livsoppholdsytelse = Livsoppholdsytelse.JOBBSJANSEN

        init {
            require(harYtelse.perioder().isNotEmpty()) { "KvpSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}

sealed interface AAPSaksopplysning : LivsoppholdSaksopplysning {

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: AarsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : AAPSaksopplysning {
        override val livsoppholdsytelse: Livsoppholdsytelse = Livsoppholdsytelse.AAP

        init {
            require(harYtelse.perioder().isNotEmpty()) { "KvpSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}

sealed interface DagpengerSaksopplysning : LivsoppholdSaksopplysning {

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: AarsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : DagpengerSaksopplysning {
        override val livsoppholdsytelse: Livsoppholdsytelse = Livsoppholdsytelse.DAGPENGER

        init {
            require(harYtelse.perioder().isNotEmpty()) { "KvpSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}

sealed interface ForeldrepengerSaksopplysning : LivsoppholdSaksopplysning {

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: AarsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : ForeldrepengerSaksopplysning {
        override val livsoppholdsytelse: Livsoppholdsytelse = Livsoppholdsytelse.FORELDREPENGER

        init {
            require(harYtelse.perioder().isNotEmpty()) { "KvpSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}

sealed interface OmsorgspengerSaksopplysning : LivsoppholdSaksopplysning {

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: AarsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : OmsorgspengerSaksopplysning {
        override val livsoppholdsytelse: Livsoppholdsytelse = Livsoppholdsytelse.OMSORGSPENGER

        init {
            require(harYtelse.perioder().isNotEmpty()) { "KvpSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}

sealed interface OpplæringspengerSaksopplysning : LivsoppholdSaksopplysning {

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: AarsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : OpplæringspengerSaksopplysning {
        override val livsoppholdsytelse: Livsoppholdsytelse = Livsoppholdsytelse.OPPLÆRINGSPENGER

        init {
            require(harYtelse.perioder().isNotEmpty()) { "KvpSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}
