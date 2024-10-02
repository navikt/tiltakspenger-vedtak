package no.nav.tiltakspenger.vedtak.clients.poaotilgang

import no.nav.poao_tilgang.client.NavAnsattNavIdentBehandleFortroligBrukerePolicyInput
import no.nav.poao_tilgang.client.NavAnsattNavIdentBehandleSkjermedePersonerPolicyInput
import no.nav.poao_tilgang.client.NavAnsattNavIdentBehandleStrengtFortroligBrukerePolicyInput
import no.nav.poao_tilgang.client.PoaoTilgangClient
import no.nav.poao_tilgang.client.PoaoTilgangHttpClient
import no.nav.tiltakspenger.saksbehandling.ports.PoaoTilgangGateway

class PoaoTilgangClient(
    baseUrl: String,
    val getToken: () -> String,
) : PoaoTilgangGateway {
    private val poaoTilgangclient: PoaoTilgangClient =
        PoaoTilgangHttpClient(
            baseUrl = baseUrl,
            tokenProvider = getToken,
        )

    override suspend fun evaluerTilgangTilSkjermet(navAnsattIdent: String): Boolean {
        val response = poaoTilgangclient.evaluatePolicy(NavAnsattNavIdentBehandleSkjermedePersonerPolicyInput(navAnsattIdent)).getOrThrow()
        return response.isPermit
    }

    override suspend fun evaluerTilgangTilFortrolig(navAnsattIdent: String): Boolean {
        val response = poaoTilgangclient.evaluatePolicy(NavAnsattNavIdentBehandleFortroligBrukerePolicyInput(navAnsattIdent)).getOrThrow()
        return response.isPermit
    }

    override suspend fun evaluerTilgangTilStrengtFortrolig(navAnsattIdent: String): Boolean {
        val response = poaoTilgangclient.evaluatePolicy(NavAnsattNavIdentBehandleStrengtFortroligBrukerePolicyInput(navAnsattIdent)).getOrThrow()
        return response.isPermit
    }

    override suspend fun erSkjermet(fnr: String): Boolean {
        return poaoTilgangclient.erSkjermetPerson(fnr).getOrThrow()
    }

    override suspend fun erSkjermetBolk(fnrListe: List<String>): Map<String, Boolean> {
        return poaoTilgangclient.erSkjermetPerson(fnrListe).getOrThrow()
    }
}
