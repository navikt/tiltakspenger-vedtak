package no.nav.tiltakspenger.vedtak.service

import no.nav.tiltakspenger.domene.Søknad
import no.nav.tiltakspenger.domene.Tiltak
import no.nav.tiltakspenger.vedtak.repository.SøkerRepository
import no.nav.tiltakspenger.vedtak.routes.person.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

class PersonServiceImpl(
    val søkerRepository: SøkerRepository
) : PersonService {
    override fun hentPerson(ident: String): PersonDTO? {
        val søker = søkerRepository.hent(ident) ?: return null

        return PersonDTO(
            personopplysninger = PersonopplysningerDTO(
                fornavn = søker.personopplysninger!!.fornavn,
                etternavn = søker.personopplysninger!!.etternavn,
                ident = søker.ident,
                barn = søker.barn.map { barn ->
                    BarnDTO(
                        fornavn = barn.fornavn,
                        etternavn = barn.etternavn,
                        ident = barn.ident,
                        bosted = barn.bosted,
                    )
                },
            ),
            behandlinger = listOf(
                BehandlingDTO(
                    id = "behandlingId",
                    søknad = Søknad(
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
                    tiltak = TiltakDTO(
                        arrangør = "Joblearn",
                        navn = "Gruppe AMO",
                        periode = PeriodeDTO(
                            fra = LocalDate.of(2022, Month.APRIL, 1),
                            til = LocalDate.of(2022, Month.APRIL, 20),
                        ),
                        prosent = 80,
                        dagerIUken = 4,
                        status = "Godkjent"
                    ),
                    periode = PeriodeDTO(
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
}