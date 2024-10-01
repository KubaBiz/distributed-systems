import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class Doctor_2 {
    public static void main(String[] argv) throws Exception {

        // info
        System.out.println("DOCTOR 2");

        String EXCHANGE_NAME = "request_exchange";
        String QUEUE_NAME = "Doctor_2";
        String ADMIN_QUEUE = "admin";

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // queue
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        // exchange
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        channel.basicQos(1);

        Thread queue_handler = new Thread(() -> {
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, StandardCharsets.UTF_8);
                    System.out.println("Received: " + message);
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            };
            try {
                channel.basicConsume(QUEUE_NAME, false, consumer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        queue_handler.start();


        while (true) {
            // read msg
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter message: ");
            String message = br.readLine();

            String[] words = message.split("\\s+");
            String first_word = words.length > 0 ? words[0] : "";

            // break condition
            if ("exit".equals(message)) {
                queue_handler.interrupt();
                break;
            }

            if ((!"hip".equalsIgnoreCase(first_word) && !"knee".equalsIgnoreCase(first_word) && !"elbow".equalsIgnoreCase(first_word))){
                System.out.println("Wrong examination type");
                continue;
            }

            if (words.length < 2){
                System.out.println("Wrong message structure [type + surname + optional additional information]");
                continue;
            }

            String final_message = QUEUE_NAME + " " + message;
            // publish
            channel.basicPublish(EXCHANGE_NAME, first_word.toLowerCase(Locale.ROOT), null, final_message.getBytes(StandardCharsets.UTF_8));
            channel.basicPublish("", ADMIN_QUEUE, null, final_message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Sent: " + final_message);
        }
    }
}
