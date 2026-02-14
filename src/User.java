import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class User {
    private String username;
    private String password;
    private List<Email> inbox;
    private List<Email> outbox;
    private List<Email> trash; // Çöp Kutusu Listesi
    private String profileImage; // Base64 encoded image

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.inbox = new ArrayList<>();
        this.outbox = new ArrayList<>();
        this.trash = new ArrayList<>();
        this.profileImage = null;
    }

    // Getter metodları (Null Safety Eklendi - Liste null ise boş liste döner)
    public String getUsername() {
        return username;
    }

    public List<Email> getInbox() {
        if (inbox == null) inbox = new ArrayList<>();
        return inbox;
    }

    public List<Email> getOutbox() {
        if (outbox == null) outbox = new ArrayList<>();
        return outbox;
    }

    public List<Email> getTrash() {
        if (trash == null) trash = new ArrayList<>();
        return trash;
    }

    public String getProfileImage() {
        return profileImage;
    }

    // Setter metodları
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void addEmail(Email email) {
        getInbox().add(email);
    }

    public void addEmailtoOutbox(Email email) {
        getOutbox().add(email);
    }

    // Çöp kutusuna ekleme ve zaman damgası vurma
    public void addEmailToTrash(Email email) {
        email.setDeletionTimestamp(System.currentTimeMillis()); // Şu anki zamanı kaydet
        getTrash().add(email);
    }

    // Çöp kutusundan geri yükleme (Inbox'a taşır)
    public void restoreEmailFromTrash(Email email) {
        if (getTrash().contains(email)) {
            getTrash().remove(email);
            email.setDeletionTimestamp(0); // Zaman damgasını sıfırla
            getInbox().add(email);
        }
    }

    // 30 günden eski mailleri temizle (Oto Temizlik)
    public void cleanupTrash() {
        long thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000;
        long now = System.currentTimeMillis();

        Iterator<Email> iterator = getTrash().iterator();
        while (iterator.hasNext()) {
            Email email = iterator.next();
            // Süre 0'a ulaştığında veya aştığında (>= 30 gün) anında sil
            if (email.getDeletionTimestamp() > 0 && (now - email.getDeletionTimestamp() >= thirtyDaysInMillis)) {
                iterator.remove();
            }
        }
    }

    // Kullanıcı doğrulama metodu
    public boolean authenticate(String password) {
        return this.password.equals(password);
    }
}