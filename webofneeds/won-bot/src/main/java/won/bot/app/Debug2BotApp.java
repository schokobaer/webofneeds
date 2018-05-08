package won.bot.app;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class Debug2BotApp {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(
                new Object[]{"classpath:/spring/app/debugBotApp.xml"}
        );
        app.setWebEnvironment(false);
        ConfigurableApplicationContext applicationContext =  app.run(args);

	}

}
