package no.nav.tiltakspenger.vedtak.auth

import no.nav.tiltakspenger.libs.common.AccessToken

interface EntraIdSystemtokenClient {

    /**
     * @param forceUpdateCache Hvis satt til true, vil systemtoken hentes på nytt, uavhengig av om det finnes i cache. Cache vil også oppdateres.
     */
    suspend fun getSystemtoken(
        otherAppId: String,
        forceUpdateCache: Boolean = false,
    ): AccessToken

    /** Hvis et token har expired, er dette en mulighet for å invalidere det. */
    fun invalidateToken(otherAppId: String)
}
