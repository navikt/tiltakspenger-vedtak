package no.nav.tiltakspenger.meldekort.domene

import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.meldekort.domene.ReduksjonAvYtelsePåGrunnAvFravær.IngenReduksjon
import no.nav.tiltakspenger.meldekort.domene.ReduksjonAvYtelsePåGrunnAvFravær.YtelsenFallerBort
import java.time.LocalDate

/**
 * Når en bruker er på tiltak kan hen være 1-5 av 7 dager i uken på tiltak. Dvs. minst 2 dager per uke må være Sperret eller IkkeDeltatt.
 *
 * Vi vet at det på et tidspunkt kommer til å være mulig å fylle ut en meldekortdag for flere enn ett tiltak. Da vil man kunne rename Meldekortdag til MeldekortdagForTiltak og wrappe den i en Meldekortdag(List<MeldekortdagForTiltak>)
 */
sealed interface Meldekortdag {
    val dato: LocalDate
    val meldekortId: MeldekortId

    val reduksjon: ReduksjonAvYtelsePåGrunnAvFravær?
    val tiltakstype: TiltakstypeSomGirRett

    data class IkkeUtfylt(
        override val meldekortId: MeldekortId,
        override val dato: LocalDate,
        override val tiltakstype: TiltakstypeSomGirRett,
    ) : Meldekortdag {
        override val reduksjon = null
    }

    sealed interface Utfylt : Meldekortdag {
        override val tiltakstype: TiltakstypeSomGirRett
        override val reduksjon: ReduksjonAvYtelsePåGrunnAvFravær

        /** Begrenses av antallDager (1-5) per uke og vurderingsperioden sine utfall. */
        val harDeltattEllerFravær: Boolean

        sealed interface Deltatt : Utfylt {
            override val harDeltattEllerFravær get() = true

            data class DeltattUtenLønnITiltaket(
                override val meldekortId: MeldekortId,
                override val dato: LocalDate,
                override val tiltakstype: TiltakstypeSomGirRett,
            ) : Deltatt {
                override val reduksjon = IngenReduksjon
            }

            data class DeltattMedLønnITiltaket(
                override val meldekortId: MeldekortId,
                override val dato: LocalDate,
                override val tiltakstype: TiltakstypeSomGirRett,
            ) : Deltatt {
                override val reduksjon = YtelsenFallerBort
            }
        }

        data class IkkeDeltatt(
            override val meldekortId: MeldekortId,
            override val dato: LocalDate,
            // TODO post-mvp: Siden vi bare støtter et tiltak i MVP kan vi implisitt fylle ut denne. Dersom vi har flere tiltak, må bruker i så fall velge tiltaket han ikke har deltatt på og det er ikke sikkert det gir mening og vi tar bort feltet.
            override val tiltakstype: TiltakstypeSomGirRett,
        ) : Utfylt {
            override val reduksjon = YtelsenFallerBort
            override val harDeltattEllerFravær = false
        }

        sealed interface Fravær : Utfylt {
            override val harDeltattEllerFravær get() = true

            /**
             * @property reduksjon I tilfellet syk bruker/barn, gjøres det en utregning på tvers av meldekort basert på egenmeldingsdager, sykedager og karantene.
             */
            sealed interface Syk : Fravær {
                override val reduksjon: ReduksjonAvYtelsePåGrunnAvFravær

                data class SykBruker(
                    override val meldekortId: MeldekortId,
                    override val dato: LocalDate,
                    override val tiltakstype: TiltakstypeSomGirRett,
                    override val reduksjon: ReduksjonAvYtelsePåGrunnAvFravær,
                ) : Syk

                data class SyktBarn(
                    override val meldekortId: MeldekortId,
                    override val dato: LocalDate,
                    override val tiltakstype: TiltakstypeSomGirRett,
                    override val reduksjon: ReduksjonAvYtelsePåGrunnAvFravær,
                ) : Syk
            }

            sealed interface Velferd : Fravær {
                data class VelferdGodkjentAvNav(
                    override val meldekortId: MeldekortId,
                    override val dato: LocalDate,
                    override val tiltakstype: TiltakstypeSomGirRett,
                ) : Velferd {
                    override val reduksjon = IngenReduksjon
                }

                data class VelferdIkkeGodkjentAvNav(
                    override val meldekortId: MeldekortId,
                    override val dato: LocalDate,
                    override val tiltakstype: TiltakstypeSomGirRett,
                ) : Velferd {
                    override val reduksjon = YtelsenFallerBort
                }
            }
        }

        /**
         * En meldekortdag bruker ikke får mulighet til å fylle ut.
         * Gjelder for disse tilfellene:
         * 1. Første del av første meldekort i en sak.
         * 1. Siste del av siste meldekort i en sak.
         * 1. Andre dager bruker ikke får melde pga. vilkårsvurderingen. Delvis innvilget. Dette vil ikke gjelde MVP.
         */
        data class Sperret(
            override val meldekortId: MeldekortId,
            override val dato: LocalDate,
            override val tiltakstype: TiltakstypeSomGirRett,
        ) : Utfylt {
            override val reduksjon = YtelsenFallerBort
            override val harDeltattEllerFravær = false
        }
    }
}
