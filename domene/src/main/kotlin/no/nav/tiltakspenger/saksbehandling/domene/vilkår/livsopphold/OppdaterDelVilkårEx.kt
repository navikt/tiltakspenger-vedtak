package no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold

import java.time.LocalDateTime
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.AAPDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.AlderspensjonDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.DagpengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.ForeldrepengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.GjenlevendepensjonDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.JobbsjansenDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.LivsoppholdDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.OmsorgspengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.OpplæringspengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.OvergangsstønadDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.PensjonsinntektDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.PleiepengerNærståendeDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.PleiepengerSyktBarnDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.SupplerendestønadAlderDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.SupplerendestønadFlyktningDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.SvangerskapspengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.SykepengerDelVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.delvilkår.UføretrygdDelVilkår

fun LivsoppholdVilkår.oppdaterDelVilkår(
    command: LeggTilLivsoppholdSaksopplysningCommand,
): Map<LivsoppholdsytelseType, LivsoppholdDelVilkår> =
    when (val type = command.livsoppholdsytelseType) {
        LivsoppholdsytelseType.AAP -> {
            val saksopplysning = AAPSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as AAPDelVilkår).leggTilSaksopplysning(saksopplysning))
        }

        LivsoppholdsytelseType.DAGPENGER -> {
            val saksopplysning = DagpengerSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as DagpengerDelVilkår).leggTilSaksopplysning(saksopplysning))
        }

        LivsoppholdsytelseType.ALDERSPENSJON -> {
            val saksopplysning = AlderspensjonSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as AlderspensjonDelVilkår).leggTilSaksopplysning(
                saksopplysning,
            ))
        }

        LivsoppholdsytelseType.GJENLEVENDEPENSJON -> {
            val saksopplysning = GjenlevendepensjonSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as GjenlevendepensjonDelVilkår).leggTilSaksopplysning(
                saksopplysning,
            ))
        }

        LivsoppholdsytelseType.SYKEPENGER -> {
            val saksopplysning = SykepengerSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as SykepengerDelVilkår).leggTilSaksopplysning(saksopplysning))
        }

        LivsoppholdsytelseType.JOBBSJANSEN -> {
            val saksopplysning = JobbsjansenSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as JobbsjansenDelVilkår).leggTilSaksopplysning(
                saksopplysning,
            ))
        }

        LivsoppholdsytelseType.FORELDREPENGER -> {
            val saksopplysning = ForeldrepengerSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as ForeldrepengerDelVilkår).leggTilSaksopplysning(
                saksopplysning,
            ))
        }

        LivsoppholdsytelseType.OMSORGSPENGER -> {
            val saksopplysning = OmsorgspengerSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as OmsorgspengerDelVilkår).leggTilSaksopplysning(
                saksopplysning,
            ))
        }

        LivsoppholdsytelseType.OPPLÆRINGSPENGER -> {
            val saksopplysning = OpplæringspengerSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as OpplæringspengerDelVilkår).leggTilSaksopplysning(
                saksopplysning,
            ))
        }

        LivsoppholdsytelseType.OVERGANGSSTØNAD -> {
            val saksopplysning = OvergangsstønadSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as OvergangsstønadDelVilkår).leggTilSaksopplysning(
                saksopplysning,
            ))
        }

        LivsoppholdsytelseType.PENSJONSINNTEKT -> {
            val saksopplysning = PensjonsinntektSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as PensjonsinntektDelVilkår).leggTilSaksopplysning(
                saksopplysning,
            ))
        }

        LivsoppholdsytelseType.PLEIEPENGER_NÆRSTÅENDE -> {
            val saksopplysning = PleiepengerNærståendeSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as PleiepengerNærståendeDelVilkår).leggTilSaksopplysning(
                saksopplysning,
            ))
        }

        LivsoppholdsytelseType.PLEIEPENGER_SYKTBARN -> {
            val saksopplysning = PleiepengerSyktBarnSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as PleiepengerSyktBarnDelVilkår).leggTilSaksopplysning(
                saksopplysning,
            ))
        }

        LivsoppholdsytelseType.SUPPLERENDESTØNAD_ALDER -> {
            val saksopplysning = SupplerendeStønadAlderSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as SupplerendestønadAlderDelVilkår).leggTilSaksopplysning(
                saksopplysning,
            ))
        }

        LivsoppholdsytelseType.SUPPLERENDESTØNAD_FLYKTNING -> {
            val saksopplysning = SupplerendeStønadFlyktningSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as SupplerendestønadFlyktningDelVilkår).leggTilSaksopplysning(
                saksopplysning,
            ))

        }

        LivsoppholdsytelseType.SVANGERSKAPSPENGER -> {
            val saksopplysning = SvangerskapspengerSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as SvangerskapspengerDelVilkår).leggTilSaksopplysning(
                saksopplysning,
            ))
        }

        LivsoppholdsytelseType.UFØRETRYGD -> {
            val saksopplysning = UføretrygdSaksopplysning.Saksbehandler(
                harYtelse = Periodisering(
                    command.ytelseForPeriode.map { PeriodeMedVerdi(it.tilYtelse(), it.periode) },
                ).utvid(HarYtelse.HAR_IKKE_YTELSE, vurderingsperiode),
                årsakTilEndring = command.årsakTilEndring,
                saksbehandler = command.saksbehandler,
                tidsstempel = LocalDateTime.now(),
            )
            delVilkår + (type to (delVilkår[type]!! as UføretrygdDelVilkår).leggTilSaksopplysning(saksopplysning))
        }
    }
