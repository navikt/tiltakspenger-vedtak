package no.nav.tiltakspenger.vedtak.routes.søker

// class InnsendingRoutesTest {

//    private val søkerServiceMock = mockk<SøkerService>()
//    private val innloggetSaksbehandlerProviderMock = mockk<InnloggetSaksbehandlerProvider>()

//    @Test
//    fun `kalle med en ident i body burde svare ok`() {
//        val søkerId = SøkerId.random()
//        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandler()
//        every {
//            søkerServiceMock.hentSøkerOgSøknader(søkerId, saksbehandler())
//        } returns SøkerDTO(
//            søkerId = "",
//            ident = "1234",
//            personopplysninger = PersonopplysningerDTO(
//                fornavn = "Foo",
//                etternavn = "Bar",
//                ident = "",
//                fødselsdato = LocalDate.now(),
//                barn = listOf(),
//                fortrolig = false,
//                strengtFortrolig = false,
//                skjermet = false,
//            ),
//            behandlinger = listOf(
//                KlarForBehandlingDTO(
//                    søknad = SøknadDTO(
//                        id = "",
//                        søknadId = "",
//                        søknadsdato = 18.november(2022),
//                        arrangoernavn = null,
//                        tiltakskode = null,
//                        beskrivelse = null,
//                        startdato = 18.november(2022),
//                        sluttdato = null,
//                        antallDager = 0,
//                        fritekst = null,
//                        vedlegg = emptyList(),
//                    ),
// //                    registrerteTiltak = listOf(),
//                    vurderingsperiode = ÅpenPeriodeDTO(fra = 18.november(2022), til = null),
//                    statligeYtelser = StatligeYtelserDTO(
//                        samletUtfall = UtfallDTO.KreverManuellVurdering,
//                        aap = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.ARENA,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                        dagpenger = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.ARENA,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                        uføre = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.EF,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                        pleiepengerNærstående = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.FPSAK,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                        pleiepengerSyktBarn = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.FPSAK,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                        foreldrepenger = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.FPSAK,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                        svangerskapspenger = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.FPSAK,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                        opplæringspenger = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.K9SAK,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                        omsorgspenger = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.FPSAK,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                        overgangsstønad = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.EF,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                        sykepenger = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.SAKSB,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                        gjenlevendepensjon = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.SAKSB,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                        alderspensjon = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.SØKNAD,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                        supplerendeStønad = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.SØKNAD,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                        supplerendeStønadFlyktning = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.SØKNAD,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                        supplerendeStønadAlder = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.SØKNAD,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                    ),
//                    kommunaleYtelser = KommunaleYtelserDTO(
//                        samletUtfall = UtfallDTO.KreverManuellVurdering,
//                        kvp = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.SØKNAD,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                        introProgrammet = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.SØKNAD,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//
//                    ),
//                    pensjonsordninger = PensjonsordningerDTO(
//                        samletUtfall = UtfallDTO.KreverManuellVurdering,
//                        perioder = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.PESYS,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                    ),
//                    lønnsinntekt = LønnsinntekterDTO(
//                        samletUtfall = UtfallDTO.KreverManuellVurdering,
//                        perioder = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.SØKNAD,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                    ),
//                    institusjonsopphold = InstitusjonsoppholdDTO(
//                        samletUtfall = UtfallDTO.KreverManuellVurdering,
//                        perioder = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.SØKNAD,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                    ),
//                    barnetillegg = emptyList(),
//                    tiltakspengerYtelser = TiltakspengerDTO(
//                        samletUtfall = UtfallDTO.KreverManuellVurdering,
//                        perioder = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.SØKNAD,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.KreverManuellVurdering,
//                            ),
//                        ),
//                    ),
//                    alderVilkårsvurdering = AlderVilkårsvurderingDTO(
//                        samletUtfall = UtfallDTO.Oppfylt,
//                        perioder = listOf(
//                            VilkårsvurderingDTO(
//                                kilde = Kilde.PDL,
//                                detaljer = "",
//                                periode = null,
//                                kreverManuellVurdering = false,
//                                utfall = UtfallDTO.Oppfylt,
//                            ),
//                        ),
//                    ),
//                    konklusjon = KonklusjonDTO(), // TODO: Denne mangler innhold
//                    hash = "hash",
//                ),
//            ),
//        )
//
//        testApplication {
//            application {
//                // vedtakTestApi()
//                jacksonSerialization()
//                routing {
//                    søkerRoutes(
//                        innloggetSaksbehandlerProviderMock,
//                        søkerServiceMock,
//                    )
//                }
//            }
//
//            defaultRequest(
//                HttpMethod.Get,
//                url {
//                    protocol = URLProtocol.HTTPS
//                    path("$søkerPath/$søkerId")
//                },
//            ).apply {
//                status shouldBe HttpStatusCode.OK
//                contentType() shouldBe ContentType.parse("application/json; charset=UTF-8")
//                println(bodyAsText())
//                JSONAssert.assertEquals(
//                    expectedSøkerMedSøknader,
//                    bodyAsText(),
//                    JSONCompareMode.LENIENT,
//                )
//            }
//        }
//    }

//    @Test
//    fun `at saksbehandler ikke har tilgang burde svare forbidden`() {
//        val søkerId = SøkerId.random()
//        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandler()
//        every {
//            søkerServiceMock.hentSøkerOgSøknader(søkerId, saksbehandler())
//        } throws TilgangException("Test")
//
//        testApplication {
//            application {
//                // vedtakTestApi()
//                jacksonSerialization()
//                routing {
//                    søkerRoutes(
//                        innloggetSaksbehandlerProviderMock,
//                        søkerServiceMock,
//                    )
//                }
//            }
//
//            defaultRequest(
//                HttpMethod.Get,
//                url {
//                    protocol = URLProtocol.HTTPS
//                    path("$søkerPath/$søkerId")
//                },
//            ).apply {
//                status shouldBe HttpStatusCode.Forbidden
//            }
//        }
//    }
//
//    @Test
//    fun `kalle med en ident i body som ikke finnes i db burde svare med 404 Not Found`() {
//        val sokerId = SøkerId.random()
//        every { innloggetSaksbehandlerProviderMock.hentInnloggetSaksbehandler(any()) } returns saksbehandler()
//        every {
//            søkerServiceMock.hentSøkerOgSøknader(sokerId, saksbehandler())
//        } returns null
//
//        testApplication {
//            application {
//                // vedtakTestApi()
//                jacksonSerialization()
//                routing {
//                    søkerRoutes(
//                        innloggetSaksbehandlerProviderMock,
//                        søkerServiceMock,
//                    )
//                }
//            }
//
//            defaultRequest(
//                HttpMethod.Get,
//                url {
//                    protocol = URLProtocol.HTTPS
//                    path("$søkerPath/$sokerId")
//                },
//            ).apply {
//                status shouldBe HttpStatusCode.NotFound
//                assertEquals("Søker ikke funnet", bodyAsText())
//            }
//        }
//    }
//
//    private val expectedSøkerMedSøknader = """
//        {
//          "ident": "1234",
//          "behandlinger": [
//            {
//              "søknad": {
//                "id": "",
//                "søknadId": "",
//                "søknadsdato": "2022-11-18",
//                "arrangoernavn": null,
//                "tiltakskode": null,
//                "beskrivelse": null,
//                "startdato": "2022-11-18",
//                "sluttdato": null,
//                "antallDager": 0,
//                "vedlegg": []
//              },
//              "vurderingsperiode": {
//                "fra": "2022-11-18",
//                "til": null
//              },
//              "statligeYtelser": {
//                "samletUtfall": "KreverManuellVurdering",
//                "aap": [
//                  {
//                    "kilde": "ARENA",
//                    "detaljer": "",
//                    "periode": null,
//                    "kreverManuellVurdering": false,
//                    "utfall": "KreverManuellVurdering"
//                  }
//                ],
//                "dagpenger": [
//                  {
//                    "kilde": "ARENA",
//                    "detaljer": "",
//                    "periode": null,
//                    "kreverManuellVurdering": false,
//                    "utfall": "KreverManuellVurdering"
//                  }
//                ],
//                "uføre": [
//                  {
//                    "kilde": "EF",
//                    "detaljer": "",
//                    "periode": null,
//                    "kreverManuellVurdering": false,
//                    "utfall": "KreverManuellVurdering"
//                  }
//                ],
//                "pleiepengerNærstående": [
//                  {
//                    "kilde": "FPSAK",
//                    "detaljer": "",
//                    "periode": null,
//                    "kreverManuellVurdering": false,
//                    "utfall": "KreverManuellVurdering"
//                  }
//                ],
//                "pleiepengerSyktBarn": [
//                  {
//                    "kilde": "FPSAK",
//                    "detaljer": "",
//                    "periode": null,
//                    "kreverManuellVurdering": false,
//                    "utfall": "KreverManuellVurdering"
//                  }
//                ],
//                "foreldrepenger": [
//                  {
//                    "kilde": "FPSAK",
//                    "detaljer": "",
//                    "periode": null,
//                    "kreverManuellVurdering": false,
//                    "utfall": "KreverManuellVurdering"
//                  }
//                ],
//                "svangerskapspenger": [
//                  {
//                    "kilde": "FPSAK",
//                    "detaljer": "",
//                    "periode": null,
//                    "kreverManuellVurdering": false,
//                    "utfall": "KreverManuellVurdering"
//                  }
//                ],
//                "omsorgspenger": [
//                  {
//                    "kilde": "FPSAK",
//                    "detaljer": "",
//                    "periode": null,
//                    "kreverManuellVurdering": false,
//                    "utfall": "KreverManuellVurdering"
//                  }
//                ],
//                "opplæringspenger": [
//                  {
//                    "kilde": "K9SAK",
//                    "detaljer": "",
//                    "periode": null,
//                    "kreverManuellVurdering": false,
//                    "utfall": "KreverManuellVurdering"
//                  }
//                ]
//              },
//              "kommunaleYtelser": {
//                "samletUtfall": "KreverManuellVurdering",
//                "kvp": [
//                  {
//                    "kilde": "SØKNAD",
//                    "detaljer": "",
//                    "periode": null,
//                    "kreverManuellVurdering": false,
//                    "utfall": "KreverManuellVurdering"
//                  }
//                ],
//                "introProgrammet": [
//                  {
//                    "kilde": "SØKNAD",
//                    "detaljer": "",
//                    "periode": null,
//                    "kreverManuellVurdering": false,
//                    "utfall": "KreverManuellVurdering"
//                  }
//                ]
//              },
//              "pensjonsordninger": {
//                "samletUtfall": "KreverManuellVurdering",
//                "perioder": [
//                  {
//                    "kilde": "PESYS",
//                    "detaljer": "",
//                    "periode": null,
//                    "kreverManuellVurdering": false,
//                    "utfall": "KreverManuellVurdering"
//                  }
//                ]
//              },
//              "lønnsinntekt": {
//                "samletUtfall": "KreverManuellVurdering",
//                "perioder": [
//                  {
//                    "kilde": "SØKNAD",
//                    "detaljer": "",
//                    "periode": null,
//                    "kreverManuellVurdering": false,
//                    "utfall": "KreverManuellVurdering"
//                  }
//                ]
//              },
//              "institusjonsopphold": {
//                "samletUtfall": "KreverManuellVurdering",
//                "perioder": [
//                  {
//                    "kilde": "SØKNAD",
//                    "detaljer": "",
//                    "periode": null,
//                    "kreverManuellVurdering": false,
//                    "utfall": "KreverManuellVurdering"
//                  }
//                ]
//              },
//              "barnetillegg": []
//            }
//          ],
//          "personopplysninger": {
//            "fornavn": "Foo",
//            "etternavn": "Bar",
//            "ident": "",
//            "barn": []
//          }
//        }
//    """.trimIndent()
// }
