package no.nav.tiltakspenger.vedtak.routes.person


import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.*
import java.time.LocalDate

class PersonMapper {
    fun mapPerson(søker: Søker, søknadId: String): BehandlingDTO {
        //TODO: Må velge de riktige..
        val søknad: Søknad = søker.søknader.first { it.søknadId == søknadId }
        val vurderingsperiode = Periode(LocalDate.now(), LocalDate.now())
        val vilkårsvurderinger = Vilkårsvurderinger(
            statligeYtelserVilkårsvurderinger = StatligeYtelserVilkårsvurderinger(
                aap = AAPVilkårsvurdering(ytelser = søker.ytelser, vurderingsperiode = vurderingsperiode),
                dagpenger = DagpengerVilkårsvurdering(ytelser = søker.ytelser, vurderingsperiode = vurderingsperiode),
            ),
            kommunaleYtelserVilkårsvurderinger = KommunaleYtelserVilkårsvurderinger(
                intro = IntroProgrammetVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode),
                kvp = KVPVilkårsvurdering(søknad = søknad, vurderingsperiode = vurderingsperiode),
            )
        )

        val behandlingDTO = BehandlingDTO(
            personopplysninger = PersonopplysningerDTO(
                fornavn = søker.personopplysningerSøker()!!.fornavn,
                etternavn = søker.personopplysningerSøker()!!.etternavn,
                ident = søker.personopplysningerSøker()!!.ident,
                barn = søker.personopplysningerBarnMedIdent().map {
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
            vurderinger = listOf(
                vilkårsvurderinger.statligeYtelserVilkårsvurderinger,
                vilkårsvurderinger.kommunaleYtelserVilkårsvurderinger
            ).map {
                VilkårsVurderingsKategori(
                    tittel = it.lovreferanse().paragraf,
                    utfall = it.samletUtfall().mapToUtfallDTO(),
                    vilkårsvurderinger = it.vurderinger().map { vurdering ->
                        VilkårsvurderingDTO(
                            utfall = vurdering.utfall.mapToUtfallDTO(),
                            periode = PeriodeDTO(
                                fra = vurdering.fom ?: LocalDate.MIN,
                                til = vurdering.tom ?: LocalDate.MAX
                            ),
                            vilkår = vurdering.lovreferanse.paragraf,
                            kilde = vurdering.kilde,
                            detaljer = "Testdetaljer"
                        )
                    },
                    detaljer = "Testdetaljer",
                    lovreferanse = "§ 7"
                )
            }
        )
        return behandlingDTO
    }
}

private fun Utfall.mapToUtfallDTO(): UtfallDTO {
    return when (this) {
        Utfall.OPPFYLT -> UtfallDTO.Oppfylt
        Utfall.IKKE_OPPFYLT -> UtfallDTO.IkkeOppfylt
        Utfall.KREVER_MANUELL_VURDERING -> UtfallDTO.KreverManuellVurdering
        Utfall.IKKE_IMPLEMENTERT -> UtfallDTO.IkkeImplementert
    }
}
