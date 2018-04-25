package de.bringmeister.spring.aws.kinesis;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaTestListener implements KinesisListener<FooCreatedEvent, EventMetadata>  {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @NotNull
    @Override
    public String streamName() {
        return "foo-event-stream";
    }

    @Override
    public void handle(FooCreatedEvent data, EventMetadata metadata) {
        log.info("Java Kinesis listener caught message");
        JavaListenerTest.LATCH.countDown();
    }
}
