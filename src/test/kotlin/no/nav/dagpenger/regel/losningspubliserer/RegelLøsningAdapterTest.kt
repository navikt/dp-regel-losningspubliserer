package no.nav.dagpenger.regel.losningspubliserer

import io.kotest.matchers.shouldBe
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RegelLøsningAdapterTest {
    private val testrapid = TestRapid()

    init {
        RegelLøsningAdapter(testrapid)
    }

    @BeforeEach
    fun reset() {
        testrapid.reset()
    }

    @Test
    fun `Skal motta løsning på vurdering av minste inntekt og videresende løsningen på riktig format`() {
        testrapid.sendTestMessage(
            """
            {
                "behovId": "finBehovId",
                "beregningsDato": "2019-02-27",
                "minsteinntektResultat": { }
            }
            """.trimIndent(),
        )
        testrapid.inspektør.size shouldBe 1
        testrapid.inspektør.message(0).let { message ->
            message["behovId"].asText() shouldBe "finBehovId"
            message["@prosessertAv"].asText() shouldBe "dp-regel-losningspubliserer"
        }
    }

    @Test
    fun `Skal ikke behandle prosesserte meldinger`() {
        //language=JSON
        testrapid.sendTestMessage(
            """
            {
                "behovId": "ekstraFinBehovId",
                "beregningsDato": "2019-02-27",
                "minsteinntektResultat": { },
                "@prosessertAv": "dp-regel-losningspubliserer"
            }
            """.trimIndent(),
        )
        testrapid.inspektør.size shouldBe 0
    }

    @Test
    fun `Skal ikke behandle meldinger som har andre resultater i tillegg til minsteinntektResultat`() {
        //language=JSON
        testrapid.sendTestMessage(
            """
            {
                "behovId": "ekstraFinBehovId",
                "beregningsDato": "2019-02-27",
                "minsteinntektResultat": { },
                "periodeResultat": { },
                "@prosessertAv": "dp-regel-losningspubliserer"
            }
            """.trimIndent(),
        )
        testrapid.inspektør.size shouldBe 0
    }
}
