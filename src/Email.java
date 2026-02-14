import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Email implements Serializable {
    private String sender;
    private String recipient;
    private String subject;
    private String content;
    private String timestamp;
    private boolean isRead;
    private boolean isStarred; // Yıldızlı mı?
    private long deletionTimestamp; // Silinme zamanı (Epoch ms cinsinden) - Çöp kutusu için

    public Email(String sender, String recipient, String subject, String content) {
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.isRead = false;
        this.isStarred = false;
        this.deletionTimestamp = 0;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void toggleStar() {
        this.isStarred = !this.isStarred;
    }

    // Liste görünümü için özet metin
    public String getPreview() {
        return String.format("[%s] %s - %s", timestamp, sender, subject);
    }

    public long getDeletionTimestamp() {
        return deletionTimestamp;
    }

    public void setDeletionTimestamp(long deletionTimestamp) {
        this.deletionTimestamp = deletionTimestamp;
    }

    @Override
    public String toString() {
        return getPreview();
    }
}