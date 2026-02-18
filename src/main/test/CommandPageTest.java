import com.viscan.CommandPage;

public class CommandPageTest {
    public static void main(String[] args) {

        CommandPage page = new CommandPage("test.fxml", "test");
        page.loadTab();
        System.out.println(page.getController());
    }
}
