package no.nav.tiltakspenger.vedtak.routes.livsopphold

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.server.util.url
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import no.nav.tiltakspenger.common.TestApplicationContext
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.libs.common.Rolle
import no.nav.tiltakspenger.libs.common.Roller
import no.nav.tiltakspenger.objectmothers.ObjectMother.fraOgMedDatoJa
import no.nav.tiltakspenger.objectmothers.ObjectMother.ja
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.periodeJa
import no.nav.tiltakspenger.objectmothers.førstegangsbehandlingUavklart
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.vedtak.clients.defaultObjectMapper
import no.nav.tiltakspenger.vedtak.routes.behandling.BEHANDLING_PATH
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.SamletUtfallDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.LivsoppholdVilkårDTO
import no.nav.tiltakspenger.vedtak.routes.behandling.vilkår.livsopphold.livsoppholdRoutes
import no.nav.tiltakspenger.vedtak.routes.configureExceptions
import no.nav.tiltakspenger.vedtak.routes.defaultRequest
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO
import no.nav.tiltakspenger.vedtak.routes.jacksonSerialization
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import org.junit.jupiter.api.Test

class LivsoppholdRoutesTest {
    private val mockInnloggetSaksbehandlerProvider = mockk<InnloggetSaksbehandlerProvider>()

    private val objectMapper: ObjectMapper = defaultObjectMapper()

    private val saksbehandlerIdent = "Q123456"
    private val saksbehandler =
        Saksbehandler(
            saksbehandlerIdent,
            "Superman",
            "a@b.c",
            Roller(listOf(Rolle.SAKSBEHANDLER, Rolle.SKJERMING, Rolle.STRENGT_FORTROLIG_ADRESSE)),
        )

    @Test
    fun `test at endepunkt for henting og lagring av livsopphold fungerer`() = runTest {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns saksbehandler

        with(TestApplicationContext()) {
            val tac = this
            val sak = this.førstegangsbehandlingUavklart(
                saksbehandler = saksbehandler,
            )
            val behandlingId = sak.førstegangsbehandling.id
            testApplication {
                application {
                    jacksonSerialization()
                    routing {
                        livsoppholdRoutes(
                            innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                            livsoppholdVilkårService = tac.førstegangsbehandlingContext.livsoppholdVilkårService,
                            behandlingService = tac.førstegangsbehandlingContext.behandlingService,
                            auditService = tac.personContext.auditService,
                        )
                    }
                }
                // Sjekk at man kan kjøre Get
                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/livsopphold")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val livsoppholdVilkår = objectMapper.readValue<LivsoppholdVilkårDTO>(bodyAsText())
                    livsoppholdVilkår.avklartSaksopplysning.shouldBeNull()
                }

                // Sjekk at man kan si at bruker ikke har livsoppholdytelser
                defaultRequest(
                    HttpMethod.Post,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/livsopphold")
                    },
                ) {
                    setBody(bodyLivsoppholdYtelse(sak.førstegangsbehandling.vurderingsperiode.toDTO(), false))
                }.apply {
                    status shouldBe HttpStatusCode.Created
                }

                // Sjekk at dataene har blitt oppdatert
                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/livsopphold")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val livsoppholdVilkår = objectMapper.readValue<LivsoppholdVilkårDTO>(bodyAsText())
                    livsoppholdVilkår.avklartSaksopplysning!!.harLivsoppholdYtelser.shouldBeFalse()
                    livsoppholdVilkår.avklartSaksopplysning.saksbehandler shouldNotBeNull { this.navIdent shouldBe saksbehandlerIdent }
                }
            }
        }
    }

    @Test
    fun `test at sbh ikke kan si at bruker har livsoppholdytelser`() = runTest {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns saksbehandler

        with(TestApplicationContext()) {
            val tac = this
            val sak = this.førstegangsbehandlingUavklart(
                saksbehandler = saksbehandler,
            )
            val behandlingId = sak.førstegangsbehandling.id

            testApplication {
                application {
                    configureExceptions()
                    jacksonSerialization()
                    routing {
                        livsoppholdRoutes(
                            innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                            livsoppholdVilkårService = tac.førstegangsbehandlingContext.livsoppholdVilkårService,
                            behandlingService = tac.førstegangsbehandlingContext.behandlingService,
                            auditService = tac.personContext.auditService,
                        )
                    }
                }

                // Sjekk at man ikke kan si at bruker har livsoppholdytelser
                defaultRequest(
                    HttpMethod.Post,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/livsopphold")
                    },
                ) {
                    setBody(bodyLivsoppholdYtelse(sak.førstegangsbehandling.vurderingsperiode.toDTO(), true))
                }.apply {
                    status shouldBe HttpStatusCode.NotImplemented
                }
            }
        }
    }

    @Test
    fun `test at livsoppholdytelser blir uavklart om man bare har data fra søknaden`() = runTest {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns saksbehandler

        with(TestApplicationContext()) {
            val tac = this
            val sak = this.førstegangsbehandlingUavklart(
                saksbehandler = saksbehandler,
            )
            val behandlingId = sak.førstegangsbehandling.id
            testApplication {
                application {
                    jacksonSerialization()
                    routing {
                        livsoppholdRoutes(
                            innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                            livsoppholdVilkårService = tac.førstegangsbehandlingContext.livsoppholdVilkårService,
                            behandlingService = tac.førstegangsbehandlingContext.behandlingService,
                            auditService = tac.personContext.auditService,
                        )
                    }
                }
                // Sjekk at man kan kjøre Get
                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/livsopphold")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val livsoppholdVilkår = objectMapper.readValue<LivsoppholdVilkårDTO>(bodyAsText())
                    livsoppholdVilkår.samletUtfall shouldBe SamletUtfallDTO.UAVKLART
                    livsoppholdVilkår.avklartSaksopplysning.shouldBeNull()
                }

                // Sjekk at man kan si at bruker ikke har livsoppholdytelser
                defaultRequest(
                    HttpMethod.Post,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/livsopphold")
                    },
                ) {
                    setBody(bodyLivsoppholdYtelse(sak.førstegangsbehandling.vurderingsperiode.toDTO(), false))
                }.apply {
                    status shouldBe HttpStatusCode.Created
                }

                // Sjekk at dataene har blitt oppdatert
                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/livsopphold")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    val livsoppholdVilkår = objectMapper.readValue<LivsoppholdVilkårDTO>(bodyAsText())
                    livsoppholdVilkår.avklartSaksopplysning!!.harLivsoppholdYtelser.shouldBeFalse()
                    livsoppholdVilkår.avklartSaksopplysning.saksbehandler shouldNotBeNull { this.navIdent shouldBe saksbehandlerIdent }
                }
            }
        }
    }

    @Test
    fun `test alle livsoppholdytelser stemmer overens med søknadsdata`() = runTest {
        every { mockInnloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(any()) } returns saksbehandler

        val søknadMedSykepenger =
            nySøknad(
                sykepenger = periodeJa(fom = 1.januar(2023), tom = 31.mars(2023)),
            )

        val livsoppholdVilkårSykepenger = opprettSakOgKjørGetPåLivsopphold(søknadMedSykepenger)
        livsoppholdVilkårSykepenger.avklartSaksopplysning.shouldBeNull()
        livsoppholdVilkårSykepenger.samletUtfall shouldBe SamletUtfallDTO.UAVKLART

        val søknadMedEtterlønn =
            nySøknad(
                etterlønn = ja(),
            )
        val livsoppholdVilkårEtterlønn = opprettSakOgKjørGetPåLivsopphold(søknadMedEtterlønn)
        livsoppholdVilkårEtterlønn.avklartSaksopplysning.shouldBeNull()
        livsoppholdVilkårEtterlønn.samletUtfall shouldBe SamletUtfallDTO.UAVKLART

        val søknadMedGjenlevendepensjon =
            nySøknad(
                gjenlevendepensjon = periodeJa(fom = 1.januar(2023), tom = 31.mars(2023)),
            )
        val livsoppholdVilkårGjenlevendepensjon =
            opprettSakOgKjørGetPåLivsopphold(søknadMedGjenlevendepensjon)
        livsoppholdVilkårGjenlevendepensjon.avklartSaksopplysning.shouldBeNull()
        livsoppholdVilkårGjenlevendepensjon.samletUtfall shouldBe SamletUtfallDTO.UAVKLART

        val søknadMedSuAlder =
            nySøknad(
                supplerendeStønadAlder = periodeJa(fom = 1.januar(2023), tom = 31.mars(2023)),
            )
        val livsoppholdVilkårSuAlder = opprettSakOgKjørGetPåLivsopphold(søknadMedSuAlder)
        livsoppholdVilkårSuAlder.avklartSaksopplysning.shouldBeNull()
        livsoppholdVilkårSuAlder.samletUtfall shouldBe SamletUtfallDTO.UAVKLART

        val søknadMedSuflykning =
            nySøknad(
                supplerendeStønadFlyktning = periodeJa(fom = 1.januar(2023), tom = 31.mars(2023)),
            )
        val livsoppholdVilkårSuflykning = opprettSakOgKjørGetPåLivsopphold(søknadMedSuflykning)
        livsoppholdVilkårSuflykning.avklartSaksopplysning.shouldBeNull()
        livsoppholdVilkårSuflykning.samletUtfall shouldBe SamletUtfallDTO.UAVKLART

        val søknadMedJobbsjansen =
            nySøknad(
                jobbsjansen = periodeJa(fom = 1.januar(2023), tom = 31.mars(2023)),
            )
        val livsoppholdVilkårJobbsjansen = opprettSakOgKjørGetPåLivsopphold(søknadMedJobbsjansen)
        livsoppholdVilkårJobbsjansen.avklartSaksopplysning.shouldBeNull()
        livsoppholdVilkårJobbsjansen.samletUtfall shouldBe SamletUtfallDTO.UAVKLART

        val søknadMedPensjonsinntekt =
            nySøknad(
                trygdOgPensjon = periodeJa(fom = 1.januar(2023), tom = 31.mars(2023)),
            )
        val livsoppholdVilkårPensjonsinntekt = opprettSakOgKjørGetPåLivsopphold(søknadMedPensjonsinntekt)
        livsoppholdVilkårPensjonsinntekt.avklartSaksopplysning.shouldBeNull()
        livsoppholdVilkårPensjonsinntekt.samletUtfall shouldBe SamletUtfallDTO.UAVKLART

        val søknadMedAlderpensjon =
            nySøknad(
                alderspensjon = fraOgMedDatoJa(fom = 1.januar(2023)),
            )
        val livsoppholdVilkårAlderpensjon = opprettSakOgKjørGetPåLivsopphold(søknadMedAlderpensjon)
        livsoppholdVilkårAlderpensjon.avklartSaksopplysning.shouldBeNull()
        livsoppholdVilkårAlderpensjon.samletUtfall shouldBe SamletUtfallDTO.UAVKLART
    }

    private suspend fun opprettSakOgKjørGetPåLivsopphold(
        søknad: Søknad,
    ): LivsoppholdVilkårDTO {
        lateinit var livsoppholdVilkårDTO: LivsoppholdVilkårDTO

        with(TestApplicationContext()) {
            val tac = this
            val sak = this.førstegangsbehandlingUavklart(
                søknad = søknad,
                fnr = søknad.fnr,
            )
            val behandlingId = sak.førstegangsbehandling.id
            testApplication {
                application {
                    configureExceptions()
                    jacksonSerialization()
                    routing {
                        livsoppholdRoutes(
                            innloggetSaksbehandlerProvider = mockInnloggetSaksbehandlerProvider,
                            livsoppholdVilkårService = tac.førstegangsbehandlingContext.livsoppholdVilkårService,
                            behandlingService = tac.førstegangsbehandlingContext.behandlingService,
                            auditService = tac.personContext.auditService,
                        )
                    }
                }

                defaultRequest(
                    HttpMethod.Get,
                    url {
                        protocol = URLProtocol.HTTPS
                        path("$BEHANDLING_PATH/$behandlingId/vilkar/livsopphold")
                    },
                ).apply {
                    status shouldBe HttpStatusCode.OK
                    livsoppholdVilkårDTO = objectMapper.readValue<LivsoppholdVilkårDTO>(bodyAsText())
                }
            }
        }

        return livsoppholdVilkårDTO
    }

    private fun bodyLivsoppholdYtelse(
        periodeDTO: PeriodeDTO,
        harYtelse: Boolean,
    ): String {
        val harYtelseString = if (harYtelse) "true" else "false"
        return """
            {
              "ytelseForPeriode": 
                {
                  "periode": {
                    "fraOgMed": "${periodeDTO.fraOgMed}",
                    "tilOgMed": "${periodeDTO.tilOgMed}"
                  },
                  "harYtelse": $harYtelseString
                }
            }
        """.trimIndent()
    }
}
