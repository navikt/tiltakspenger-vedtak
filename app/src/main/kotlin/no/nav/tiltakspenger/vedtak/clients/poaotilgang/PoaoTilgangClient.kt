package no.nav.tiltakspenger.vedtak.clients.poaotilgang

import kotlinx.coroutines.runBlocking
import no.nav.poao_tilgang.client.NavAnsattNavIdentBehandleFortroligBrukerePolicyInput
import no.nav.poao_tilgang.client.NavAnsattNavIdentBehandleSkjermedePersonerPolicyInput
import no.nav.poao_tilgang.client.NavAnsattNavIdentBehandleStrengtFortroligBrukerePolicyInput
import no.nav.poao_tilgang.client.PoaoTilgangClient
import no.nav.poao_tilgang.client.PoaoTilgangHttpClient
import no.nav.tiltakspenger.libs.common.AccessToken
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.saksbehandling.ports.PoaoTilgangGateway

class PoaoTilgangClient(
    baseUrl: String,
    val getToken: suspend () -> AccessToken,
) : PoaoTilgangGateway {
    // TODO post-mvp jah: Her mister vi kontroll over om vi får 401 eller 403. Vi bør håndtere dette og invalidere token.
    //  Dette bør sees på dersom denne fila taes i bruk.
    private val poaoTilgangclient: PoaoTilgangClient =
        PoaoTilgangHttpClient(
            baseUrl = baseUrl,
            tokenProvider = { runBlocking { getToken().token } },
        )

    override suspend fun evaluerTilgangTilSkjermet(navAnsattIdent: String): Boolean {
        val response =
            poaoTilgangclient.evaluatePolicy(NavAnsattNavIdentBehandleSkjermedePersonerPolicyInput(navAnsattIdent))
                .getOrThrow()
        return response.isPermit
    }

    override suspend fun evaluerTilgangTilFortrolig(navAnsattIdent: String): Boolean {
        val response =
            poaoTilgangclient.evaluatePolicy(NavAnsattNavIdentBehandleFortroligBrukerePolicyInput(navAnsattIdent))
                .getOrThrow()
        return response.isPermit
    }

    override suspend fun evaluerTilgangTilStrengtFortrolig(navAnsattIdent: String): Boolean {
        val response =
            poaoTilgangclient.evaluatePolicy(NavAnsattNavIdentBehandleStrengtFortroligBrukerePolicyInput(navAnsattIdent))
                .getOrThrow()
        return response.isPermit
    }

    override suspend fun erSkjermet(fnr: Fnr): Boolean {
        return poaoTilgangclient.erSkjermetPerson(fnr.toString()).getOrThrow()
    }

    override suspend fun erSkjermetBolk(fnrListe: List<Fnr>): Map<Fnr, Boolean> {
        val fnrSomStringsListe = fnrListe.map { it.toString() }
        return poaoTilgangclient.erSkjermetPerson(fnrSomStringsListe).getOrThrow().mapKeys { Fnr.fromString(it.key) }
    }
}
