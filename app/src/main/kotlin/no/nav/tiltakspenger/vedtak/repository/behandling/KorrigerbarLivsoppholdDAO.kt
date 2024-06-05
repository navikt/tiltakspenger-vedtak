package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.libs.periodisering.Periodisering
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.HarYtelse
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.KorrigerbarLivsopphold
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Utfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vurdering
import java.time.LocalDate

internal class KorrigerbarLivsoppholdDAO(
    private val livsoppholdVurderingDAO: LivsoppholdVurderingDAO = LivsoppholdVurderingDAO(),
    private val livsoppholdSaksopplysningDAO: LivsoppholdSaksopplysningDAO = LivsoppholdSaksopplysningDAO(),
) {

    fun hent(behandlingId: BehandlingId, txSession: TransactionalSession): Map<Vilkår, KorrigerbarLivsopphold> {
        val denormaliserteVurderinger: List<DenormalisertLivsoppholdVurdering> =
            livsoppholdVurderingDAO.hent(behandlingId, txSession)
        val denormaliserteSaksopplysninger: List<DenormalisertLivsoppholdSaksopplysning> =
            livsoppholdSaksopplysningDAO.hent(behandlingId, txSession)

        val vurderinger: Map<Vilkår, Vurdering> = normaliserVurdering(denormaliserteVurderinger)
        val opprinneligeSaksopplysninger: Map<Vilkår, LivsoppholdSaksopplysning> =
            normaliserSaksopplysning(
                denormaliserteSaksopplysninger,
                DenormalisertLivsoppholdSaksopplysning.DenormalisertSaksopplysningType.OPPRINNELIG,
            )
        val korrigerteSaksopplysninger: Map<Vilkår, LivsoppholdSaksopplysning> =
            normaliserSaksopplysning(
                denormaliserteSaksopplysninger,
                DenormalisertLivsoppholdSaksopplysning.DenormalisertSaksopplysningType.KORRIGERT,
            )
        val avklarteSaksopplysninger: Map<Vilkår, LivsoppholdSaksopplysning> =
            normaliserSaksopplysning(
                denormaliserteSaksopplysninger,
                DenormalisertLivsoppholdSaksopplysning.DenormalisertSaksopplysningType.AVKLART,
            )

        return vurderinger.values.map { vurdering ->
            KorrigerbarLivsopphold.fromDb(
                vurderingsperiode = vurdering.utfall.totalePeriode,
                vilkår = vurdering.vilkår,
                opprinneligLivsoppholdSaksopplysning = opprinneligeSaksopplysninger.get(vurdering.vilkår)!!,
                korrigertLivsoppholdSaksopplysning = korrigerteSaksopplysninger.get(vurdering.vilkår)!!,
                avklartLivsoppholdSaksopplysning = avklarteSaksopplysninger.get(vurdering.vilkår)!!,
                vurdering = vurdering,
            )
        }.associateBy { it.vilkår }
    }

    fun slett(behandlingId: BehandlingId, txSession: TransactionalSession) {
        livsoppholdVurderingDAO.slett(behandlingId, txSession)
        livsoppholdSaksopplysningDAO.slett(behandlingId, txSession)
    }

    fun lagre(
        behandlingId: BehandlingId,
        livsopphold: Collection<KorrigerbarLivsopphold>,
        txSession: TransactionalSession,
    ) {
        livsopphold.forEach {
            lagre(behandlingId, it, txSession)
        }
    }

    private fun lagre(
        behandlingId: BehandlingId,
        livsopphold: KorrigerbarLivsopphold,
        txSession: TransactionalSession,
    ) {
        denormaliserVurderinger(Pair(livsopphold.vilkår, livsopphold)).forEach {
            livsoppholdVurderingDAO.lagre(behandlingId, it, txSession)
        }
        denormaliserSaksopplysninger(Pair(livsopphold.vilkår, livsopphold)).forEach {
            livsoppholdSaksopplysningDAO.lagre(behandlingId, it, txSession)
        }
    }

    private fun denormaliserVurderinger(livsopphold: Pair<Vilkår, KorrigerbarLivsopphold>): List<DenormalisertLivsoppholdVurdering> {
        return livsopphold.second.vurdering.utfall.perioder().map {
            DenormalisertLivsoppholdVurdering(
                vilkår = livsopphold.first,
                detaljer = livsopphold.second.vurdering.detaljer,
                periodeFra = it.periode.fra,
                periodeTil = it.periode.til,
                periodeVerdi = it.verdi,
            )
        }
    }

    private fun normaliserVurdering(vurderinger: List<DenormalisertLivsoppholdVurdering>): Map<Vilkår, Vurdering> {
        val noe: List<Vurdering> =
            vurderinger.groupBy { it.vilkår }.values.map { values: List<DenormalisertLivsoppholdVurdering> ->
                val perioderMedVerdi: List<PeriodeMedVerdi<Utfall>> = values.map { denormalisertVurdering ->
                    PeriodeMedVerdi(
                        denormalisertVurdering.periodeVerdi,
                        Periode(denormalisertVurdering.periodeFra, denormalisertVurdering.periodeTil),
                    )
                }
                val firstValue: DenormalisertLivsoppholdVurdering = values.first()
                val periodisering = Periodisering.fraPeriodeListe(
                    perioderMedVerdi = perioderMedVerdi,
                )
                Vurdering(
                    vilkår = firstValue.vilkår,
                    utfall = periodisering,
                    detaljer = firstValue.detaljer,
                )
            }
        return noe.associateBy { it.vilkår }
    }

    private fun normaliserSaksopplysning(
        saksopplysninger: List<DenormalisertLivsoppholdSaksopplysning>,
        type: DenormalisertLivsoppholdSaksopplysning.DenormalisertSaksopplysningType,
    ): Map<Vilkår, LivsoppholdSaksopplysning> {
        val noe: List<LivsoppholdSaksopplysning> =
            saksopplysninger.groupBy { it.vilkår }.values.map { values: List<DenormalisertLivsoppholdSaksopplysning> ->

                val perioderMedVerdi: List<PeriodeMedVerdi<HarYtelse>> = values.filter { it.type == type }.map { noe ->
                    PeriodeMedVerdi(noe.periodeVerdi, Periode(noe.periodeFra, noe.periodeTil))
                }
                val firstValue: DenormalisertLivsoppholdSaksopplysning = values.first()
                val periodisering = Periodisering.fraPeriodeListe(
                    perioderMedVerdi = perioderMedVerdi,
                )
                LivsoppholdSaksopplysning(
                    kilde = firstValue.kilde,
                    vilkår = firstValue.vilkår,
                    harYtelse = periodisering,
                    detaljer = firstValue.detaljer,
                    saksbehandler = firstValue.saksbehandler,
                )
            }
        return noe.associateBy { it.vilkår }
    }

    private fun denormaliserSaksopplysninger(livsopphold: Pair<Vilkår, KorrigerbarLivsopphold>): List<DenormalisertLivsoppholdSaksopplysning> {
        return livsopphold.second.opprinneligLivsoppholdSaksopplysning.harYtelse.perioder().map {
            DenormalisertLivsoppholdSaksopplysning(
                vilkår = livsopphold.first,
                type = DenormalisertLivsoppholdSaksopplysning.DenormalisertSaksopplysningType.OPPRINNELIG,
                kilde = livsopphold.second.opprinneligLivsoppholdSaksopplysning.kilde,
                detaljer = livsopphold.second.opprinneligLivsoppholdSaksopplysning.detaljer,
                saksbehandler = livsopphold.second.opprinneligLivsoppholdSaksopplysning.saksbehandler,
                periodeFra = it.periode.fra,
                periodeTil = it.periode.til,
                periodeVerdi = it.verdi,
            )
        } + (
            livsopphold.second.korrigertLivsoppholdSaksopplysning?.harYtelse?.perioder()?.map {
                DenormalisertLivsoppholdSaksopplysning(
                    vilkår = livsopphold.first,
                    type = DenormalisertLivsoppholdSaksopplysning.DenormalisertSaksopplysningType.KORRIGERT,
                    kilde = livsopphold.second.korrigertLivsoppholdSaksopplysning!!.kilde,
                    detaljer = livsopphold.second.korrigertLivsoppholdSaksopplysning!!.detaljer,
                    saksbehandler = livsopphold.second.korrigertLivsoppholdSaksopplysning!!.saksbehandler,
                    periodeFra = it.periode.fra,
                    periodeTil = it.periode.til,
                    periodeVerdi = it.verdi,
                )
            } ?: emptyList()
            ) + livsopphold.second.avklartLivsoppholdSaksopplysning.harYtelse.perioder().map {
            DenormalisertLivsoppholdSaksopplysning(
                vilkår = livsopphold.first,
                type = DenormalisertLivsoppholdSaksopplysning.DenormalisertSaksopplysningType.AVKLART,
                kilde = livsopphold.second.avklartLivsoppholdSaksopplysning.kilde,
                detaljer = livsopphold.second.avklartLivsoppholdSaksopplysning.detaljer,
                saksbehandler = livsopphold.second.avklartLivsoppholdSaksopplysning.saksbehandler,
                periodeFra = it.periode.fra,
                periodeTil = it.periode.til,
                periodeVerdi = it.verdi,
            )
        }
    }
}

data class DenormalisertLivsoppholdSaksopplysning(
    val vilkår: Vilkår,
    val type: DenormalisertSaksopplysningType,
    val kilde: Kilde,
    val detaljer: String,
    val saksbehandler: String?,
    val periodeFra: LocalDate,
    val periodeTil: LocalDate,
    val periodeVerdi: HarYtelse,
) {
    enum class DenormalisertSaksopplysningType {
        OPPRINNELIG, KORRIGERT, AVKLART
    }
}

data class DenormalisertLivsoppholdVurdering(
    val vilkår: Vilkår,
    val detaljer: String,
    val periodeFra: LocalDate,
    val periodeTil: LocalDate,
    val periodeVerdi: Utfall,
)
