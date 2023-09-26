package no.nav.tiltakspenger.domene.saksopplysning

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.YtelseSak
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import java.time.LocalDate

enum class TypeSaksopplysning {
    IKKE_INNHENTET_ENDA,
    HAR_YTELSE,
    HAR_IKKE_YTELSE,
}

data class SaksopplysningDTO(
    val fom: LocalDate,
    val tom: LocalDate,
    val vilkårstittel: String,
    val begrunnelse: String,
    val harYtelse: Boolean,
)

sealed class Saksopplysning {
    abstract val fom: LocalDate
    abstract val tom: LocalDate
    abstract val vilkår: Vilkår
    abstract val kilde: Kilde
    abstract val detaljer: String
    abstract val typeSaksopplysning: TypeSaksopplysning

    data class Dagpenger(
        override val fom: LocalDate,
        override val tom: LocalDate,
        override val vilkår: Vilkår,
        override val kilde: Kilde, // "Arena" / "Saksbehandler"
        override val detaljer: String,
        override val typeSaksopplysning: TypeSaksopplysning,
    ) : Saksopplysning() {
        companion object {
            fun initSaksopplysning(periode: Periode): Dagpenger {
                return Dagpenger(
                    fom = periode.fra,
                    tom = periode.til,
                    vilkår = Vilkår.DAGPENGER,
                    kilde = Kilde.ARENA,
                    detaljer = "",
                    typeSaksopplysning = TypeSaksopplysning.IKKE_INNHENTET_ENDA,
                )
            }

            fun lagFakta(ytelser: List<YtelseSak>?, periode: Periode) =
                DagpengerTolker.tolkeData(ytelser, periode)
        }
    }

    data class Aap(
        override val fom: LocalDate,
        override val tom: LocalDate,
        override val vilkår: Vilkår,
        override val kilde: Kilde,
        override val detaljer: String,
        override val typeSaksopplysning: TypeSaksopplysning,
    ) : Saksopplysning() {
        companion object {
            fun initSaksopplysning(periode: Periode): Aap {
                return Aap(
                    fom = periode.fra,
                    tom = periode.til,
                    vilkår = Vilkår.AAP,
                    kilde = Kilde.ARENA,
                    detaljer = "",
                    typeSaksopplysning = TypeSaksopplysning.IKKE_INNHENTET_ENDA,
                )
            }
            fun lagSaksopplysningFraSBH(fom: LocalDate, tom: LocalDate, detaljer: String, typeSaksopplysning: TypeSaksopplysning): Aap {
                return Aap(
                    fom = fom,
                    tom = tom,
                    vilkår = Vilkår.AAP,
                    kilde = Kilde.SAKSB,
                    detaljer = detaljer,
                    typeSaksopplysning = typeSaksopplysning,
                )
            }

            fun lagSaksopplysninger(ytelser: List<YtelseSak>?, periode: Periode) =
                AapTolker.tolkeData(ytelser, periode)
        }
    }
}

private fun prioriterOverlappendeSaksopplysning() {
}

private fun lagVurderingerForOppfyltePerioder() {
}

// Eksempel på bruker som ikke har andre ytelser
// Saksopplysninger
// |     fom     |    tom     | Vilkår| Kilde   | opphørTidligereSaksopplysning  |

// Periode som skal vurderes : 2023-01-01 - 2023-03-31

// Vurderinger
// | Vilkår | kilde  |    fom     |    tom     |
// |   AAP  | Arena  | 2023-01-01 | 2023-03-31 |  Oppfylt

// ----

// Eksempel på bruker går på AAP deler av perioden
// Saksopplysninger
// |     fom     |    tom     | Vilkår| Kilde   | opphørTidligereSaksopplysning  |
// |  2023-01-01 | 2023-01-31 |  AAP  | Arena   |        false                   |

// Periode som skal vurderes : 2023-01-01 - 2023-03-31

// Vurderinger
// | Vilkår | kilde  |    fom     |    tom     |
// |   AAP  | Arena  | 2023-01-01 | 2023-01-31 |  IkkeOppfylt
// |   AAP  | Arena  | 2023-02-01 | 2023-03-31 |  Oppfylt

// ---

// Eksempel med saksbehandler overstyrer saksopplysninger og sier at bruker ikke går på AAP
// Saksopplysninger
// |     fom     |    tom     | Vilkår| Kilde           | opphørTidligereSaksopplysning  |
// |  2023-01-01 | 2023-01-31 |  AAP  | Arena           |         false                  |
// |  2023-01-01 | 2023-01-31 |  AAP  | Saksbehandler   |         true                   |

// Periode som skal vurderes : 2023-01-01 - 2023-03-31

// Vurderinger
// | Vilkår | kilde  |    fom     |    tom     |
// |   AAP  | Arena  | 2023-01-01 | 2023-01-31 |   Oppfylt
// |   AAP  | Arena  | 2023-02-01 | 2023-03-31 |   Oppfylt
// Eller :
// | Vilkår | kilde  |    fom     |    tom     |
// |   AAP  | Arena  | 2023-01-01 | 2023-03-31 |   Oppfylt

// ---

// Eksempel med saksbehandler overstyrer saksopplysninger og sier at bruker ikke går på AAP i deler av perioden
// Saksopplysninger
// |     fom     |    tom     | Vilkår| Kilde           | opphørTidligereSaksopplysning  |
// |  2023-01-01 | 2023-01-31 |  AAP  | Arena           |         false                  |
// |  2023-01-01 | 2023-01-15 |  AAP  | Saksbehandler   |         true                   |

// Periode som skal vurderes : 2023-01-01 - 2023-03-31

// Vurderinger
// | Vilkår | kilde  |    fom     |    tom     |
// |   AAP  | Arena  | 2023-01-01 | 2023-01-15 |   Oppfylt
// |   AAP  | Arena  | 2023-01-16 | 2023-01-31 |   IkkeOppfylt
// |   AAP  | Arena  | 2023-02-01 | 2023-03-31 |   Oppfylt

// ---

// Eksempel med saksbehandler overstyrer saksopplysninger og sier at bruker går på AAP
// Saksopplysninger
// |     fom     |    tom     | Vilkår| Kilde           | opphørTidligereSaksopplysning  |
// |  2023-01-01 | 2023-01-31 |  AAP  | Saksbehandler   |         false                  |

// Periode som skal vurderes : 2023-01-01 - 2023-03-31

// Vurderinger
// | Vilkår | kilde  |    fom     |    tom     |
// |   AAP  | Arena  | 2023-01-01 | 2023-01-31 |   IkkeOppfylt
// |   AAP  | Arena  | 2023-02-01 | 2023-03-31 |   Oppfylt

fun List<Saksopplysning>.lagVurdering(vilkår: Vilkår): List<Vurdering> =
// TODO Her må vi kanskje lage Vurderinger for Oppfylte perioder for at vi skal kunne lage DelvisInnvilget?

// Lag liste med Vurdering av alle Saksopplysninger som har opphør... false (egentlig alle som er kilde != SAKSB + kilde == SAKSB && Opphør == true )

// Trenger vi å slå sammen overlappende perioder her ?

// Lag liste med Vurderinger av alle Saksopplysniger som har opphør.. true
// Fjern disse periodene fra den første listen
    // Slå sammen periodene

    this.map { fakta ->
        Vurdering.IkkeOppfylt(
            vilkår = fakta.vilkår,
            kilde = fakta.kilde,
            fom = fakta.fom,
            tom = fakta.tom,
            detaljer = fakta.detaljer,
        )
    }.ifEmpty {
        listOf(
            Vurdering.Oppfylt(
                vilkår = vilkår,
                kilde = settKilde(vilkår),
                detaljer = "",
            ),
        )
    }

private fun settKilde(vilkår: Vilkår): Kilde {
    return when (vilkår) {
        Vilkår.AAP -> Kilde.ARENA
        Vilkår.ALDER -> TODO()
        Vilkår.ALDERSPENSJON -> TODO()
        Vilkår.DAGPENGER -> Kilde.ARENA
        Vilkår.FORELDREPENGER -> TODO()
        Vilkår.GJENLEVENDEPENSJON -> TODO()
        Vilkår.INSTITUSJONSOPPHOLD -> TODO()
        Vilkår.INTROPROGRAMMET -> TODO()
        Vilkår.KOMMUNALEYTELSER -> TODO()
        Vilkår.KVP -> TODO()
        Vilkår.LØNNSINNTEKT -> TODO()
        Vilkår.OMSORGSPENGER -> TODO()
        Vilkår.OPPLÆRINGSPENGER -> TODO()
        Vilkår.OVERGANGSSTØNAD -> TODO()
        Vilkår.PENSJONSINNTEKT -> TODO()
        Vilkår.PLEIEPENGER_NÆRSTÅENDE -> TODO()
        Vilkår.PLEIEPENGER_SYKT_BARN -> TODO()
        Vilkår.STATLIGEYTELSER -> TODO()
        Vilkår.SUPPLERENDESTØNADALDER -> TODO()
        Vilkår.SUPPLERENDESTØNADFLYKTNING -> TODO()
        Vilkår.SVANGERSKAPSPENGER -> TODO()
        Vilkår.SYKEPENGER -> TODO()
        Vilkår.TILTAKSPENGER -> TODO()
        Vilkår.UFØRETRYGD -> TODO()
    }
}

enum class Kilde(val navn: String) {
    ARENA("Arena"),
    PDL("Pdl"),
    EF("EF"),
    FPSAK("FPSAK"),
    K9SAK("K9SAK"),
    PESYS("pesys"),
    SØKNAD("Søknad"),
    SAKSB("Saksbehandler"),
}
