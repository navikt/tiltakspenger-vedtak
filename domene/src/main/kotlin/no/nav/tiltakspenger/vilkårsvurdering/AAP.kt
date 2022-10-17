package no.nav.tiltakspenger.vilkårsvurdering

//sealed class StatligeYtelser(
//    private val ytelser: List<YtelseSak>,
//    private val vurderingsperiode: Periode,
//) : Vilkårsvurderinger() {
//
//    override var manuellVurdering: Vurdering?
//        get() = TODO("Not yet implemented")
//        set(value) {}
//
//    override fun vurderinger(): List<Vurdering> {
//        TODO("Not yet implemented")
//    }
//
//    override fun samletUtfallYtelser(): Utfall {
//        TODO("Not yet implemented")
//    }
//
//}
//data class AAP(
//    private val ytelser: List<YtelseSak>,
//    private val vurderingsperiode: Periode,
//) : StatligeYtelser() {
//    override val lovReferanse: Lovreferanse = Lovreferanse.AAP
//    override var manuellVurdering: Vurdering? = null
//    private val ytelseVurderinger = lagYtelseVurderinger()
//
//    override fun vurderinger(): List<Vurdering> = (ytelseVurderinger + manuellVurdering).filterNotNull()
//
//    override fun samletUtfallYtelser(): Utfall {
//        val utfall = ytelseVurderinger.map { it.utfall }
//        return when {
//            utfall.any { it == Utfall.IKKE_OPPFYLT } -> Utfall.IKKE_OPPFYLT
//            utfall.any { it == Utfall.KREVER_MANUELL_VURDERING } -> Utfall.KREVER_MANUELL_VURDERING
//            else -> Utfall.OPPFYLT
//        }
//    }
//
//    private fun lagYtelseVurderinger(): List<Vurdering> =
//        ytelser
//            .filter {
//                Periode(
//                    it.fomGyldighetsperiode.toLocalDate(),
//                    (it.tomGyldighetsperiode?.toLocalDate() ?: LocalDate.MAX)
//                ).overlapperMed(vurderingsperiode)
//            }
//            .filter { it.status == YtelseSak.YtelseSakStatus.AKTIV }
//            .filter { it.ytelsestype == YtelseSak.YtelseSakYtelsetype.AA }
//            .map {
//                Vurdering(
//                    kilde = "Arena",
//                    fom = it.fomGyldighetsperiode.toLocalDate(),
//                    tom = it.tomGyldighetsperiode?.toLocalDate(),
//                    utfall = Utfall.IKKE_OPPFYLT,
//                    detaljer = "",
//                )
//            }.ifEmpty {
//                listOf(
//                    Vurdering(
//                        kilde = "Arena",
//                        fom = null,
//                        tom = null,
//                        utfall = Utfall.OPPFYLT,
//                        detaljer = "",
//                    )
//                )
//            }
//}
