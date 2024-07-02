package no.nav.tiltakspenger.vedtak.repository.behandling.livsopphold

import java.time.LocalDateTime
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.AAPSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.AlderspensjonSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.DagpengerSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.ForeldrepengerSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.GjenlevendepensjonSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.JobbsjansenSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.OmsorgspengerSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.OpplæringspengerSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.OvergangsstønadSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.PensjonsinntektSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.PleiepengerNærståendeSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.PleiepengerSyktBarnSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.SupplerendeStønadAlderSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.SupplerendeStønadFlyktningSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.SvangerskapspengerSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.SykepengerSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.UføretrygdSaksopplysning
import no.nav.tiltakspenger.vedtak.repository.felles.SaksbehandlerDbJson
import no.nav.tiltakspenger.vedtak.repository.felles.toDbJson

internal data class LivsoppholdSaksopplysningDbJson(
    val livsoppholdsytelseType: LivsoppholdsytelseTypeDbJson,
    val harYtelseForPeriode: List<PeriodiseringAvHarYtelseDbJson>,
    val tidsstempel: String,
    val årsakTilEndring: ÅrsakTilEndringDbJson?,
    val saksbehandler: SaksbehandlerDbJson?,
) {
    fun toAapSaksbehandlerDomain(): AAPSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return AAPSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toDagpengerSaksbehandlerDomain(): DagpengerSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return DagpengerSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toForeldrepengerSaksbehandlerDomain(): ForeldrepengerSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return ForeldrepengerSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toOmsorgspengerSaksbehandlerDomain(): OmsorgspengerSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return OmsorgspengerSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toOpplæringspengerSaksbehandlerDomain(): OpplæringspengerSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return OpplæringspengerSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toOvergangsstønadSaksbehandlerDomain(): OvergangsstønadSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return OvergangsstønadSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toPleiepengerNærståendeSaksbehandlerDomain(): PleiepengerNærståendeSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return PleiepengerNærståendeSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toPleiepengerSyktBarnSaksbehandlerDomain(): PleiepengerSyktBarnSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return PleiepengerSyktBarnSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toSvangerskapspengerSaksbehandlerDomain(): SvangerskapspengerSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return SvangerskapspengerSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toUføretrygdSaksbehandlerDomain(): UføretrygdSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return UføretrygdSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toAlderspensjonSaksbehandlerDomain(): AlderspensjonSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return AlderspensjonSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toGjenlevendepensjonSaksbehandlerDomain(): GjenlevendepensjonSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return GjenlevendepensjonSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toSykepengerSaksbehandlerDomain(): SykepengerSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return SykepengerSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toJobbsjansenSaksbehandlerDomain(): JobbsjansenSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return JobbsjansenSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toPensjonsinntektSaksbehandlerDomain(): PensjonsinntektSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return PensjonsinntektSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toSupplerendestønadAlderSaksbehandlerDomain(): SupplerendeStønadAlderSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return SupplerendeStønadAlderSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toSupplerendeStønadFlyktningSaksbehandlerDomain(): SupplerendeStønadFlyktningSaksopplysning.Saksbehandler {
        require(årsakTilEndring != null) { "Krever årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler != null) { "Krever saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return SupplerendeStønadFlyktningSaksopplysning.Saksbehandler(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            årsakTilEndring = this.årsakTilEndring.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
            saksbehandler = this.saksbehandler.toDomain(),
        )
    }

    fun toAlderspensjonSøknadDomain(): AlderspensjonSaksopplysning.Søknad {
        require(årsakTilEndring == null) { "Kan ikke ha årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler == null) { "Kan ikke ha saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return AlderspensjonSaksopplysning.Søknad(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
        )
    }

    fun toGjenlevendepensjonSøknadDomain(): GjenlevendepensjonSaksopplysning.Søknad {
        require(årsakTilEndring == null) { "Kan ikke ha årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler == null) { "Kan ikke ha saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return GjenlevendepensjonSaksopplysning.Søknad(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
        )
    }

    fun toSykepengerSøknadDomain(): SykepengerSaksopplysning.Søknad {
        require(årsakTilEndring == null) { "Kan ikke ha årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler == null) { "Kan ikke ha saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return SykepengerSaksopplysning.Søknad(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
        )
    }

    fun toJobbsjansenSøknadDomain(): JobbsjansenSaksopplysning.Søknad {
        require(årsakTilEndring == null) { "Kan ikke ha årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler == null) { "Kan ikke ha saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return JobbsjansenSaksopplysning.Søknad(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
        )
    }

    fun toPensjonsinntektSøknadDomain(): PensjonsinntektSaksopplysning.Søknad {
        require(årsakTilEndring == null) { "Kan ikke ha årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler == null) { "Kan ikke ha saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return PensjonsinntektSaksopplysning.Søknad(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
        )
    }

    fun toSupplerendestønadAlderSøknadDomain(): SupplerendeStønadAlderSaksopplysning.Søknad {
        require(årsakTilEndring == null) { "Kan ikke ha årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler == null) { "Kan ikke ha saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return SupplerendeStønadAlderSaksopplysning.Søknad(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
        )
    }

    fun toSupplerendestønadFlyktningSøknadDomain(): SupplerendeStønadFlyktningSaksopplysning.Søknad {
        require(årsakTilEndring == null) { "Kan ikke ha årsak til endring for XYZSaksopplysning.Saksbehandler." }
        require(saksbehandler == null) { "Kan ikke ha saksbehandler for XYZSaksopplysning.Saksbehandler." }
        return SupplerendeStønadFlyktningSaksopplysning.Søknad(
            harYtelse = this.harYtelseForPeriode.toDomain(),
            tidsstempel = LocalDateTime.parse(this.tidsstempel),
        )
    }
}

internal fun LivsoppholdSaksopplysning.toDbJson(): LivsoppholdSaksopplysningDbJson {
    return LivsoppholdSaksopplysningDbJson(
        livsoppholdsytelseType = livsoppholdsytelseType.toDbJson(),
        harYtelseForPeriode = harYtelse.toDbJson(),
        tidsstempel = tidsstempel.toString(),
        årsakTilEndring = årsakTilEndring?.toDbJson(),
        saksbehandler = saksbehandler?.toDbJson(),
    )
}
