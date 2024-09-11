package no.nav.tiltakspenger.vedtak.context

import no.nav.tiltakspenger.saksbehandling.ports.TiltakGateway
import no.nav.tiltakspenger.vedtak.Configuration
import no.nav.tiltakspenger.vedtak.auth.AzureTokenProvider
import no.nav.tiltakspenger.vedtak.clients.tiltak.TiltakClientImpl
import no.nav.tiltakspenger.vedtak.clients.tiltak.TiltakGatewayImpl

open class TiltakContext {
    private val tokenProviderTiltak by lazy { AzureTokenProvider(config = Configuration.oauthConfigTiltak()) }
    private val tiltakClient by lazy { TiltakClientImpl(getToken = tokenProviderTiltak::getToken) }

    open val tiltakGateway: TiltakGateway by lazy { TiltakGatewayImpl(tiltakClient) }
}
