package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.SamletUtfall
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.Vilkårssett
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.AlderSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.alder.AlderVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.InstitusjonsoppholdVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.institusjonsopphold.institusjonsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.IntroVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.introduksjonsprogrammet.introSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist.KravfristVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kravfrist.kravfristSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.KVPVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.kvp.kvpSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.LivsoppholdVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.livsopphold.livsoppholdSaksopplysning
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.TiltakDeltagelseVilkår
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.tiltakSaksopplysning
import java.time.LocalDate

data class Førstegangsbehandling(
    override val id: BehandlingId,
    override val sakId: SakId,
    override val saksnummer: Saksnummer,
    override val fnr: Fnr,
    override val vurderingsperiode: Periode,
    override val søknader: List<Søknad>,
    override val saksbehandler: String?,
    override val beslutter: String?,
    override val vilkårssett: Vilkårssett,
    override val status: BehandlingStatus,
    override val tilstand: BehandlingTilstand,
) : Behandling {
    init {
        // TODO jah: Brekker for mange tester. Bør legges inn når vi er ferdig med vilkår 2.0
        // require(vilkårssett.totalePeriode == vurderingsperiode) { "Vilkårssettets periode (${vilkårssett.totalePeriode} må være lik vurderingsperioden $vurderingsperiode" }
    }

    val samletUtfall = vilkårssett.samletUtfall

    companion object {

        fun opprettBehandling(
            sakId: SakId,
            saksnummer: Saksnummer,
            fnr: Fnr,
            søknad: Søknad,
            fødselsdato: LocalDate,
            saksbehandler: Saksbehandler,
            registrerteTiltak: List<Tiltak>,
        ): Førstegangsbehandling {
            val vurderingsperiode = søknad.vurderingsperiode()
            val tiltak = registrerteTiltak.find {
                it.eksternId == søknad.tiltak.id &&
                    Periode(it.deltakelseFom, it.deltakelseTom).overlapperMed(vurderingsperiode)
            }

            require(tiltak != null) { "Ingen tiltak samsvarer med det tiltaket bruker søkte på i søknaden" }

            return Førstegangsbehandling(
                id = BehandlingId.random(),
                saksnummer = saksnummer,
                sakId = sakId,
                fnr = fnr,
                søknader = listOf(søknad),
                vurderingsperiode = vurderingsperiode,
                vilkårssett = Vilkårssett(
                    vilkårsvurderinger = emptyList(),
                    institusjonsoppholdVilkår = InstitusjonsoppholdVilkår.opprett(
                        søknad.institusjonsoppholdSaksopplysning(
                            vurderingsperiode,
                        ),
                    ),
                    kvpVilkår = KVPVilkår.opprett(søknad.kvpSaksopplysning(vurderingsperiode)),
                    introVilkår = IntroVilkår.opprett(søknad.introSaksopplysning(vurderingsperiode)),
                    livsoppholdVilkår = LivsoppholdVilkår.opprett(
                        søknad.livsoppholdSaksopplysning(vurderingsperiode),
                        vurderingsperiode,
                    ),
                    alderVilkår = AlderVilkår.opprett(
                        AlderSaksopplysning.Personopplysning.opprett(fødselsdato = fødselsdato),
                        vurderingsperiode,
                    ),
                    kravfristVilkår = KravfristVilkår.opprett(søknad.kravfristSaksopplysning(), vurderingsperiode),
                    tiltakDeltagelseVilkår = TiltakDeltagelseVilkår.opprett(tiltak.tiltakSaksopplysning(), vurderingsperiode),
                ),
                saksbehandler = saksbehandler.navIdent,
                beslutter = null,
                status = BehandlingStatus.Manuell,
                tilstand = BehandlingTilstand.UNDER_BEHANDLING,
            )
        }
    }

    override fun søknad(): Søknad = sisteSøknadMedOpprettetFraFørste()

    private fun sisteSøknadMedOpprettetFraFørste(): Søknad =
        søknader.maxBy { it.opprettet }.copy(opprettet = søknader.minBy { it.opprettet }.opprettet)

    override fun leggTilSøknad(søknad: Søknad): Førstegangsbehandling {
        require(
            this.tilstand in listOf(
                BehandlingTilstand.OPPRETTET,
                BehandlingTilstand.UNDER_BEHANDLING,
                BehandlingTilstand.TIL_BESLUTTER,
            ),
        ) { "Kan ikke oppdatere tiltak, feil tilstand $tilstand" }

        // Avgjørelse jah: Vi skal ikke oppdatere vilkårsettet her mens vi skriver om til vilkår 2.0.
        // TODO jah: Fjern mulighet for samtidige søknader.
        //  Dersom avklaringen er basert på saksopplysning fra søknaden, bør vi nullstille avklaringen i påvente av en saksbehandler-opplysning.
        //  Dersom vi allerede har en saksbehander-opplysning, bør vi kreve at saksbehandler tar stilling til alle vilkårene på nytt.
        return this.copy(
            søknader = this.søknader + søknad,
            vurderingsperiode = søknad.vurderingsperiode(),
            vilkårssett = vilkårssett,
            tilstand = BehandlingTilstand.OPPRETTET,
        )
    }

    override fun taBehandling(saksbehandler: Saksbehandler): Behandling {
        return if (this.tilstand == BehandlingTilstand.TIL_BESLUTTER) {
            this.beslutterTarBehandling(saksbehandler)
        } else {
            this.saksbehandlerTarBehandling(saksbehandler)
        }
    }

    private fun saksbehandlerTarBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        require(
            this.tilstand in listOf(BehandlingTilstand.OPPRETTET, BehandlingTilstand.UNDER_BEHANDLING),
        ) { "Vanlig saksbehandler kan ikke ta behandlingen, feil tilstand $tilstand" }
        check(saksbehandler.isSaksbehandler()) { "Saksbehandler må være saksbehandler" }
        return this.copy(saksbehandler = saksbehandler.navIdent, tilstand = BehandlingTilstand.UNDER_BEHANDLING)
    }

    private fun beslutterTarBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        require(
            this.tilstand in listOf(BehandlingTilstand.TIL_BESLUTTER),
        ) { "Kan ikke godkjenne behandling, feil tilstand $tilstand" }

        check(saksbehandler.isBeslutter()) { "Saksbehandler må være beslutter" }
        return this.copy(beslutter = saksbehandler.navIdent)
    }

    override fun avbrytBehandling(saksbehandler: Saksbehandler): Førstegangsbehandling {
        require(
            this.tilstand in listOf(BehandlingTilstand.OPPRETTET, BehandlingTilstand.UNDER_BEHANDLING),
        ) { "Kan ikke avbryte behandling, feil tilstand $tilstand" }

        check(saksbehandler.isSaksbehandler() || saksbehandler.isAdmin()) { "Kan ikke avbryte en behandling som ikke er din" }
        return this.copy(saksbehandler = null)
    }

    override fun tilBeslutning(saksbehandler: Saksbehandler): Førstegangsbehandling {
        require(this.tilstand == BehandlingTilstand.UNDER_BEHANDLING) { "Kan ikke sende behandling til beslutning, feil tilstand $tilstand" }

        checkNotNull(this.saksbehandler) { "Ikke lov å sende Behandling til Beslutter uten saksbehandler" }
        check(saksbehandler.navIdent == this.saksbehandler) { "Det er ikke lov å sende en annen sin behandling til beslutter" }
        check(samletUtfall != SamletUtfall.UAVKLART) { "Kan ikke sende denne behandlingen til beslutter" }
        return this.copy(tilstand = BehandlingTilstand.TIL_BESLUTTER)
    }

    override fun iverksett(utøvendeBeslutter: Saksbehandler): Førstegangsbehandling {
        require(this.tilstand == BehandlingTilstand.TIL_BESLUTTER) { "Kan ikke iverksette behandling, feil tilstand $tilstand" }
        // checkNotNull(saksbehandler) { "Kan ikke iverksette en behandling uten saksbehandler" }
        checkNotNull(beslutter) { "Ikke lov å iverksette uten beslutter" }
        check(utøvendeBeslutter.roller.contains(Rolle.BESLUTTER)) { "Saksbehandler må være beslutter" }
        check(this.beslutter == utøvendeBeslutter.navIdent) { "Kan ikke iverksette en behandling man ikke er beslutter på" }

        return when (status) {
            BehandlingStatus.Manuell -> throw IllegalStateException("En behandling til beslutter kan ikke være manuell")
            BehandlingStatus.Avslag -> throw IllegalStateException("Iverksett av Avslag fungerer, men skal ikke tillates i mvp 1 ${this.id}")
            else -> this.copy(tilstand = BehandlingTilstand.IVERKSATT)
        }
    }

    override fun sendTilbake(utøvendeBeslutter: Saksbehandler): Førstegangsbehandling {
        require(this.tilstand == BehandlingTilstand.TIL_BESLUTTER) { "Kan ikke sende tilbake behandling, feil tilstand $tilstand" }

        check(utøvendeBeslutter.isBeslutter() || utøvendeBeslutter.isAdmin()) { "Saksbehandler må være beslutter eller administrator" }
        check(this.beslutter == utøvendeBeslutter.navIdent || utøvendeBeslutter.isAdmin()) { "Det er ikke lov å sende en annen sin behandling tilbake til saksbehandler" }
        return this.copy(
            tilstand = BehandlingTilstand.UNDER_BEHANDLING,
        )
    }

    private fun List<Utfallsperiode>.slåSammen(neste: Utfallsperiode): List<Utfallsperiode> {
        if (this.isEmpty()) return listOf(neste)
        val forrige = this.last()
        return if (forrige.kanSlåsSammen(neste)) {
            this.dropLast(1) + forrige.copy(
                tom = neste.tom,
            )
        } else {
            this + neste
        }
    }
}
