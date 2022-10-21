package no.nav.tiltakspenger.vedtak.routes.person


import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak.ArenaTiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkårsvurderinger
import java.time.LocalDate
import java.util.*
import no.nav.tiltakspenger.domene.Søknad as DomeneSøknad // TODO: Denne må man ha en DTO-versjon av!
import no.nav.tiltakspenger.domene.Tiltak as DomeneTiltak // TODO: Denne må man ha en DTO-versjon av!

class PersonMapper {
    fun mapPerson(søker: Søker, vilkårsvurderinger: Vilkårsvurderinger): PersonDTO {
        //TODO: Må velge de riktige..
        val søknad: Søknad = søker.søknader.first()
        val tiltak: Tiltaksaktivitet =
            søker.arenaTiltaksaktivitetForSøknad(søknad) ?: søker.tiltak.first() // TODO: Må takle å være null

        val personDto = PersonDTO(
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
            behandlinger = listOf(
                BehandlingDTO(
                    id = UUID.randomUUID().toString(),
                    søknad = DomeneSøknad(
                        id = søknad.søknadId,
                        ident = søker.personopplysningerSøker()!!.ident,
                        opprettet = søknad.opprettet ?: søknad.tidsstempelHosOss,
                        tiltak = DomeneTiltak(
                            id = if (søknad.tiltak is ArenaTiltak) {
                                (søknad.tiltak as ArenaTiltak).arenaId
                            } else {
                                "?"
                            },
                            arrangør = søknad.tiltak.arrangoernavn ?: "?",
                            navn = søknad.tiltak.tiltakskode?.navn ?: "?",
                            startDato = søknad.tiltak.startdato,
                            sluttDato = søknad.tiltak.sluttdato ?: LocalDate.MAX
                        ),
                        deltarKvp = søknad.deltarKvp,
                    ),
                    tiltak = TiltakDTO(
                        arrangør = tiltak.arrangør ?: "?",
                        navn = tiltak.tiltak.navn,
                        periode = PeriodeDTO(
                            fra = tiltak.deltakelsePeriode.fom ?: LocalDate.MIN,
                            til = tiltak.deltakelsePeriode.tom ?: LocalDate.MAX
                        ),
                        prosent = tiltak.deltakelseProsent?.toInt() ?: 0,
                        dagerIUken = tiltak.antallDagerPerUke?.toInt() ?: 0,
                        status = tiltak.deltakerStatus.tekst,
                    ),
                    periode = PeriodeDTO(
                        fra = tiltak.deltakelsePeriode.fom ?: søknad.tiltak.startdato,
                        til = tiltak.deltakelsePeriode.tom ?: søknad.tiltak.sluttdato ?: LocalDate.MAX
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
                                    kilde = vurdering.kilde
                                )
                            }
                        )
                    }
                )
            )
        )
        return personDto
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
