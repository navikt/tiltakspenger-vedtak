package no.nav.tiltakspenger.vedtak.service.søknad


import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.AAPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.DagpengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.InstitusjonVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.InstitusjonsoppholdVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.IntroProgrammetVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.KVPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.KommunaleYtelserVilkårsvurderinger
import no.nav.tiltakspenger.vilkårsvurdering.LønnsinntektVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.LønnsinntektVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.PensjonsinntektVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.PensjonsinntektVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.StatligeYtelserVilkårsvurderinger
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.VilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.Vilkårsvurderinger
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import java.time.LocalDate

class BehandlingMapper {
    fun mapSøkerMedSøknad(søker: Søker, søknadId: String): BehandlingDTO? {
        val søknad = søker.søknader.firstOrNull { it.søknadId == søknadId } ?: return null
        val vurderingsperiode = Periode(søknad.tiltak.startdato, søknad.tiltak.sluttdato ?: LocalDate.MAX)
        val vilkårsvurderinger = vilkårsvurderinger(søker, vurderingsperiode, søknad)

        return BehandlingDTO(
            personopplysninger = PersonopplysningerDTO(
                fornavn = søker.personopplysningerSøker()?.fornavn,
                etternavn = søker.personopplysningerSøker()?.etternavn,
                ident = søker.personopplysningerSøker()?.ident ?: søknad.ident,
                barn = mapBarn(søker)
            ),
            søknad = SøknadDTO(
                søknadId = søknad.søknadId,
                søknadsdato = (søknad.opprettet ?: søknad.tidsstempelHosOss).toLocalDate(),
                arrangoernavn = søknad.tiltak.arrangoernavn,
                tiltakskode = søknad.tiltak.tiltakskode?.navn,
                startdato = søknad.tiltak.startdato,
                sluttdato = søknad.tiltak.sluttdato,
                antallDager = 2, // TODO
            ),
            registrerteTiltak = søker.tiltak.map {
                TiltakDTO(
                    arrangør = it.arrangør,
                    navn = it.tiltak.navn,
                    periode = it.deltakelsePeriode.fom?.let { fom ->
                        PeriodeDTO(
                            fra = fom,
                            til = it.deltakelsePeriode.tom
                        )
                    },
                    prosent = it.deltakelseProsent,
                    dagerIUken = it.antallDagerPerUke,
                    status = it.deltakerStatus.tekst,
                )
            },
            vurderingsperiode = PeriodeDTO(
                fra = vurderingsperiode.fra,
                til = vurderingsperiode.til
            ),
            statligeYtelser = mapVilkårsvurderingKategori(vilkårsvurderinger.statligeYtelser),
            kommunaleYtelser = mapKommunalseVilkårsvurderingKategori(vilkårsvurderinger.kommunaleYtelser),
            pensjonsordninger = mapVilkårsvurderingKategori(vilkårsvurderinger.pensjonsordninger),
            lønnsinntekt = mapVilkårsvurderingKategori(vilkårsvurderinger.lønnsinntekt),
            institusjonsopphold = mapVilkårsvurderingKategori(vilkårsvurderinger.institusjonopphold),
        )
    }

    private fun mapKommunalseVilkårsvurderingKategori(kommunaleYtelserVilkårsvurderinger: KommunaleYtelserVilkårsvurderinger): KommunaleVilkårsVurderingsKategoriDTO {
        return KommunaleVilkårsVurderingsKategoriDTO(
            tittel = kommunaleYtelserVilkårsvurderinger.lovreferanse().paragraf,
            lovreferanse = kommunaleYtelserVilkårsvurderinger.lovreferanse().paragraf,
            utfall = kommunaleYtelserVilkårsvurderinger.samletUtfall().mapToUtfallDTO(),
            detaljer = kommunaleYtelserVilkårsvurderinger.samletUtfall().mapToUtfallDTO().name,
            introProgrammet = kommunaleYtelserVilkårsvurderinger.intro.vurderinger().map { mapVilkårsvurderingDTO(it) },
            kvp = kommunaleYtelserVilkårsvurderinger.kvp.vurderinger().map { mapVilkårsvurderingDTO(it) },
        )
    }


    private fun mapVilkårsvurderingKategori(v: VilkårsvurderingKategori): VilkårsVurderingsKategoriDTO =
        VilkårsVurderingsKategoriDTO(
            tittel = v.lovreferanse().paragraf,
            lovreferanse = v.lovreferanse().paragraf,
            utfall = v.samletUtfall().mapToUtfallDTO(),
            detaljer = v.samletUtfall().mapToUtfallDTO().name,
            vilkårsvurderinger = v.vurderinger().map { vurdering ->
                mapVilkårsvurderingDTO(vurdering)
            }
        )

    private fun mapVilkårsvurderingDTO(vurdering: Vurdering) =
        VilkårsvurderingDTO(
            utfall = vurdering.utfall.mapToUtfallDTO(),
            periode = vurdering.fom?.let { fom ->
                PeriodeDTO(
                    fra = fom,
                    til = vurdering.tom
                )
            },
            vilkår = vurdering.lovreferanse.paragraf,
            kilde = vurdering.kilde,
            detaljer = vurdering.detaljer,
        )

    private fun mapBarn(søker: Søker) = listOf<BarnDTO>()
    /*
    søker.personopplysningerBarnMedIdent().map {
        BarnDTO(
            fornavn = it.fornavn,
            etternavn = it.etternavn,
            ident = it.ident,
            bosted = it.oppholdsland,
        )
    } + søker.personopplysningerBarnUtenIdent().map {
        BarnDTO(
            fornavn = it.fornavn!!,
            etternavn = it.etternavn!!,
            ident = null, // TODO
            bosted = null, // TODO
        )
    }
     */

    private fun vilkårsvurderinger(
        søker: Søker,
        vurderingsperiode: Periode,
        søknad: Søknad
    ) = Vilkårsvurderinger(
        statligeYtelser = StatligeYtelserVilkårsvurderinger(
            aap = AAPVilkårsvurdering(ytelser = søker.ytelser, vurderingsperiode = vurderingsperiode),
            dagpenger = DagpengerVilkårsvurdering(ytelser = søker.ytelser, vurderingsperiode = vurderingsperiode),
        ),
        kommunaleYtelser = KommunaleYtelserVilkårsvurderinger(
            intro = IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode),
            kvp = KVPVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode),
        ),
        pensjonsordninger = PensjonsinntektVilkårsvurderingKategori(
            pensjonsinntektVilkårsvurdering = PensjonsinntektVilkårsvurdering(
                søknad = søknad,
                vurderingsperiode = vurderingsperiode,
            )
        ),
        lønnsinntekt = LønnsinntektVilkårsvurderingKategori(
            lønnsinntektVilkårsvurdering = LønnsinntektVilkårsvurdering(
                søknad = søknad,
                vurderingsperiode = vurderingsperiode,
            )
        ),
        institusjonopphold = InstitusjonVilkårsvurderingKategori(
            institusjonsoppholdVilkårsvurdering = InstitusjonsoppholdVilkårsvurdering(
                søknad = søknad,
                vurderingsperiode = vurderingsperiode,
                institusjonsopphold = emptyList(),
            )
        )
    )

    private fun Utfall.mapToUtfallDTO(): UtfallDTO {
        return when (this) {
            Utfall.OPPFYLT -> UtfallDTO.Oppfylt
            Utfall.IKKE_OPPFYLT -> UtfallDTO.IkkeOppfylt
            Utfall.KREVER_MANUELL_VURDERING -> UtfallDTO.KreverManuellVurdering
            Utfall.IKKE_IMPLEMENTERT -> UtfallDTO.IkkeImplementert
        }
    }
}
