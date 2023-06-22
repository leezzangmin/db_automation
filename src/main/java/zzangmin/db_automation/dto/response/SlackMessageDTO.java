package zzangmin.db_automation.dto.response;

public class SlackMessageDTO {
    private String text;

    public SlackMessageDTO(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
