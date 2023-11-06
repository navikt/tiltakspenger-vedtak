package no.nav.tiltakspenger.vedtak.exception.types

import no.nav.tiltakspenger.vedtak.exception.BusinessException
import no.nav.tiltakspenger.vedtak.exception.EntityNotFoundException
import no.nav.tiltakspenger.vedtak.exception.MessageCode

class BehandlingIkkeFunnetException(behandlingId: String) :
    BusinessException(MessageCode.BEHANDLING_FINNES_IKKE, "")
    , EntityNotFoundException
