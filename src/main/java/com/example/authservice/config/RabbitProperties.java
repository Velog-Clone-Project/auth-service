package com.example.authservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitProperties {

    private Exchanges exchanges;
    private Queues queues;
    private RoutingKeys routingKeys;

    @Getter @Setter
    public static class Exchanges{
        private String user;
        private String post;
    }

    @Getter @Setter
    public static class Queues{
        private User user;
        private Post post;

        @Getter @Setter
        public static class User{
            private String created;
            private String deleted;
            private  String updated;
        }

        @Getter @Setter
        public static class Post{
            private String created;
            private String deleted;
        }
    }

    @Getter @Setter
    public static class RoutingKeys{
        private User user;
        private Post post;

        @Getter @Setter
        public static class User{
            private String created;
            private String deleted;
            private  String updated;
        }

        @Getter @Setter
        public static class Post{
            private String created;
            private String deleted;
        }
    }


}
