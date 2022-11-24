## Oppsummering av hendelse


## Rotårsak til feilen
Vi lagret ikke identen fra søknaden. Den ble satt på Innsending, men update-kallet i Repositoryet oppdaterte bare tilstand. Testene fanget ikke opp dette, de var ikke gode nok. (Det var en endring der i at Innsending har både journalpostId og ident, mens Søker bare hadde ident, og det ekstra feltet som nå er med tok ikke testene (eller repoet) høyde for.

## Læring

## Hva gjorde vi bra
Vi fant feilen og rettet den?

## Hva gjorde vi dårlig

## Hvordan hindrer vi at noe lignende skjer igjen? (tiltak)
stikkord:
- lage flere tester
- ikke så store brancher/kanskje ikke branch?
