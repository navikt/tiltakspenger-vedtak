package no.nav.tiltakspenger.vedtak.service.søknad

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vilkårsvurdering.Inngangsvilkårsvurderinger
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import no.nav.tiltakspenger.vilkårsvurdering.kategori.InstitusjonVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.KommunaleYtelserVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.LønnsinntektVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.PensjonsinntektVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.StatligeYtelserVilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.kategori.VilkårsvurderingKategori
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.AAPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.DagpengerVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.InstitusjonsoppholdVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.IntroProgrammetVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.KVPVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.LønnsinntektVilkårsvurdering
import no.nav.tiltakspenger.vilkårsvurdering.vurdering.PensjonsinntektVilkårsvurdering
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
                tiltakskode = søknad.tiltak.tiltakskode?.navn ?: "Annet",
                beskrivelse = when (søknad.tiltak) {
                    is Tiltak.ArenaTiltak -> null
                    is Tiltak.BrukerregistrertTiltak -> (søknad.tiltak as Tiltak.BrukerregistrertTiltak).beskrivelse
                },
                startdato = søknad.tiltak.startdato,
                sluttdato = søknad.tiltak.sluttdato,
                antallDager = if (søknad.tiltak is Tiltak.BrukerregistrertTiltak) {
                    (søknad.tiltak as Tiltak.BrukerregistrertTiltak).antallDager
                } else null,
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
            barnetillegg = mapBarnetillegg(søknad.barnetillegg, søker.personopplysningerBarnMedIdent())
        )
    }

    private fun mapBarnetillegg(
        barnetillegg: List<Barnetillegg>,
        barnMedIdent: List<Personopplysninger.BarnMedIdent>,
    ): List<BarnetilleggDTO> {
        return barnetillegg.map {
            BarnetilleggDTO(
                navn = if (it.fornavn != null) it.fornavn + " " + it.etternavn else null,
                alder = it.alder,
                fødselsdato = if (it is Barnetillegg.UtenIdent) it.fødselsdato
                else barnMedIdent.firstOrNull { b -> b.ident == (it as Barnetillegg.MedIdent).ident }?.fødselsdato,
                bosatt = it.oppholdsland,
                kilde = "Søknad",
                utfall = UtfallDTO.Oppfylt,
                søktBarnetillegg = it.søktBarnetillegg
            )
        }
    }

    private fun mapKommunalseVilkårsvurderingKategori(kommunaleYtelserVilkårsvurderingKategori: KommunaleYtelserVilkårsvurderingKategori): KommunaleVilkårsVurderingsKategoriDTO {
        return KommunaleVilkårsVurderingsKategoriDTO(
            ytelse = kommunaleYtelserVilkårsvurderingKategori.vilkår().tittel,
            lovreferanse = kommunaleYtelserVilkårsvurderingKategori.vilkår().lovreferanse.paragraf,
            utfall = kommunaleYtelserVilkårsvurderingKategori.samletUtfall().mapToUtfallDTO(),
            detaljer = kommunaleYtelserVilkårsvurderingKategori.samletUtfall().mapToUtfallDTO().name,
            introProgrammet = kommunaleYtelserVilkårsvurderingKategori.intro.vurderinger()
                .map { mapVilkårsvurderingDTO(it) },
            kvp = kommunaleYtelserVilkårsvurderingKategori.kvp.vurderinger().map { mapVilkårsvurderingDTO(it) },
        )
    }


    private fun mapVilkårsvurderingKategori(v: VilkårsvurderingKategori): VilkårsVurderingsKategoriDTO =
        VilkårsVurderingsKategoriDTO(
            ytelse = v.vilkår().tittel,
            lovreferanse = v.vilkår().lovreferanse.paragraf,
            utfall = v.samletUtfall().mapToUtfallDTO(),
            detaljer = v.samletUtfall().mapToUtfallDTO().name,
            vilkårsvurderinger = v.vurderinger().map { vurdering ->
                mapVilkårsvurderingDTO(vurdering)
            }
        )

    private fun mapVilkårsvurderingDTO(vurdering: Vurdering) =
        VilkårsvurderingDTO(
            ytelse = vurdering.vilkår.tittel,
            lovreferanse = vurdering.vilkår.lovreferanse.paragraf,
            utfall = vurdering.utfall.mapToUtfallDTO(),
            periode = vurdering.fom?.let { fom ->
                PeriodeDTO(
                    fra = fom,
                    til = vurdering.tom
                )
            },
            vilkår = vurdering.vilkår.lovreferanse.paragraf,
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
    ) = Inngangsvilkårsvurderinger(
        statligeYtelser = StatligeYtelserVilkårsvurderingKategori(
            aap = AAPVilkårsvurdering(ytelser = søker.ytelser, vurderingsperiode = vurderingsperiode),
            dagpenger = DagpengerVilkårsvurdering(ytelser = søker.ytelser, vurderingsperiode = vurderingsperiode),
        ),
        kommunaleYtelser = KommunaleYtelserVilkårsvurderingKategori(
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
                // institusjonsopphold = emptyList(),
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
