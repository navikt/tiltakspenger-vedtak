package no.nav.tiltakspenger.vilkårsvurdering.vurdering

// data class OppdeltTiltakPeriode(
//    val tiltak: List<Tiltak>,
//    val totaltAntallDager: Float,
//    val periode: Periode,
// ) {
//
//    fun overlappendePerioder(andrePerioder: List<Periode>): List<OppdeltTiltakPeriode> =
//        this.periode.overlappenderPerioder(andrePerioder)
//            .map {
//                OppdeltTiltakPeriode(
//                    tiltak = this.tiltak,
//                    totaltAntallDager = this.totaltAntallDager,
//                    periode = it,
//                )
//            }
//
//    fun ikkeOverlappendePeriode(annenPeriode: OppdeltTiltakPeriode): List<OppdeltTiltakPeriode> =
//        periode.ikkeOverlappendePeriode(annenPeriode.periode)
//            .map {
//                if (periode.inneholderHele(it)) {
//                    OppdeltTiltakPeriode(
//                        tiltak = this.tiltak,
//                        totaltAntallDager = this.totaltAntallDager,
//                        periode = it,
//                    )
//                } else {
//                    OppdeltTiltakPeriode(
//                        tiltak = annenPeriode.tiltak,
//                        totaltAntallDager = annenPeriode.totaltAntallDager,
//                        periode = it,
//                    )
//                }
//            }
//
//    fun overlappendePeriode(annenPeriode: OppdeltTiltakPeriode): OppdeltTiltakPeriode? =
//        periode.overlappendePeriode(annenPeriode.periode)?.let {
//            OppdeltTiltakPeriode(
//                tiltak = this.tiltak + annenPeriode.tiltak,
//                totaltAntallDager = this.totaltAntallDager + annenPeriode.totaltAntallDager,
//                periode = it,
//            )
//        }
//
//    fun overlapperMed(annenPeriode: OppdeltTiltakPeriode): Boolean =
//        this.periode.overlapperMed(annenPeriode.periode)
// }

// class TiltakdeltakelseVilkårsvurdering(
//    private val tiltak: List<Tiltak>,
//    private val vurderingsperiode: Periode,
// ) : Vilkårsvurdering() {
//
//    private val tiltakMedRettPåTiltakspenger =
//        tiltak.filter { it.tiltak.rettPåTiltakspenger }
//
//    override fun vilkår(): Vilkår = Vilkår.SYKEPENGER
//
//    override var manuellVurdering: Vurdering? = null // TODO: Man kan ikke bare ha én manuell vurdering her!
//
//    override fun vurderinger(): List<Vurdering> =
//        (denIkkeManuelleVurderingen() + manuellVurdering).filterNotNull()
//
//    private fun denIkkeManuelleVurderingen(): List<Vurdering> {
//        // TODO: Denne koden er ikke avansert nok
//        // Det er noen tiltak som ikke gir rett på tiltakspenger
//        // hvis søker også går på et annet, spesifikt tiltak samtidig.
//        // (Søker får da lønn gjennom det andre tiltaket)
//
//        val alleVurderinger = mutableListOf<Vurdering>()
//
//        val tiltakSomMåVurderesManuelt: List<Vurdering.KreverManuellVurdering> = tiltakMedRettPåTiltakspenger
//            .filter {
//                it.deltakelsePeriode.fom == null ||
//                    it.deltakelsePeriode.tom == null ||
//                    it.antallDagerPerUke == null
//            }
//            .map {
//                Vurdering.KreverManuellVurdering(
//                    vilkår = vilkår(),
//                    kilde = Kilde.ARENA,
//                    fom = it.deltakelsePeriode.fom ?: vurderingsperiode.fra,
//                    tom = it.deltakelsePeriode.tom ?: vurderingsperiode.til,
//                    detaljer = "${it.tiltak.navn} hos ${it.arrangør} er ufullstendig",
//                )
//            }
//        alleVurderinger.addAll(tiltakSomMåVurderesManuelt)
//        val gjenståendeVurderingsperiode =
//            vurderingsperiode.trekkFra(tiltakSomMåVurderesManuelt.map { Periode(it.fom, it.tom) })
//
//        val ikkeOverlappendeOppdelteTiltakPerioder = tiltakMedRettPåTiltakspenger
//            .filter {
//                it.deltakelsePeriode.fom != null &&
//                    it.deltakelsePeriode.tom != null &&
//                    it.antallDagerPerUke != null
//            }
//            .map {
//                OppdeltTiltakPeriode(
//                    tiltak = listOf(it),
//                    totaltAntallDager = it.antallDagerPerUke!!,
//                    periode = Periode(it.deltakelsePeriode.fom!!, it.deltakelsePeriode.tom!!),
//                )
//            }
//            .fold(
//                initial = listOf(),
//                operation = ::kombinerOppdelteTiltakPerioder,
//            )
//            .flatMap {
//                it.overlappendePerioder(gjenståendeVurderingsperiode)
//            }
//        // TODO: Vurder.Oppfylt tar ikke datoer,
//        // fordi vi har basert oss på at et oppfylt vilkår gjelder hele vurderingsperioden
//        // For tiltaksdeltakelse så gir ikke det mening, da det er nyanser som er viktige
//        // Vi må vite hvor mange dager i uken man får godkjent!
//        // Så her ønsker vi egentlig å gjøre noe ala:
//        /*
//        val vurderingerForPerioderMedTiltak = ikkeOverlappendeOppdelteTiltakPerioder.map {
//            Vurdering.Oppfylt(
//                vilkår = vilkår(),
//                kilde = "Arena",
//                fom = it.periode.fra,
//                tom = it.periode.til,
//                detaljer = "Perioden dekkes for ${it.totaltAntallDager} pga ${oppsummer(it.tiltak)}"
//            )
//        }
//        alleVurderinger.addAll(vurderingerForPerioderMedTiltak)
//         */
//
//        val vurderingerForPerioderUtenTiltak =
//            gjenståendeVurderingsperiode
//                .trekkFra(ikkeOverlappendeOppdelteTiltakPerioder.map { it.periode })
//                .map {
//                    Vurdering.IkkeOppfylt(
//                        vilkår = vilkår(),
//                        kilde = Kilde.ARENA,
//                        fom = it.fra,
//                        tom = it.til,
//                        detaljer = "Søker har ikke tiltak i denne periode",
//                    )
//                }
//        alleVurderinger.addAll(vurderingerForPerioderUtenTiltak)
//
//        return alleVurderinger
//    }
//
//    private fun kombinerOppdelteTiltakPerioder(
//        ikkeOverlappendeOppdelteTiltakPerioder: List<OppdeltTiltakPeriode>,
//        nyPeriode: OppdeltTiltakPeriode,
//    ): List<OppdeltTiltakPeriode> =
//        ikkeOverlappendeOppdelteTiltakPerioder
//            .flatMap { it.ikkeOverlappendePeriode(nyPeriode) + it.overlappendePeriode(nyPeriode) }
//            .filterNotNull()
//
//    override fun detIkkeManuelleUtfallet(): Utfall {
//        val utfall = denIkkeManuelleVurderingen().map { it.utfall }
//        return when {
//            utfall.any { it == Utfall.IKKE_OPPFYLT } -> Utfall.IKKE_OPPFYLT
//            utfall.any { it == Utfall.KREVER_MANUELL_VURDERING } -> Utfall.KREVER_MANUELL_VURDERING
//            else -> Utfall.OPPFYLT
//        }
//    }
// }
