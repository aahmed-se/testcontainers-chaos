package testcontainers.chaos;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.junit.Test;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PulsarContainer;
import org.testcontainers.containers.ToxiproxyContainer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


public class PulsarContainerTest {

    public static final String TEST_TOPIC = "test_topic";
    PulsarContainer pulsar;
    Network network;
    ToxiproxyContainer toxiproxy;
    ToxiproxyContainer.ContainerProxy pulsarProxy;
    ToxiproxyContainer.ContainerProxy pulsarHttpProxy;

    @Test
    public void testUsage() {
        network = Network.newNetwork();
        pulsar = new PulsarContainer().withNetwork(network);
        toxiproxy = new ToxiproxyContainer().withNetwork(network);
        pulsarProxy = toxiproxy.getProxy(pulsar, 6650);
        pulsarHttpProxy = toxiproxy.getProxy(pulsar, 8080);

        pulsarProxy.toxics();
    }

    protected void testPulsarFunctionality(String pulsarBrokerUrl) throws Exception {

        try (
            PulsarClient client = PulsarClient.builder()
                .serviceUrl(pulsarBrokerUrl)
                .build();

            Consumer consumer = client.newConsumer()
                .topic(TEST_TOPIC)
                .subscriptionName("test-subs")
                .subscribe();
            
            Producer<byte[]> producer = client.newProducer()
                .topic(TEST_TOPIC)
                .create()
        ) {
            producer.send("test containers".getBytes());
            CompletableFuture<Message> future = consumer.receiveAsync();
            Message message = future.get(5, TimeUnit.SECONDS);

            assertThat(new String(message.getData()))
                .isEqualTo("test containers");
        }
    }

}