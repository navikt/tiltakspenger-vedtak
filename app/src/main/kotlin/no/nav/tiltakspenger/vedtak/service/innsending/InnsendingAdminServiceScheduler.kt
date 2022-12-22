package no.nav.tiltakspenger.vedtak.service.innsending

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging


private val LOG = KotlinLogging.logger {}

class InnsendingAdminServiceScheduler(
    private val innsendingAdminService: InnsendingAdminService
) {
    companion object {
        const val TO_DØGN = 172800000L
    }

    fun scheduledTask(): Boolean {
        LOG.info("Launching background task")
        val launch: Job = GlobalScope.launch {
            while (true) {
                LOG.info("Venter i to døgn")
                delay(TO_DØGN)
                LOG.info("Våknet etter to døgn, resetter alle ferdigstilte innsendinger")
                innsendingAdminService.resettInnsendingerSomErFerdigstilt()
                LOG.info("Har resatt alle ferdigstilte innsendinger")
            }
        }
        return launch.isActive
    }
}
