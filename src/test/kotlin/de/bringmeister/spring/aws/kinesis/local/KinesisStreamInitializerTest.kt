package de.bringmeister.spring.aws.kinesis.local

import com.amazonaws.services.kinesis.AmazonKinesis
import com.amazonaws.services.kinesis.model.DescribeStreamResult
import com.amazonaws.services.kinesis.model.ResourceNotFoundException
import com.amazonaws.services.kinesis.model.StreamDescription
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.junit.MockitoJUnitRunner
import java.lang.IllegalStateException

@RunWith(MockitoJUnitRunner::class)
class KinesisStreamInitializerTest {

    @Mock
    private lateinit var kinesis: AmazonKinesis

    @InjectMocks
    private lateinit var streamInitializer: KinesisStreamInitializer

    @Test
    fun `should do nothing if stream already exists`() {
        doReturn(aDescriptionOfAnActiveStream())
            .whenever(kinesis)
            .describeStream("MY_STREAM")

        streamInitializer.createStreamIfMissing("MY_STREAM")
    }

    @Test
    fun `should create missing stream`() {

        whenever(kinesis.describeStream("MY_STREAM"))
            .doThrow(ResourceNotFoundException("Stream not found!")) // not found exception
            .thenReturn(aDescriptionOfAStreamInCreation()) // in creation
            .thenReturn(aDescriptionOfAnActiveStream()) // finally active

        streamInitializer.createStreamIfMissing("MY_STREAM")

        verify(kinesis).createStream("MY_STREAM", 1)
    }

    @Test(expected = IllegalStateException::class) // timeout after 30 seconds!
    fun `should wait for stream in creation and timeout`() {
        doReturn(aDescriptionOfAStreamInCreation())
            .whenever(kinesis)
            .describeStream("MY_STREAM")

        streamInitializer.createStreamIfMissing("MY_STREAM")
    }

    private fun aDescriptionOfAnActiveStream(): DescribeStreamResult {
        return DescribeStreamResult()
                    .withStreamDescription(
                            StreamDescription()
                                .withStreamStatus("ACTIVE")
                    )
    }

    private fun aDescriptionOfAStreamInCreation(): DescribeStreamResult {
        return DescribeStreamResult()
            .withStreamDescription(
                StreamDescription()
                    .withStreamStatus("CREATING")
            )
    }
}