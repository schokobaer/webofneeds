package won.bot.framework.eventbot.event.impl.telegram;

import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import won.bot.framework.eventbot.event.BaseEvent;

public class SendHelpEvent extends BaseEvent{
    private AbsSender absSender;
    private User user;
    private Chat chat;
    private String[] strings;

    public SendHelpEvent(AbsSender absSender, User user, Chat chat, String[] strings) {
        this.absSender = absSender;
        this.user = user;
        this.chat = chat;
        this.strings = strings;
    }

    public AbsSender getAbsSender() {
        return absSender;
    }

    public User getUser() {
        return user;
    }

    public Chat getChat() {
        return chat;
    }

    public String[] getStrings() {
        return strings;
    }
}
