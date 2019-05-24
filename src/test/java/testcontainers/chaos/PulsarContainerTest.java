package testcontainers.chaos;

import eu.rekawek.toxiproxy.model.ToxicDirection;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.junit.Test;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PulsarContainer;
import org.testcontainers.containers.ToxiproxyContainer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class PulsarContainerTest {

    private static final String TEST_TOPIC = "test_topic";
    private static final int NUM_OF_MESSAGES = 100;
    private PulsarContainer pulsar;
    private Network network;
    private ToxiproxyContainer toxiproxy;
    private ToxiproxyContainer.ContainerProxy pulsarProxy;
    private ToxiproxyContainer.ContainerProxy pulsarHttpProxy;

    @Test
    public void testUsage() throws Exception {
        network = Network.newNetwork();
        pulsar = new PulsarContainer().withNetwork(network);

        toxiproxy = new ToxiproxyContainer().withNetwork(network);

        pulsar.start();
        toxiproxy.start();

        pulsarProxy = toxiproxy.getProxy(pulsar, 6650);
        pulsarHttpProxy = toxiproxy.getProxy(pulsar, 8080);

        final String brokerUrl = "pulsar://" +
                pulsarProxy.getContainerIpAddress() + ":" + pulsarProxy.getProxyPort();

        log.info("pulsar broker Url : {}", brokerUrl);

        final String pulsarHttoUrl = "http://" +
                pulsarHttpProxy.getContainerIpAddress() + ":" + pulsarHttpProxy.getProxyPort();

        log.info("pulsar http Url : {}", pulsarHttoUrl);

        testPulsarFunctionality("pulsar://" +
                pulsarProxy.getContainerIpAddress() + ":" + pulsarProxy.getProxyPort());

        toxiproxy.stop();
        pulsar.stop();
        network.close();

    }

    protected void testPulsarFunctionality(String pulsarBrokerUrl) throws Exception {

        pulsarProxy.toxics()
                .latency("latencyDownstream", ToxicDirection.DOWNSTREAM, 1_100)
                .setJitter(150);

        pulsarProxy.toxics()
                .latency("latencyUpstream", ToxicDirection.UPSTREAM, 2_100)
                .setJitter(200);

        @Cleanup
        final PulsarClient client = PulsarClient.builder()
                .serviceUrl(pulsarBrokerUrl)
                .build();

        @Cleanup
        final Consumer<String> consumer = client.newConsumer(Schema.STRING)
                .topic(TEST_TOPIC)
                .subscriptionName("test-subs")
                .subscribe();

        @Cleanup
        final Producer<String> producer = client.newProducer(Schema.STRING)
                .topic(TEST_TOPIC)
                .create();

        for (int i = 0; i < NUM_OF_MESSAGES; ++i) {
            producer.send("Hello_" + i);
        }

        for (int i = 0; i < NUM_OF_MESSAGES; ++i) {
            final Message<String> message = consumer.receive();
            assertThat(message.getValue()).matches("Hello_\\d+");
        }
    }

}