package dev.gustavo.finance.data.mapper

import dev.gustavo.finance.domain.util.DataError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

class DataErrorMapperTest {

    @Test
    fun `IOException should map to NO_INTERNET`() {
        val exception = IOException("No internet")
        assertEquals(DataError.Network.NO_INTERNET, exception.toDataError())
    }

    @Test
    fun `Unknown exception should map to UNKNOWN`() {
        val exception = Exception("Random error")
        assertEquals(DataError.Network.UNKNOWN, exception.toDataError())
    }
    
    // Note: Testing ResponseException subtypes like ClientRequestException 
    // requires a mock HttpResponse which is hard to create manually in KMP without MockK.
    // We'll focus on the branches we can easily trigger or add more if we add a mocking library.
}
