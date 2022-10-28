package no.nav.tiltakspenger.vedtak.service

import no.nav.tiltakspenger.domene.Søknad
import no.nav.tiltakspenger.domene.Tiltak
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository
import no.nav.tiltakspenger.vedtak.routes.person.BarnDTO
import no.nav.tiltakspenger.vedtak.routes.person.BehandlingDTO
import no.nav.tiltakspenger.vedtak.routes.person.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.person.PersonDTO
import no.nav.tiltakspenger.vedtak.routes.person.PersonopplysningerDTO
import no.nav.tiltakspenger.vedtak.routes.person.TiltakDTO
import no.nav.tiltakspenger.vedtak.routes.person.UtfallDTO
import no.nav.tiltakspenger.vedtak.routes.person.VilkårsVurderingsKategori
import no.nav.tiltakspenger.vedtak.routes.person.VilkårsvurderingDTO
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

class PersonServiceImpl(
    val søkerRepository: SøkerRepository
) : PersonService {
    override fun hentPerson(ident: String): PersonDTO? {
        val søker = søkerRepository.hent(ident) ?: return null

        return BehandlingDTO(
            personopplysninger = PersonopplysningerDTO(
                fornavn = søker.personopplysningerSøker()!!.fornavn,
                etternavn = søker.personopplysningerSøker()!!.etternavn,
                ident = søker.ident,
                barn = søker.personopplysningerBarnMedIdent().map { barn ->
                    BarnDTO(
                        fornavn = barn.fornavn,
                        etternavn = barn.etternavn,
                        ident = barn.ident,
                        bosted = ""
                    )
                } + søker.personopplysningerBarnUtenIdent().map { barn ->
                    BarnDTO(
                        fornavn = barn.fornavn!!,
                        etternavn = barn.etternavn!!,
                        ident = barn.fødselsdato.toString(), // TODO her må vi endre sammen med frontend
                        bosted = ""
                    )
                },
            ),

                    søknad = no.nav.tiltakspenger.vedtak.routes.person.SøknadDTO(
                        id = "søknadId",
                        ident = "personIdent?",
                        opprettet = LocalDateTime.of(2022, 5, 30, 20, 0, 0),
                        tiltak = Tiltak(
                            id = "tiltakId",
                            arrangør = "arrangør",
                            navn = "navnTiltak",
                            startDato = LocalDate.of(2022, Month.APRIL, 30),
                            sluttDato = LocalDate.of(2022, Month.APRIL, 30),
                        ),
                        deltarKvp = false
                    ),
                    registrerteTiltak = listOf(TiltakDTO(
                        arrangør = "Joblearn",
                        navn = "Gruppe AMO",
                        periode = PeriodeDTO(
                            fra = LocalDate.of(2022, Month.APRIL, 1),
                            til = LocalDate.of(2022, Month.APRIL, 20),
                        ),
                        prosent = 80,
                        dagerIUken = 4,
                        status = "Godkjent"
                    )),
                    vurderingsperiode = PeriodeDTO(
                        fra = LocalDate.of(2022, Month.APRIL, 1),
                        til = LocalDate.of(2022, Month.APRIL, 20),
                    ),
                    vurderinger = listOf(
                        VilkårsVurderingsKategori(
                            tittel = "Statlige ytelser",
                            utfall = UtfallDTO.Uavklart,
                            vilkårsvurderinger = listOf(
                                VilkårsvurderingDTO(
                                    utfall = UtfallDTO.Oppfylt,
                                    periode = PeriodeDTO(
                                        fra = LocalDate.of(2022, Month.APRIL, 1),
                                        til = LocalDate.of(2022, Month.APRIL, 20),
                                    ),
                                    vilkår = "Dagpenger",
                                    kilde = "Arena"
                                ),
                                VilkårsvurderingDTO(
                                    utfall = UtfallDTO.Oppfylt,
                                    periode = PeriodeDTO(
                                        fra = LocalDate.of(2022, Month.APRIL, 1),
                                        til = LocalDate.of(2022, Month.APRIL, 20),
                                    ),
                                    vilkår = "AAP",
                                    kilde = "Arena"
                                ),
                                VilkårsvurderingDTO(
                                    utfall = UtfallDTO.Uavklart,
                                    periode = PeriodeDTO(
                                        fra = LocalDate.of(2022, Month.APRIL, 1),
                                        til = LocalDate.of(2022, Month.APRIL, 20),
                                    ),
                                    vilkår = "Tiltakspenger",
                                    kilde = "Arena"
                                )
                            ),
                        )
                    )
                )
            ),
        )
    }

    override fun hentSøkerOgSøknader(ident: String): SøkerDTO? {
        val søker = søkerRepository.hent(ident) ?: return null
        return SøkerDTO(
            ident = søker.ident,
            søknader = søker.søknader.map {
                SøknadDTO(
                    søknadId = it.søknadId,
                    arrangoernavn = it.tiltak.arrangoernavn ?: "Ukjent",
                    tiltakskode = it.tiltak.tiltakskode?.navn ?: "Ukjent",
                    startdato = it.tiltak.startdato,
                    sluttdato = it.tiltak.sluttdato,
                )
            }
        )
    }
}
