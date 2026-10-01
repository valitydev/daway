package dev.vality.daway.config;

import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class KafkaListenerTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public int getOrder() {
        return 3500;
    }

    @Override
    public void beforeTestMethod(TestContext testContext) {
        var context = testContext.getApplicationContext();
        var broker = context.getBean(EmbeddedKafkaBroker.class);
        var registry = context.getBean(KafkaListenerEndpointRegistry.class);
        registry.getListenerContainers().stream().filter(container -> container.isRunning()).forEach(container ->
                ContainerTestUtils.waitForAssignment(container, broker.getPartitionsPerTopic()));
    }
}
