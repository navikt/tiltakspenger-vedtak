package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import java.time.LocalDateTime
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall2

sealed interface LivsoppholdSaksopplysning {

    val livsoppholdsytelseType: LivsoppholdsytelseType
    val harYtelse: Periodisering<HarYtelse>
    val tidsstempel: LocalDateTime
    val totalePeriode: Periode

    val årsakTilEndring: ÅrsakTilEndring?
    val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler?
    fun vurderMaskinelt(): Periodisering<Utfall2>
}

interface LivsoppholdSaksopplysningFraSøknad : LivsoppholdSaksopplysning

sealed interface SupplerendeStønadAlderSaksopplysning : LivsoppholdSaksopplysning {

    data class Søknad(
        override val harYtelse: Periodisering<HarYtelse>,
        override val tidsstempel: LocalDateTime,
    ) : SupplerendeStønadAlderSaksopplysning, LivsoppholdSaksopplysningFraSøknad {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.SUPPLERENDESTØNAD_ALDER
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "SupplerendeStønadAlderSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : SupplerendeStønadAlderSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.SUPPLERENDESTØNAD_ALDER

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "SupplerendeStønadAlderSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}

sealed interface SupplerendeStønadFlyktningSaksopplysning : LivsoppholdSaksopplysning {

    data class Søknad(
        override val harYtelse: Periodisering<HarYtelse>,
        override val tidsstempel: LocalDateTime,
    ) : SupplerendeStønadFlyktningSaksopplysning, LivsoppholdSaksopplysningFraSøknad {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.SUPPLERENDESTØNAD_FLYKTNING
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "SupplerendeStønadFlyktningSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : SupplerendeStønadFlyktningSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.SUPPLERENDESTØNAD_FLYKTNING

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "SupplerendeStønadFlyktningSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}

sealed interface AlderspensjonSaksopplysning : LivsoppholdSaksopplysning {

    data class Søknad(
        override val harYtelse: Periodisering<HarYtelse>,
        override val tidsstempel: LocalDateTime,
    ) : AlderspensjonSaksopplysning, LivsoppholdSaksopplysningFraSøknad {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.ALDERSPENSJON
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "AlderspensjonSaksopplysning må ha minst én periode, men var tom." }

        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : AlderspensjonSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.ALDERSPENSJON

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "AlderspensjonSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}

sealed interface PensjonsinntektSaksopplysning : LivsoppholdSaksopplysning {

    data class Søknad(
        override val harYtelse: Periodisering<HarYtelse>,
        override val tidsstempel: LocalDateTime,
    ) : PensjonsinntektSaksopplysning, LivsoppholdSaksopplysningFraSøknad {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.PENSJONSINNTEKT
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "AlderspensjonSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : PensjonsinntektSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.PENSJONSINNTEKT

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "AlderspensjonSaksopplysning må ha minst én periode, men var tom." }
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
    ) : GjenlevendepensjonSaksopplysning, LivsoppholdSaksopplysningFraSøknad {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.GJENLEVENDEPENSJON
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "GjenlevendepensjonSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : GjenlevendepensjonSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.GJENLEVENDEPENSJON

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "GjenlevendepensjonSaksopplysning må ha minst én periode, men var tom." }
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
    ) : SykepengerSaksopplysning, LivsoppholdSaksopplysningFraSøknad {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.SYKEPENGER
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "SykepengerSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : SykepengerSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.SYKEPENGER

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "SykepengerSaksopplysning må ha minst én periode, men var tom." }
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
    ) : JobbsjansenSaksopplysning, LivsoppholdSaksopplysningFraSøknad {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.JOBBSJANSEN
        override val årsakTilEndring = null
        override val saksbehandler = null

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "JobbsjansenSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : JobbsjansenSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.JOBBSJANSEN

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "JobbsjansenSaksopplysning må ha minst én periode, men var tom." }
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
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : AAPSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.AAP

        init {
            require(harYtelse.perioder().isNotEmpty()) { "AAPSaksopplysning må ha minst én periode, men var tom." }
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
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : DagpengerSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.DAGPENGER

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "DagpengerSaksopplysning må ha minst én periode, men var tom." }
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
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : ForeldrepengerSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.FORELDREPENGER

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "ForeldrepengerSaksopplysning må ha minst én periode, men var tom." }
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
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : OmsorgspengerSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.OMSORGSPENGER

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "OmsorgspengerSaksopplysning må ha minst én periode, men var tom." }
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
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : OpplæringspengerSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.OPPLÆRINGSPENGER

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "OpplæringspengerSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}

sealed interface OvergangsstønadSaksopplysning : LivsoppholdSaksopplysning {

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : OvergangsstønadSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.OVERGANGSSTØNAD

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "OvergangsstønadSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}

sealed interface PleiepengerNærståendeSaksopplysning : LivsoppholdSaksopplysning {

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : PleiepengerNærståendeSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.PLEIEPENGER_NÆRSTÅENDE

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "PleiepengerNærståendeSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}

sealed interface PleiepengerSyktBarnSaksopplysning : LivsoppholdSaksopplysning {

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : PleiepengerSyktBarnSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.PLEIEPENGER_SYKTBARN

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "PleiepengerSyktBarnSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}

sealed interface SvangerskapspengerSaksopplysning : LivsoppholdSaksopplysning {

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : SvangerskapspengerSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.SVANGERSKAPSPENGER

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "SvangerskapspengerSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}

sealed interface UføretrygdSaksopplysning : LivsoppholdSaksopplysning {

    data class Saksbehandler(
        override val harYtelse: Periodisering<HarYtelse>,
        override val årsakTilEndring: ÅrsakTilEndring,
        override val tidsstempel: LocalDateTime,
        override val saksbehandler: no.nav.tiltakspenger.felles.Saksbehandler,
    ) : UføretrygdSaksopplysning {
        override val livsoppholdsytelseType: LivsoppholdsytelseType = LivsoppholdsytelseType.UFØRETRYGD

        init {
            require(
                harYtelse.perioder().isNotEmpty(),
            ) { "UføretrygdSaksopplysning må ha minst én periode, men var tom." }
        }

        override val totalePeriode: Periode = harYtelse.totalePeriode

        override fun vurderMaskinelt(): Periodisering<Utfall2> {
            return harYtelse.map { it.vurderMaskinelt() }
        }
    }
}
