package no.nav.tiltakspenger.domene

import no.nav.tiltakspenger.domene.fakta.FødselsdatoFaktum
import no.nav.tiltakspenger.domene.vilkår.ErOver18År
import java.time.LocalDate.now
import java.time.LocalDateTime

object FaktaInhenter {
    fun hentAldersFakta(saksbehandling: Saksbehandling1) {
        Thread.sleep(2000)
        saksbehandling.opplys(
            FødselsdatoFaktum(
                fødselsdato = now(),
                kilde = FaktumKilde.SYSTEM
            )
        )
    }
}

interface Saksbehandling {
    fun behandle(søknad: Søknad)
}

class Førstegangsbehandling private constructor(
    val ident: String,
    var søknad: Søknad?,
    val vilkårsvurderinger: List<Vilkårsvurdering>,
    var tilstand: Tilstand
) : Saksbehandling {
    constructor(ident: String) : this(
        vilkårsvurderinger = listOf(
            Vilkårsvurdering(vilkår = ErOver18År, vurderingsperiode = Periode(fra = now(), til = now())),
        ),
        søknad = null,
        tilstand = Tilstand.Start,
        ident = ident
    )

    override fun behandle(søknad: Søknad) {
        tilstand.behandle(søknad, this)
    }
//    fun hentFakta() {
//        vilkårsvurdering.forEach { it.start }
//        // Noen må hente fakta
//    }

//    fun onTilstandChange(newTilstand: Tilstand) {
//        when (newTilstand) {
//            Tilstand.Tilstandstype.HENTER_OPPLYSNINGER -> hentFakta()
//        }
//    }

    fun vurder() {
//       vilkårsvurdering.forEach { it.vurder() }
    }

    sealed class Tilstand() {
        open fun behandle(søknad: Søknad, førstegangsbehandling: Førstegangsbehandling) {
            println("KAN IKKE BEHANDLE")
        }

        open fun onEntry(førstegangsbehandling: Førstegangsbehandling) {}

        enum class Tilstandstype {
            START,
            SØKNAD_MOTTATT,
            HENTER_OPPLYSNINGER,
            VILKÅRSVURDERING,
            TIL_MANUELL_BEHANDLING,
            FERDIG
        }

        object Start : Tilstand() {
            fun håndterSøknad(søknad: Søknad) {
                // TODO masse greier
                // registere ting på førstegangsbehandlingen gitt fra søknaden
                // trenger PDL-stuff
                //
            }

            override fun behandle(søknad: Søknad, førstegangsbehandling: Førstegangsbehandling) {
                førstegangsbehandling.søknad = søknad
                requireNotNull(førstegangsbehandling.søknad) { "Her burde søknaden være satt" }
                førstegangsbehandling.nesteTilstand(Vurder)
            }
        }

        object Vurder : Tilstand() {
            override fun onEntry(førstegangsbehandling: Førstegangsbehandling) {
                val søknad = requireNotNull(førstegangsbehandling.søknad)
                førstegangsbehandling.vurder()
            }

            fun håndter(faktum: Faktum, førstegangsbehandling: Førstegangsbehandling) {
                // førstegangsbehandling.vurder(faktum)
            }
        }
    }

    private fun nesteTilstand(nestetilstand: Tilstand.Vurder) {
        tilstand = nestetilstand
        tilstand.onEntry(this)
    }
}

class Saksbehandling1(
    private val startet: LocalDateTime = LocalDateTime.now(),
    val ident: String,
    vilkårsVurderinger: List<Vilkårsvurdering>
) {
    private var vilkårsVurderinger: List<Vilkårsvurdering> = vilkårsVurderinger
        get() = vilkårsVurderinger

    fun opplys(faktum: Faktum) {
        vilkårsVurderinger = vilkårsVurderinger.map { vilkår -> vilkår.vurder(faktum) }
    }

    fun erInngangOppfylt(): Boolean {
        return vilkårsVurderinger.erInngangsVilkårOppfylt()
    }

    companion object {
        fun start(ident: String) {
            val vurderinger = inngangsVilkår.map { Vilkårsvurdering(
                vilkår = it,
                vurderingsperiode = Periode(fra = now(), til = now())
            ) }
            FaktaInhenter.hentAldersFakta(
                Saksbehandling1(
                    ident = ident,
                    vilkårsVurderinger = vurderinger
                )
            )
        }
    }
}
